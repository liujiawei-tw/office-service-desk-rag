package com.example.ticketserver.dto;

import com.example.ticketserver.TicketCategory;
import com.example.ticketserver.entity.Ticket;
import java.time.LocalDateTime;

public record TicketResponse(
        String ticketId,
        String requesterName,
        TicketCategory category,
        String issue,
        String status,
        LocalDateTime createdAt) {

    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getTicketId(),
                ticket.getRequesterName(),
                ticket.getCategory(),
                ticket.getIssue(),
                ticket.getStatus(),
                ticket.getCreatedAt());
    }
}
