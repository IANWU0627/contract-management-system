-- 通知中心持久化 + 提醒用户隔离

CREATE TABLE IF NOT EXISTS notification_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  type VARCHAR(64) NOT NULL,
  title VARCHAR(255) NOT NULL,
  content TEXT,
  data TEXT,
  is_read BOOLEAN DEFAULT FALSE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE contract_reminder
  ADD COLUMN IF NOT EXISTS recipient_user_id BIGINT NULL;

UPDATE contract_reminder cr
JOIN contract c ON c.id = cr.contract_id
SET cr.recipient_user_id = c.creator_id
WHERE cr.recipient_user_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_notification_user_created ON notification_message(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notification_user_read ON notification_message(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_reminder_recipient_created ON contract_reminder(recipient_user_id, created_at DESC);
