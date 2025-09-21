package com.yourorg.imdbloader.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.util.Arrays;
import java.util.List;

@Component
public class DatabaseHealthChecker {

    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthChecker.class);
    
    private final JdbcTemplate jdbcTemplate;
    
    @Value("${imdb.data.auto-load:false}")
    private boolean autoLoadData;

    public DatabaseHealthChecker(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void checkAndSetupTables() {
        log.info("🔍 Checking database health...");
        
        List<String> requiredTables = Arrays.asList(
                "title_basics",
                "title_akas", 
                "title_principals",
                "name_basics",
                "user_profiles",
                "user_preferences"
        );

        for (String table : requiredTables) {
            if (!tableExists(table)) {
                if (table.equals("user_profiles") || table.equals("user_preferences")) {
                    log.info("⚠️ Table '{}' will be created by DatabaseInitializer", table);
                } else if (!autoLoadData) {
                    log.warn("⚠️ Missing IMDB table: {} (auto-load is disabled)", table);
                } else {
                    log.info("⚠️ Missing IMDB table: {} (will be created during data loading)", table);
                }
            } else {
                log.debug("✅ Table exists: {}", table);
            }
        }

        log.info("✅ Database health check completed");
        
        if (!autoLoadData) {
            log.info("💡 To load IMDB data automatically, set imdb.data.auto-load=true in application.properties");
        }
    }

    private boolean tableExists(String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables " +
                            "WHERE table_schema = 'public' AND table_name = ?",
                    Integer.class, tableName
            );
            return count != null && count > 0;
        } catch (Exception e) {
            log.debug("Error checking table existence for {}: {}", tableName, e.getMessage());
            return false;
        }
    }
}
