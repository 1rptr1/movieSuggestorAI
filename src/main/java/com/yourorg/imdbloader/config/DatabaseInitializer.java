package com.yourorg.imdbloader.config;

import com.yourorg.imdbloader.service.ImdbLoaderService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class DatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final ImdbLoaderService imdbLoaderService;

    public DatabaseInitializer(ImdbLoaderService imdbLoaderService) {
        this.imdbLoaderService = imdbLoaderService;
    }

    public void init(Path imdbDataDir) throws SQLException {
        log.info("🚀 Initializing database with IMDB data from: {}", imdbDataDir);
        
        Connection conn = entityManager.unwrap(Connection.class);

        // 1. Create all required tables
        createTables(conn);

        // 2. Check if data already exists
        if (isDataAlreadyLoaded(conn)) {
            log.info("✅ IMDB data already exists in database, skipping data loading");
            return;
        }

        // 3. Load IMDB data
        imdbLoaderService.loadImdbData(imdbDataDir, conn);
        
        log.info("🎉 Database initialization completed successfully!");
    }

    private void createTables(Connection conn) throws SQLException {
        log.info("📋 Creating database tables...");
        
        try (Statement stmt = conn.createStatement()) {
            
            // Create user_profiles table (for movie suggestor)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_profiles (
                    user_id VARCHAR(50) PRIMARY KEY,
                    preferences JSONB DEFAULT '{}'::jsonb,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
            
            // Create user_preferences table (for tracking liked movies)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS user_preferences (
                    id BIGSERIAL PRIMARY KEY,
                    user_id VARCHAR(50) NOT NULL,
                    liked_movie_id VARCHAR(20) NOT NULL
                )
            """);

            // Create name_basics table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS name_basics (
                    nconst VARCHAR(20) PRIMARY KEY,
                    primary_name TEXT,
                    birth_year INTEGER,
                    death_year INTEGER,
                    primary_profession TEXT,
                    known_for_titles TEXT
                )
            """);

            // Create title_basics table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS title_basics (
                    tconst VARCHAR(20) PRIMARY KEY,
                    title_type VARCHAR(50),
                    primary_title TEXT,
                    original_title TEXT,
                    is_adult BOOLEAN,
                    start_year INTEGER,
                    end_year INTEGER,
                    runtime_minutes INTEGER,
                    genres TEXT
                )
            """);

            // Create title_principals table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS title_principals (
                    tconst VARCHAR(20),
                    ordering INTEGER,
                    nconst VARCHAR(20),
                    category VARCHAR(50),
                    job TEXT,
                    characters TEXT,
                    PRIMARY KEY (tconst, ordering),
                    FOREIGN KEY (tconst) REFERENCES title_basics(tconst),
                    FOREIGN KEY (nconst) REFERENCES name_basics(nconst)
                )
            """);

            // Create title_akas table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS title_akas (
                    title_id VARCHAR(20),
                    ordering INTEGER,
                    title TEXT,
                    region VARCHAR(10),
                    language VARCHAR(10),
                    types TEXT,
                    attributes TEXT,
                    is_original_title BOOLEAN,
                    PRIMARY KEY (title_id, ordering),
                    FOREIGN KEY (title_id) REFERENCES title_basics(tconst)
                )
            """);

            // Create useful indexes for performance
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_title_basics_type ON title_basics(title_type)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_title_basics_year ON title_basics(start_year)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_title_basics_title ON title_basics(primary_title)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_title_principals_tconst ON title_principals(tconst)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_title_principals_nconst ON title_principals(nconst)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_title_principals_category ON title_principals(category)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_name_basics_name ON name_basics(primary_name)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_user_preferences_user_id ON user_preferences(user_id)");

            log.info("✅ All database tables created successfully");
        }
    }

    private boolean isDataAlreadyLoaded(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM title_basics LIMIT 1");
            if (rs.next()) {
                int count = rs.getInt(1);
                log.info("Found {} records in title_basics table", count);
                return count > 0;
            }
        } catch (SQLException e) {
            log.warn("Could not check existing data, assuming empty database: {}", e.getMessage());
        }
        return false;
    }
}
