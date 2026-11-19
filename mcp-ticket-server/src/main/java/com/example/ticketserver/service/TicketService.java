package com.example.ticketserver.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import com.example.ticketserver.TicketCategory;
import com.example.ticketserver.dto.TicketResponse;
import com.example.ticketserver.entity.Ticket;
import com.example.ticketserver.repository.TicketRepository;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

    private static final String DEFAULT_STATUS = "待處理";

    private final TicketRepository repository;

    public TicketService(TicketRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> findAll() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(TicketResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<TicketResponse> findByTicketId(String ticketId) {
        return repository.findByTicketId(normalize(ticketId)).map(TicketResponse::from);
    }

    @Transactional
    public TicketResponse create(String requesterName, TicketCategory category, String issue) {
        String temporaryTicketId = UUID.randomUUID().toString().replace("-", "");
        Ticket ticket = new Ticket(temporaryTicketId, requesterName, category, issue, DEFAULT_STATUS);
        repository.saveAndFlush(ticket);

        ticket.setTicketId(String.format(Locale.ROOT, "IT-%03d", ticket.getId()));
        return TicketResponse.from(repository.saveAndFlush(ticket));
    }

    @Transactional
    public Optional<TicketResponse> update(String ticketId, String requesterName, String issue, String status) {
        return repository.findByTicketId(normalize(ticketId)).map(ticket -> {
            ticket.setRequesterName(requesterName);
            ticket.setIssue(issue);
            ticket.setStatus(status);
            return TicketResponse.from(ticket);
        });
    }

    @Transactional
    public Optional<TicketResponse> updateStatus(String ticketId, String status) {
        return repository.findByTicketId(normalize(ticketId)).map(ticket -> {
            ticket.setStatus(status);
            return TicketResponse.from(ticket);
        });
    }

    @Transactional
    public Optional<TicketResponse> updateIssue(String ticketId, String issue) {
        return repository.findByTicketId(normalize(ticketId)).map(ticket -> {
            ticket.setIssue(issue);
            return TicketResponse.from(ticket);
        });
    }

    @Transactional
    public Optional<TicketResponse> updateRequesterName(String ticketId, String requesterName) {
        return repository.findByTicketId(normalize(ticketId)).map(ticket -> {
            ticket.setRequesterName(requesterName);
            return TicketResponse.from(ticket);
        });
    }

    @Transactional
    public boolean delete(String ticketId) {
        return repository.findByTicketId(normalize(ticketId)).map(ticket -> {
            repository.delete(ticket);
            return true;
        }).orElse(false);
    }

    @Transactional
    public void ensureSampleTickets() {
        addSampleIfMissing("IT-001", "王小明", TicketCategory.NETWORK, "VPN 無法連線", "處理中");
        addSampleIfMissing("IT-002", "陳怡君", TicketCategory.PERIPHERAL, "印表機異常", "已完成");
        addSampleIfMissing("IT-003", "林志宏", TicketCategory.COMPUTER, "電腦無法開機", DEFAULT_STATUS);
    }

    private void addSampleIfMissing(
            String ticketId, String requesterName, TicketCategory category, String issue, String status) {
        if (repository.findByTicketId(ticketId).isEmpty()) {
            repository.save(new Ticket(ticketId, requesterName, category, issue, status));
        }
    }

    private String normalize(String ticketId) {
        return ticketId == null ? null : ticketId.trim().toUpperCase(Locale.ROOT);
    }
}