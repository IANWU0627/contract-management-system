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
}
