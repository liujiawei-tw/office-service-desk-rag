package com.example.ticketserver.config;

import com.example.ticketserver.service.TicketService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TicketDataInitializer {

    @Bean
    CommandLineRunner initializeSampleTickets(TicketService ticketService) {
        return args -> ticketService.ensureSampleTickets();
    }
}
