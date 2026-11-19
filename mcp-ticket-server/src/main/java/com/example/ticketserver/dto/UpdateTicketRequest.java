package com.example.ticketserver.dto;

import com.example.ticketserver.TicketConstraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request body for updating a ticket. */
public record UpdateTicketRequest(
        @NotBlank(message = "報修人名稱不可空白")
        @Size(max = TicketConstraints.MAX_REQUESTER_NAME_LENGTH, message = "報修人名稱不可超過 100 個字元")
        String requesterName,
        @NotBlank(message = "問題描述不可空白")
        @Size(max = TicketConstraints.MAX_ISSUE_LENGTH, message = "問題描述不可超過 2000 個字元")
        String issue,
        @NotBlank(message = "狀態不可空白")
        @Size(max = TicketConstraints.MAX_STATUS_LENGTH, message = "狀態不可超過 100 個字元")
        String status) {
}
