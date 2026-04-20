-- 通知中心：重要标记持久化

ALTER TABLE notification_message
  ADD COLUMN IF NOT EXISTS is_important BOOLEAN DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_notification_user_important ON notification_message(user_id, is_important);
