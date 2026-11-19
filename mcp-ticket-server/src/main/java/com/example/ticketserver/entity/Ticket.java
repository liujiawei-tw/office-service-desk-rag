package com.example.ticketserver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.example.ticketserver.TicketCategory;
import com.example.ticketserver.TicketConstraints;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false, unique = true, length = TicketConstraints.MAX_TICKET_ID_LENGTH)
    private String ticketId;

    @Column(name = "requester_name", nullable = false, length = TicketConstraints.MAX_REQUESTER_NAME_LENGTH)
    private String requesterName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TicketCategory category;

    @Column(nullable = false, length = TicketConstraints.MAX_ISSUE_LENGTH)
    private String issue;

    @Column(nullable = false, length = TicketConstraints.MAX_STATUS_LENGTH)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Ticket() {
    }

    @PrePersist
    void setCreatedAtIfMissing() {
        if (createdAt == null) createdAt = LocalDateTime.now(ZoneId.of("Asia/Taipei"));
    }

    public Ticket(String ticketId, String requesterName, TicketCategory category, String issue, String status) {
        this.ticketId = ticketId;
        this.requesterName = requesterName;
        this.category = category;
        this.issue = issue;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setStatus(String status) {
        this.status = status;
    }
}
