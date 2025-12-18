-- Create notification template table to store message templates per notification type and user role
CREATE TABLE notification_template_table (
    template_id VARCHAR(36) PRIMARY KEY,
    notification_type VARCHAR(50) NOT NULL,
    user_role VARCHAR(50) NOT NULL,
    title_template TEXT,
    message_template TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Unique constraint for (notification_type, user_role)
CREATE UNIQUE INDEX uq_notification_template_type_role ON notification_template_table(notification_type, user_role);

CREATE INDEX idx_notification_template_type ON notification_template_table(notification_type);
CREATE INDEX idx_notification_template_role ON notification_template_table(user_role);
