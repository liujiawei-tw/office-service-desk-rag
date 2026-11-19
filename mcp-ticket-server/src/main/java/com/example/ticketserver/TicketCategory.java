package com.example.ticketserver;

import java.util.Arrays;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Categories aligned with the numbered knowledge-document groups. */
public enum TicketCategory {
    NETWORK("網路"),
    COMPUTER("電腦"),
    PERIPHERAL("周邊設備"),
    ACCOUNT("帳號與權限"),
    OFFICE_SOFTWARE("辦公軟體"),
    TICKET_SERVICE("報修服務"),
    MEETING_EQUIPMENT("會議設備"),
    OFFICE_FACILITY("辦公設施"),
    OTHER("其他");

    private final String label;

    TicketCategory(String label) {
        this.label = label;
    }

    @JsonValue
    public String label() {
        return label;
    }

    @JsonCreator
    public static TicketCategory fromJson(String value) {
        return parse(value).orElseThrow(() -> new IllegalArgumentException("不支援的報修類別"));
    }

    public static Optional<TicketCategory> parse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String normalized = value.trim();
        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(normalized)
                        || category.label.equals(normalized))
                .findFirst();
    }
}