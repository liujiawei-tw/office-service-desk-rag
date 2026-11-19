package com.example.ticketserver;

/** Shared limits for ticket fields stored in the database. */
public final class TicketConstraints {

    public static final int MAX_TICKET_ID_LENGTH = 32;
    public static final int MAX_REQUESTER_NAME_LENGTH = 100;
    public static final int MAX_ISSUE_LENGTH = 2000;
    public static final int MAX_STATUS_LENGTH = 100;

    private TicketConstraints() {
    }
}
