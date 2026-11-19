package com.example.helpdesk.service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Collections;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.helpdesk.dto.ChatResponse;
import com.example.helpdesk.dto.RetrievedChunk;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** 負責檢索知識文件、呼叫 LLM，並將 MCP 查票工具提供給模型。 */
@Service
public class HelpdeskChatService {

    private static final String NO_KNOWLEDGE_ANSWER =
            "知識庫中沒有找到足夠資訊，請聯絡 IT 部門。";
    private static final Pattern TICKET_ID = Pattern.compile("(?i)\\bIT-\\d{3,}\\b");

    private final ChatClient chatClient;
    private final SimpleVectorStore vectorStore;
    private final ToolCallbackProvider ticketServerTools;
    private final ToolCallTracker toolCallTracker;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final int topK;
    private final double similarityThreshold;
    private static final int MAX_CONVERSATIONS = 500;
    private static final int MAX_TURNS = 12;
    private final Map<String, List<Turn>> conversations = Collections.synchronizedMap(
            new LinkedHashMap<>(64, 0.75f, true) {
                @Override protected boolean removeEldestEntry(Map.Entry<String, List<Turn>> eldest) {
                    return size() > MAX_CONVERSATIONS;
                }
            });

    public HelpdeskChatService(ChatClient chatClient, SimpleVectorStore vectorStore,
            ToolCallbackProvider ticketServerTools,
            ToolCallTracker toolCallTracker,
            @Value("${helpdesk.rag.top-k:4}") int topK,
            @Value("${helpdesk.rag.similarity-threshold:0.3}") double similarityThreshold) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.ticketServerTools = ticketServerTools;
        this.toolCallTracker = toolCallTracker;
        this.topK = topK;
        this.similarityThreshold = similarityThreshold;
    }

    /** 檢索相關文件後生成回答；遇到報修單編號時要求模型使用 MCP 工具查詢。 */
    public String answer(String question) {
        return answerWithMetadata(question).answer();
    }

    public ChatResponse answerWithMetadata(String question) {
        return answerWithMetadata(question, null);
    }

    public ChatResponse answerWithMetadata(String question, String conversationId) {
        String safeConversationId = conversationId == null || conversationId.isBlank() ? null : conversationId.trim();
        ToolCallTracker.Capture<ChatResponse> capture = toolCallTracker.capture(() -> answerTracked(question, safeConversationId));
        ChatResponse response = capture.value();
        return new ChatResponse(response.answer(), response.sources(), capture.toolNames(), response.retrievedChunks());
    }

    private ChatResponse answerTracked(String question, String conversationId) {
        // 先做向量相似度搜尋，只把相似度達標的文件交給模型。
        List<Document> matches = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(topK)
                        .similarityThreshold(similarityThreshold)
                        .build());

        List<String> sources = matches.stream()
                .map(document -> String.valueOf(document.getMetadata().getOrDefault("source", "知識文件")))
                .filter(source -> !source.isBlank())
                .distinct()
                .toList();
        List<RetrievedChunk> retrievedChunks = matches.stream()
                .map(document -> new RetrievedChunk(
                        String.valueOf(document.getMetadata().getOrDefault("source", "知識文件")),
                        String.valueOf(document.getMetadata().getOrDefault("chunk_id", document.getId())),
                        document.getScore(),
                        true))
                .toList();

        Matcher matcher = TICKET_ID.matcher(question);
        List<String> ticketIds = new java.util.ArrayList<>();
        while (matcher.find()) {
            String ticketId = matcher.group();
            if (!ticketIds.contains(ticketId)) {
                ticketIds.add(ticketId);
            }
        }

        boolean asksAboutTicket = !ticketIds.isEmpty();
        List<Turn> previousTurns = loadTurns(conversationId);
        boolean explicitlyRequestsTicket = Pattern.compile("(?s).*((建立|新增|提交|開立).*(報修單|報修)|(報修單|報修).*(建立|新增|提交|開立)).*")
                .matcher(question).matches();
        // 沒有知識命中且不是報修單查詢時直接拒答，避免 LLM 依常識補出公司政策。
        if (matches.isEmpty() && !asksAboutTicket && !explicitlyRequestsTicket && previousTurns.isEmpty()) {
            return new ChatResponse(NO_KNOWLEDGE_ANSWER, List.of(), List.of(), retrievedChunks);
        }

        String ticketStatusContext = "";
        if (asksAboutTicket) {
            ticketStatusContext = ticketIds.stream()
                    .map(ticketId -> {
                        String ticketStatus = callTicketStatusTool(ticketId);
                        if (ticketStatus != null && !ticketStatus.isBlank()) {
                            return "報修單 " + ticketId + "：" + ticketStatus;
                        }
                        return "報修單 " + ticketId + "：目前無法取得資料。";
                    })
                    .reduce((left, right) -> left + "\n" + right)
                    .orElse("");
        }

        String context = matches.isEmpty()
                ? "沒有找到相關知識文件。"
                : matches.stream()
                        .map(document -> "[" + document.getMetadata().getOrDefault("source", "知識文件")
                                + "] " + document.getText())
                        .reduce((left, right) -> left + "\n\n" + right)
                        .orElse("沒有找到相關知識文件。");

        // 報修單狀態由 Java 端直接呼叫 MCP 工具取得，並作為回答優先來源。
        String systemPrompt = """
                你是協助一般員工處理 IT 報修與辦公室設備簡易排查的助手，請以平靜、易懂的繁體中文回答。
                
                回答時請遵守以下原則：
                1. 若使用者的問題包含報修單編號，狀態相關內容只能依據下方「報修單狀態」資料回答。這些資料由系統透過 MCP 工具 getTicketStatus 查詢取得；不得猜測、編造或推測未提供的狀態。
                2. 非報修單狀態的 IT 問題，只能依據下方「知識文件」回答。
                3. 可以整合多份知識文件中的資訊進行跨文件推理，但推論的每個前提都必須有文件依據，且結論必須能由這些內容合理推出。不得加入文件未提供的事實、步驟、網址、政策或假設，也不得以一般常識補足文件缺漏。
                4. 若文件之間有矛盾，請指出矛盾及相關文件資訊；除非文件明確說明優先順序，否則不要自行判定哪一份正確。
                5. 若文件只能回答問題的一部分，請先回答有依據的部分，並明確說明知識庫未提供其餘資訊。
                6. 排查步驟應使用一般員工看得懂的簡短步驟。不得指示使用者拆開電器、接觸裸露電線、攀高或進行帶電維修；文件提及安全風險時，優先建議停止操作並通報。
                7. 只有使用者明確要求建立、提交或新增報修單，才呼叫 createTicket；僅描述故障、問排查方式或詢問如何報修都不得建單。必須從目前對話取得姓名和問題描述；缺少時先詢問，絕不可猜姓名或補入未提供的問題事實。類別由你依問題內容選擇以下有效 enum 代碼之一：NETWORK、COMPUTER、PERIPHERAL、ACCOUNT、OFFICE_SOFTWARE、TICKET_SERVICE、MEETING_EQUIPMENT、OFFICE_FACILITY、OTHER；不得詢問使用者選類別。若問題本身不清楚才追問，分類有歧義時選最符合者。issue 僅整理使用者明確提供的事實。
                8. createTicket 成功工具回覆必須明確包含系統報修單編號；只有收到成功結果才可告知已建立。工具錯誤、失敗或結果不明時，告知目前未能確認建立成功，不可重試可能造成重複的呼叫，也不可編造編號。

                報修單狀態：
                %s

                知識文件：
                %s
                """.formatted(ticketStatusContext.isBlank() ? "無" : ticketStatusContext, context);

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));
        for (Turn turn : previousTurns) {
            messages.add(new UserMessage(turn.question()));
            messages.add(new AssistantMessage(turn.answer()));
        }
        messages.add(new UserMessage(question));

        org.springframework.ai.chat.model.ChatResponse aiResponse = chatClient.prompt()
                .messages(messages)
                .call()
                .chatResponse();

        String answer = aiResponse == null || aiResponse.getResult() == null || aiResponse.getResult().getOutput() == null
                ? NO_KNOWLEDGE_ANSWER
                : aiResponse.getResult().getOutput().getText();
        if (answer == null || answer.isBlank()) {
            answer = NO_KNOWLEDGE_ANSWER;
        }

        answer = answer.trim();
        saveTurn(conversationId, question, answer);
        return new ChatResponse(answer, sources, List.of(), retrievedChunks);
    }

    private List<Turn> loadTurns(String conversationId) {
        if (conversationId == null) return List.of();
        synchronized (conversations) {
            return List.copyOf(conversations.getOrDefault(conversationId, List.of()));
        }
    }

    private void saveTurn(String conversationId, String question, String answer) {
        if (conversationId == null) return;
        synchronized (conversations) {
            List<Turn> turns = new ArrayList<>(conversations.getOrDefault(conversationId, List.of()));
            turns.add(new Turn(question, answer));
            if (turns.size() > MAX_TURNS) turns = new ArrayList<>(turns.subList(turns.size() - MAX_TURNS, turns.size()));
            conversations.put(conversationId, List.copyOf(turns));
        }
    }

    private record Turn(String question, String answer) { }

    private String callTicketStatusTool(String ticketId) {
        if (ticketServerTools == null) {
            return null;
        }

        ToolCallback ticketStatusTool = Arrays.stream(ticketServerTools.getToolCallbacks())
                .filter(tool -> tool != null && tool.getToolDefinition() != null)
                .filter(tool -> "getTicketStatus".equals(tool.getToolDefinition().name()))
                .findFirst()
                .orElse(null);

        if (ticketStatusTool == null) {
            return null;
        }

        String arguments;
        try {
            arguments = objectMapper.writeValueAsString(Map.of("ticketId", ticketId));
        } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
            return null;
        }
        toolCallTracker.record("getTicketStatus");
        return ticketStatusTool.call(arguments);
    }
}
