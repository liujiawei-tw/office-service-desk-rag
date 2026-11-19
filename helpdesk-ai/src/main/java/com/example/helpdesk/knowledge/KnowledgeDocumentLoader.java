package com.example.helpdesk.knowledge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

/** 應用啟動時讀取知識 TXT、切塊並建立向量索引。 */
@Component
public class KnowledgeDocumentLoader implements CommandLineRunner {

    private final SimpleVectorStore vectorStore;
    private final ResourcePatternResolver resourcePatternResolver;
    private final String knowledgePattern;

    private final TokenTextSplitter textSplitter;

    public KnowledgeDocumentLoader(
            SimpleVectorStore vectorStore,
            ResourcePatternResolver resourcePatternResolver,
            @Value("${helpdesk.knowledge-pattern:classpath*:/knowledge/*.txt}") String knowledgePattern,
            @Value("${helpdesk.rag.chunk-size:300}") int chunkSize,
            @Value("${helpdesk.rag.min-chunk-size-chars:100}") int minChunkSizeChars,
            @Value("${helpdesk.rag.min-chunk-length-to-embed:5}") int minChunkLengthToEmbed,
            @Value("${helpdesk.rag.keep-separator:true}") boolean keepSeparator) {
        this.vectorStore = vectorStore;
        this.resourcePatternResolver = resourcePatternResolver;
        this.knowledgePattern = knowledgePattern;
        this.textSplitter = TokenTextSplitter.builder()
                .withChunkSize(chunkSize)
                .withMinChunkSizeChars(minChunkSizeChars)
                .withMinChunkLengthToEmbed(minChunkLengthToEmbed)
                .withKeepSeparator(keepSeparator)
                .withPunctuationMarks(List.of('。', '！', '？', '；', '\n'))
                .build();
    }

    /** 載入 TXT 並切成小段；ticket_status.txt 排除在外，避免靜態狀態取代 MCP 查詢。 */
    @Override
    public void run(String... args) throws IOException {
        Resource[] resources = resourcePatternResolver.getResources(knowledgePattern);
        List<Document> documents = Arrays.stream(resources)
                .filter(resource -> resource.getFilename() != null)
                .filter(resource -> !resource.getFilename().equalsIgnoreCase("ticket_status.txt"))
                .sorted(Comparator.comparing(Resource::getFilename))
                .map(this::toDocument)
                .toList();

        if (documents.isEmpty()) {
            throw new IllegalStateException("知識文件樣式沒有符合的 TXT 檔案：" + knowledgePattern);
        }

        // 切分後仍保留來源檔 metadata，回答時可指出資訊來自哪份知識文件。
        Map<String, Integer> chunkNumbers = new HashMap<>();
        List<Document> chunks = textSplitter.apply(documents).stream()
                .map(chunk -> {
                    String source = String.valueOf(chunk.getMetadata().getOrDefault("source", "knowledge"));
                    int chunkNumber = chunkNumbers.merge(source, 1, Integer::sum);
                    String chunkId = source + ":" + String.format(java.util.Locale.ROOT, "%04d", chunkNumber);
                    Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
                    metadata.put("chunk_id", chunkId);
                    return new Document(chunkId, chunk.getText(), metadata);
                })
                .toList();
        if (chunks.isEmpty()) {
            throw new IllegalStateException("知識文件切塊後沒有可索引內容。");
        }
        vectorStore.add(chunks);
    }

    /** 將 TXT 內容及來源檔名包裝成 RAG 文件。 */
    private Document toDocument(Resource resource) {
        try (var inputStream = resource.getInputStream()) {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return new Document(content, Map.of("source", resource.getFilename()));
        } catch (IOException e) {
            throw new IllegalStateException("無法讀取知識文件：" + resource.getDescription(), e);
        }
    }
}

