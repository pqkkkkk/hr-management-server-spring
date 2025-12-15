-- Create notification table
CREATE TABLE notification_table (
    notification_id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    reference_type VARCHAR(50) NOT NULL,
    reference_id VARCHAR(36),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    recipient_id VARCHAR(36) NOT NULL,
    
    CONSTRAINT fk_notification_recipient 
        FOREIGN KEY (recipient_id) 
        REFERENCES user_table(user_id) 
        ON DELETE CASCADE
);

-- Create index for faster queries
CREATE INDEX idx_notification_recipient ON notification_table(recipient_id);
CREATE INDEX idx_notification_is_read ON notification_table(is_read);
CREATE INDEX idx_notification_type ON notification_table(notification_type);
