package com.example.ticketserver.controller;

import java.net.URI;
import java.util.List;

import com.example.ticketserver.dto.CreateTicketRequest;
import com.example.ticketserver.dto.TicketResponse;
import com.example.ticketserver.dto.UpdateTicketRequest;
import com.example.ticketserver.service.TicketService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public List<TicketResponse> findAll() {
        return ticketService.findAll();
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> findByTicketId(@PathVariable String ticketId) {
        return ticketService.findByTicketId(ticketId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody CreateTicketRequest request) {
        TicketResponse created = ticketService.create(
                request.requesterName().trim(),
                request.category(),
                request.issue().trim());
        return ResponseEntity.created(URI.create("/api/tickets/" + created.ticketId())).body(created);
    }

    @PutMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> update(
            @PathVariable String ticketId,
            @Valid @RequestBody UpdateTicketRequest request) {
        return ticketService.update(ticketId, request.requesterName().trim(), request.issue().trim(), request.status().trim())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Void> delete(@PathVariable String ticketId) {
        return ticketService.delete(ticketId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}