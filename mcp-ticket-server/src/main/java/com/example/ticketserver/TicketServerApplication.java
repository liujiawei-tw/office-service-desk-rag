package com.example.ticketserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 啟動獨立 Spring Boot MCP 報修服務的應用程式入口。
 */
@SpringBootApplication
public class TicketServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketServerApplication.class, args);
    }
}