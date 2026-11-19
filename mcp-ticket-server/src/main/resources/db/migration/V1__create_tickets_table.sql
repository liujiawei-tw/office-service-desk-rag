CREATE TABLE tickets (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ticket_id VARCHAR(32) NOT NULL,
    issue VARCHAR(2000) NOT NULL,
    status VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_tickets_ticket_id UNIQUE (ticket_id)
) ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;
