package com.alivos.api.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * One-time, idempotent data fixups for columns added after the app already had
 * real rows in production (ddl-auto adds them as nullable, so old rows would
 * otherwise read as NULL and silently disappear from status-filtered queries).
 * Safe to run on every startup — every statement only touches rows still at
 * their default/NULL state.
 */
@Component
@RequiredArgsConstructor
@Order(0)
public class SchemaBackfillRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaBackfillRunner.class);

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Legacy Purchase rows predate the course/appointment split — relax the
        // NOT NULL constraint in case Hibernate's ddl-auto didn't do it, and
        // backfill the new discriminator column.
        jdbcTemplate.execute("ALTER TABLE purchases ALTER COLUMN course_id DROP NOT NULL");
        int purchases = jdbcTemplate.update("UPDATE purchases SET type = 'COURSE' WHERE type IS NULL");

        int reviews = jdbcTemplate.update("UPDATE course_reviews SET status = 'APPROVED' WHERE status IS NULL");

        if (purchases > 0 || reviews > 0) {
            log.info("SchemaBackfillRunner: backfilled {} purchases.type and {} course_reviews.status", purchases, reviews);
        }
    }
}
