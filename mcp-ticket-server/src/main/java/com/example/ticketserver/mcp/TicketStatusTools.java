package com.example.ticketserver.mcp;

import java.util.Locale;
import java.util.Optional;

import com.example.ticketserver.TicketCategory;
import com.example.ticketserver.TicketConstraints;
import com.example.ticketserver.dto.TicketResponse;
import com.example.ticketserver.service.TicketService;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/** MCP ticket tools; all reads and writes are delegated to TicketService. */
@Component
public class TicketStatusTools {

    private static final String CATEGORY_OPTIONS =
            "NETWORK(網路)、COMPUTER(電腦)、PERIPHERAL(周邊設備)、ACCOUNT(帳號與權限)、"
                    + "OFFICE_SOFTWARE(辦公軟體)、TICKET_SERVICE(報修服務)、"
                    + "MEETING_EQUIPMENT(會議設備)、OFFICE_FACILITY(辦公設施)、OTHER(其他)";

    private final TicketService ticketService;

    public TicketStatusTools(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /** Looks up a ticket using the current database state on every call. */
    @McpTool(name = "getTicketStatus",
            description = "依報修單編號查詢類別、問題與目前處理狀態；查無資料時明確回覆查無此單。")
    public String getTicketStatus(
            @McpToolParam(description = "報修單編號，例如 IT-001", required = true) String ticketId) {
        if (ticketId == null || ticketId.isBlank()) {
            return "請提供報修單編號。";
        }

        String normalizedId = ticketId.trim().toUpperCase(Locale.ROOT);
        return ticketService.findByTicketId(normalizedId)
                .map(ticket -> "報修單 " + normalizedId + "；類別：" + ticket.category().label()
                        + "；問題：" + ticket.issue() + "；目前狀態：" + ticket.status() + "。")
                .orElse("查無報修單 " + normalizedId + "，請確認報修單編號。");
    }

    @McpTool(name = "createTicket",
            description = "僅在使用者明確要求建立、提交或新增報修單時呼叫；只描述故障或詢問如何報修不可呼叫。"
                    + "必須從目前對話取得報修人名稱及問題描述，缺少時先詢問，不得猜測。category 必須是下列 enum 代碼之一，不接受顯示名稱："
                    + "NETWORK, COMPUTER, PERIPHERAL, ACCOUNT, OFFICE_SOFTWARE, TICKET_SERVICE, MEETING_EQUIPMENT, OFFICE_FACILITY, OTHER。"
                    + "問題清楚但分類有歧義時選最符合者。成功時回傳系統產生的編號、類別、狀態與建立時間。")
    public String createTicket(
            @McpToolParam(description = "報修人名稱", required = true) String requesterName,
            @McpToolParam(description = "報修類別 enum 代碼；有效值：NETWORK, COMPUTER, PERIPHERAL, ACCOUNT, OFFICE_SOFTWARE, TICKET_SERVICE, MEETING_EQUIPMENT, OFFICE_FACILITY, OTHER", required = true) String category,
            @McpToolParam(description = "報修單的詳細問題描述", required = true) String issue) {
        if (requesterName == null || requesterName.isBlank()) {
            return "請先提供報修人名稱。";
        }
        if (category == null || category.isBlank()) {
            return "請先提供報修類別。";
        }
        if (issue == null || issue.isBlank()) {
            return "請先提供報修問題描述。";
        }

        String normalizedRequesterName = requesterName.trim();
        if (normalizedRequesterName.length() > TicketConstraints.MAX_REQUESTER_NAME_LENGTH) {
            return "報修人名稱不可超過 100 個字元。";
        }
        Optional<TicketCategory> parsedCategory;
        try {
            parsedCategory = Optional.of(TicketCategory.valueOf(category.trim()));
        } catch (IllegalArgumentException exception) {
            parsedCategory = Optional.empty();
        }
        if (parsedCategory.isEmpty()) {
            return "報修類別不支援，請從以下類別選擇：" + CATEGORY_OPTIONS + "。";
        }
        String normalizedIssue = issue.trim();
        if (normalizedIssue.length() > TicketConstraints.MAX_ISSUE_LENGTH) {
            return "問題描述不可超過 2000 個字元。";
        }

        TicketResponse created = ticketService.create(normalizedRequesterName, parsedCategory.get(), normalizedIssue);
        return "已建立報修單 " + created.ticketId() + "；報修人：" + created.requesterName()
                + "；類別：" + created.category().label() + "；問題：" + created.issue()
                + "；目前狀態：" + created.status() + "；建立時間（台北時間）：「" + created.createdAt() + "」。";
    }

    @McpTool(name = "updateTicketRequesterName",
            description = "僅在使用者明確要求更新報修人名稱時呼叫。缺少報修單編號或名稱時先詢問；查無此單時明確回覆。")
    public String updateTicketRequesterName(
            @McpToolParam(description = "報修單編號，例如 IT-001", required = true) String ticketId,
            @McpToolParam(description = "要更新成的報修人名稱", required = true) String requesterName) {
        if (ticketId == null || ticketId.isBlank() || requesterName == null || requesterName.isBlank()) {
            return "請提供報修單編號和新的報修人名稱。";
        }

        String normalizedId = ticketId.trim().toUpperCase(Locale.ROOT);
        String normalizedRequesterName = requesterName.trim();
        if (normalizedRequesterName.length() > TicketConstraints.MAX_REQUESTER_NAME_LENGTH) {
            return "報修人名稱不可超過 100 個字元。";
        }

        Optional<TicketResponse> updated = ticketService.updateRequesterName(normalizedId, normalizedRequesterName);
        return updated.map(ticket -> "已更新報修單 " + ticket.ticketId() + " 的報修人名稱。")
                .orElse("查無報修單 " + normalizedId + "，未進行更新。");
    }

    @McpTool(name = "updateTicketStatus",
            description = "僅在使用者明確要求更新報修單狀態時呼叫。缺少報修單編號或新狀態時先詢問；查無此單時明確回覆。")
    public String updateTicketStatus(
            @McpToolParam(description = "報修單編號，例如 IT-001", required = true) String ticketId,
            @McpToolParam(description = "要更新成的狀態", required = true) String status) {
        if (ticketId == null || ticketId.isBlank() || status == null || status.isBlank()) {
            return "請提供報修單編號和要更新的狀態。";
        }

        String normalizedId = ticketId.trim().toUpperCase(Locale.ROOT);
        String normalizedStatus = status.trim();
        if (normalizedStatus.length() > TicketConstraints.MAX_STATUS_LENGTH) {
            return "狀態不可超過 100 個字元。";
        }

        Optional<TicketResponse> updated = ticketService.updateStatus(normalizedId, normalizedStatus);
        return updated.map(TicketStatusTools::updatedMessage)
                .orElse("查無報修單 " + normalizedId + "，未進行更新。");
    }

    @McpTool(name = "updateTicketIssue",
            description = "僅在使用者明確要求更新報修單問題描述時呼叫。缺少報修單編號或新問題描述時先詢問；查無此單時明確回覆。")
    public String updateTicketIssue(
            @McpToolParam(description = "報修單編號，例如 IT-001", required = true) String ticketId,
            @McpToolParam(description = "要更新成的問題描述", required = true) String issue) {
        if (ticketId == null || ticketId.isBlank() || issue == null || issue.isBlank()) {
            return "請提供報修單編號和新的問題描述。";
        }

        String normalizedId = ticketId.trim().toUpperCase(Locale.ROOT);
        String normalizedIssue = issue.trim();
        if (normalizedIssue.length() > TicketConstraints.MAX_ISSUE_LENGTH) {
            return "問題描述不可超過 2000 個字元。";
        }

        Optional<TicketResponse> updated = ticketService.updateIssue(normalizedId, normalizedIssue);
        return updated.map(TicketStatusTools::updatedMessage)
                .orElse("查無報修單 " + normalizedId + "，未進行更新。");
    }

    private static String updatedMessage(TicketResponse ticket) {
        return "已更新報修單 " + ticket.ticketId() + "；類別：" + ticket.category().label()
                + "；問題：" + ticket.issue() + "；目前狀態：" + ticket.status() + "。";
    }
}
