package com.example.ticketserver.mcp;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.LocalDateTime;

import com.example.ticketserver.TicketCategory;
import com.example.ticketserver.dto.TicketResponse;
import com.example.ticketserver.service.TicketService;
import org.junit.jupiter.api.Test;

class TicketStatusToolsTest {

    @Test
    void rejectsCategoryLabelInsteadOfEnumCode() {
        RecordingTicketService service = new RecordingTicketService();
        TicketStatusTools tools = new TicketStatusTools(service);

        assertTrue(tools.createTicket("王小明", "網路", "VPN 無法連線").contains("類別不支援"));
        assertTrue(!service.called);
    }

    @Test
    void returnsPersistedTicketFieldsAndServerCreatedAt() {
        RecordingTicketService service = new RecordingTicketService();
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 19, 14, 30);
        service.response = new TicketResponse("IT-123", "王小明", TicketCategory.NETWORK,
                "VPN 無法連線", "待處理", createdAt);
        TicketStatusTools tools = new TicketStatusTools(service);

        String result = tools.createTicket(" 王小明 ", "NETWORK", "VPN 無法連線");

        assertTrue(result.contains("IT-123"));
        assertTrue(result.contains("2026-11-19T14:30"));
        assertTrue(service.called);
        assertTrue(service.requesterName.equals("王小明"));
        assertTrue(service.category == TicketCategory.NETWORK);
        assertTrue(service.issue.equals("VPN 無法連線"));
    }

    private static final class RecordingTicketService extends TicketService {
        private boolean called;
        private String requesterName;
        private TicketCategory category;
        private String issue;
        private TicketResponse response;

        RecordingTicketService() { super(null); }

        @Override
        public TicketResponse create(String requesterName, TicketCategory category, String issue) {
            this.called = true;
            this.requesterName = requesterName;
            this.category = category;
            this.issue = issue;
            return response;
        }
    }
}
