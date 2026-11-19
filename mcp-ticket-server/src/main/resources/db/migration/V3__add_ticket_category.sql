ALTER TABLE tickets
    ADD COLUMN category VARCHAR(40) NOT NULL DEFAULT 'OTHER' AFTER requester_name;

UPDATE tickets
SET category = 'NETWORK'
WHERE ticket_id = 'IT-001' AND issue = 'VPN 無法連線';

UPDATE tickets
SET category = 'PERIPHERAL'
WHERE ticket_id = 'IT-002' AND issue = '印表機異常';

UPDATE tickets
SET category = 'COMPUTER'
WHERE ticket_id = 'IT-003' AND issue = '電腦無法開機';