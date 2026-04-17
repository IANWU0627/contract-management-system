-- 回填历史审批快照数据：
-- 仅当 contract 仍保留旧快照列时执行；脚本可在不同环境安全运行。

SET @has_legacy_snapshot_columns = (
    SELECT CASE WHEN COUNT(*) = 3 THEN 1 ELSE 0 END
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'contract'
      AND column_name IN (
          'approval_snapshot_content',
          'template_variables_snapshot',
          'approval_snapshot_meta'
      )
);

SET @backfill_sql = IF(
    @has_legacy_snapshot_columns = 1,
    'INSERT INTO contract_snapshot (
         contract_id,
         snapshot_type,
         content,
         template_variables,
         snapshot_meta,
         content_hash,
         created_by,
         created_at
     )
     SELECT
         c.id,
         ''SUBMIT_APPROVAL'' AS snapshot_type,
         c.approval_snapshot_content,
         c.template_variables_snapshot,
         c.approval_snapshot_meta,
         NULL AS content_hash,
         c.created_by,
         COALESCE(c.submitted_at, c.updated_at, c.created_at, CURRENT_TIMESTAMP) AS created_at
     FROM contract c
     WHERE (c.approval_snapshot_content IS NOT NULL OR c.template_variables_snapshot IS NOT NULL OR c.approval_snapshot_meta IS NOT NULL)
       AND NOT EXISTS (
           SELECT 1
           FROM contract_snapshot s
           WHERE s.contract_id = c.id
             AND s.snapshot_type = ''SUBMIT_APPROVAL''
       )',
    'SELECT 1'
);

PREPARE stmt FROM @backfill_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
