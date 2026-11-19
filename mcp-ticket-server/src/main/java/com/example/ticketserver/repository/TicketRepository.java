package com.example.ticketserver.repository;

import java.util.Optional;

import com.example.ticketserver.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketId(String ticketId);
}
