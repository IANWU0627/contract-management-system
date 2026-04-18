package com.contracthub.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Lightweight startup checks for required DB columns.
 * It only warns and never blocks application startup.
 */
@Component
public class DatabaseSchemaHealthCheck implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaHealthCheck.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaHealthCheck(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        checkContractTypeFieldQuickCodeId();
        checkContractTypeFieldDraftSchema();
        ensurePerformanceMilestoneTable();
    }

    private void checkContractTypeFieldQuickCodeId() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'contract_type_field'
                      AND COLUMN_NAME = 'quick_code_id'
                    """,
                    Integer.class
            );
            if (count == null || count == 0) {
                log.warn("Database schema check: missing column contract_type_field.quick_code_id. " +
                        "Please apply migration V1.4.0__Add_quick_code_id_to_contract_type_field.sql.");
            } else {
                log.info("Database schema check: contract_type_field.quick_code_id is present.");
            }
        } catch (Exception e) {
            log.warn("Database schema check failed for contract_type_field.quick_code_id: {}", e.getMessage());
        }
    }

    /**
     * 草稿表结构由 Flyway（V1.16.0 + V1.19.0）与 schema.sql 提供；应用不再在运行时 ALTER。
     * 仅检测并提示，不自动改表。
     */
    private void checkContractTypeFieldDraftSchema() {
        try {
            Integer tableCount = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM information_schema.TABLES
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'contract_type_field_draft'
                    """,
                    Integer.class
            );
            if (tableCount == null || tableCount == 0) {
                log.warn("Database schema check: table contract_type_field_draft is missing. " +
                        "Apply Flyway migrations through V1.19.0 or import schema.sql.");
                return;
            }
            Integer colCount = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'contract_type_field_draft'
                      AND COLUMN_NAME IN (
                          'contract_type', 'fields_json', 'draft_updated_at',
                          'published_at', 'publish_version'
                      )
                    """,
                    Integer.class
            );
            if (colCount == null || colCount < 5) {
                log.warn("Database schema check: contract_type_field_draft is missing expected columns. " +
                        "Run Flyway migration V1.19.0__Ensure_contract_type_field_draft_schema.sql.");
            } else {
                log.info("Database schema check: contract_type_field_draft schema is present.");
            }
        } catch (Exception e) {
            log.warn("Database schema check failed for contract_type_field_draft: {}", e.getMessage());
        }
    }

    /**
     * 旧库若未执行 V1.15 迁移，访问履约节点会 500。此处幂等补建表与索引（与 Flyway 脚本一致）。
     */
    private void ensurePerformanceMilestoneTable() {
        try {
            Integer tableCount = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*)
                    FROM information_schema.TABLES
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'contract_performance_milestone'
                    """,
                    Integer.class
            );
            if (tableCount == null || tableCount == 0) {
                log.warn("Database schema: table contract_performance_milestone missing; creating (bootstrap).");
                jdbcTemplate.execute(
                        """
                        CREATE TABLE IF NOT EXISTS contract_performance_milestone (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            contract_id BIGINT NOT NULL,
                            title VARCHAR(512) NOT NULL,
                            due_date DATE NULL,
                            offset_days INT NULL,
                            anchor_note VARCHAR(64) NULL,
                            raw_snippet VARCHAR(1024) NULL,
                            status VARCHAR(20) DEFAULT 'PENDING',
                            source VARCHAR(32) DEFAULT 'EXTRACTED',
                            last_reminded_at TIMESTAMP NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                        """
                );
            }
            ensureIndexIfAbsent(
                    "idx_cpm_contract",
                    "contract_performance_milestone",
                    "CREATE INDEX idx_cpm_contract ON contract_performance_milestone(contract_id)"
            );
            ensureIndexIfAbsent(
                    "idx_cpm_due_status",
                    "contract_performance_milestone",
                    "CREATE INDEX idx_cpm_due_status ON contract_performance_milestone(due_date, status)"
            );
            log.info("Database schema check: contract_performance_milestone is ready.");
        } catch (Exception e) {
            log.error("Database schema: failed to ensure contract_performance_milestone: {}", e.getMessage(), e);
        }
    }

    private void ensureIndexIfAbsent(String indexName, String tableName, String createDdl) {
        Integer n = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(1)
                FROM information_schema.statistics
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND INDEX_NAME = ?
                """,
                Integer.class,
                tableName,
                indexName
        );
        if (n == null || n == 0) {
            jdbcTemplate.execute(createDdl);
            log.info("Database schema: created index {} on {}", indexName, tableName);
        }
    }
}
