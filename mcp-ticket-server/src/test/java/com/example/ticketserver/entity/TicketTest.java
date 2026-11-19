package com.example.ticketserver.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import com.example.ticketserver.TicketCategory;
import org.junit.jupiter.api.Test;

class TicketTest {

    @Test
    void assignsTaipeiCreatedAtOnceAndDoesNotChangeIt() {
        Ticket ticket = new Ticket("IT-101", "王小明", TicketCategory.NETWORK, "VPN 無法連線", "待處理");
        ticket.setCreatedAtIfMissing();
        LocalDateTime createdAt = ticket.getCreatedAt();

        assertNotNull(createdAt);
        assertEquals(ZoneOffset.ofHours(8), ZoneId.of("Asia/Taipei").getRules().getOffset(createdAt));
        ticket.setStatus("處理中");
        ticket.setIssue("VPN 連線中斷");
        ticket.setRequesterName("王小明");
        ticket.setCreatedAtIfMissing();
        assertEquals(createdAt, ticket.getCreatedAt());
    }
}
