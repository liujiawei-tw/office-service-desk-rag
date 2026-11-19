ALTER TABLE tickets
    ADD COLUMN requester_name VARCHAR(100) NULL AFTER ticket_id;

UPDATE tickets
SET requester_name = '未提供'
WHERE requester_name IS NULL;

ALTER TABLE tickets
    MODIFY COLUMN requester_name VARCHAR(100) NOT NULL;
