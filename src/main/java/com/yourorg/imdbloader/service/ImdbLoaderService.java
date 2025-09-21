package com.yourorg.imdbloader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Service
public class ImdbLoaderService {

    private static final Logger log = LoggerFactory.getLogger(ImdbLoaderService.class);
    private static final int BATCH_SIZE = 1000;

    public void loadImdbData(Path dataDir, Connection connection) throws SQLException {
        log.info("Starting IMDB data loading from directory: {}", dataDir);
        
        try {
            // Load data in order of dependencies
            loadNameBasics(dataDir.resolve("name.basics.tsv"), connection);
            loadTitleBasics(dataDir.resolve("title.basics.tsv"), connection);
            loadTitlePrincipals(dataDir.resolve("title.principals.tsv"), connection);
            loadTitleAkas(dataDir.resolve("title.akas.tsv"), connection);
            
            log.info("✅ IMDB data loading completed successfully!");
        } catch (Exception e) {
            log.error("❌ Error loading IMDB data", e);
            throw new SQLException("Failed to load IMDB data", e);
        }
    }

    private void loadNameBasics(Path filePath, Connection conn) throws SQLException, IOException {
        if (!Files.exists(filePath)) {
            log.warn("⚠️ File not found: {}", filePath);
            return;
        }

        log.info("Loading name_basics from: {}", filePath);
        
        String sql = """
            INSERT INTO name_basics (nconst, primary_name, birth_year, death_year, primary_profession, known_for_titles)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (nconst) DO NOTHING
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             BufferedReader reader = Files.newBufferedReader(filePath)) {
            
            String line = reader.readLine(); // Skip header
            int count = 0;
            
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\t", -1);
                if (fields.length >= 6) {
                    stmt.setString(1, fields[0]); // nconst
                    stmt.setString(2, fields[1]); // primary_name
                    stmt.setObject(3, parseInteger(fields[2])); // birth_year
                    stmt.setObject(4, parseInteger(fields[3])); // death_year
                    stmt.setString(5, fields[4]); // primary_profession
                    stmt.setString(6, fields[5]); // known_for_titles
                    
                    stmt.addBatch();
                    count++;
                    
                    if (count % BATCH_SIZE == 0) {
                        stmt.executeBatch();
                        log.info("Processed {} name_basics records", count);
                    }
                }
            }
            
            stmt.executeBatch(); // Execute remaining batch
            log.info("✅ Loaded {} name_basics records", count);
        }
    }

    private void loadTitleBasics(Path filePath, Connection conn) throws SQLException, IOException {
        if (!Files.exists(filePath)) {
            log.warn("⚠️ File not found: {}", filePath);
            return;
        }

        log.info("Loading title_basics from: {}", filePath);
        
        String sql = """
            INSERT INTO title_basics (tconst, title_type, primary_title, original_title, is_adult, 
                                    start_year, end_year, runtime_minutes, genres)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (tconst) DO NOTHING
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             BufferedReader reader = Files.newBufferedReader(filePath)) {
            
            String line = reader.readLine(); // Skip header
            int count = 0;
            
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\t", -1);
                if (fields.length >= 9) {
                    stmt.setString(1, fields[0]); // tconst
                    stmt.setString(2, fields[1]); // title_type
                    stmt.setString(3, fields[2]); // primary_title
                    stmt.setString(4, fields[3]); // original_title
                    stmt.setBoolean(5, "1".equals(fields[4])); // is_adult
                    stmt.setObject(6, parseInteger(fields[5])); // start_year
                    stmt.setObject(7, parseInteger(fields[6])); // end_year
                    stmt.setObject(8, parseInteger(fields[7])); // runtime_minutes
                    stmt.setString(9, fields[8]); // genres
                    
                    stmt.addBatch();
                    count++;
                    
                    if (count % BATCH_SIZE == 0) {
                        stmt.executeBatch();
                        log.info("Processed {} title_basics records", count);
                    }
                }
            }
            
            stmt.executeBatch(); // Execute remaining batch
            log.info("✅ Loaded {} title_basics records", count);
        }
    }

    private void loadTitlePrincipals(Path filePath, Connection conn) throws SQLException, IOException {
        if (!Files.exists(filePath)) {
            log.warn("⚠️ File not found: {}", filePath);
            return;
        }

        log.info("Loading title_principals from: {}", filePath);
        
        String sql = """
            INSERT INTO title_principals (tconst, ordering, nconst, category, job, characters)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (tconst, ordering) DO NOTHING
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             BufferedReader reader = Files.newBufferedReader(filePath)) {
            
            String line = reader.readLine(); // Skip header
            int count = 0;
            
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\t", -1);
                if (fields.length >= 6) {
                    stmt.setString(1, fields[0]); // tconst
                    stmt.setInt(2, Integer.parseInt(fields[1])); // ordering
                    stmt.setString(3, fields[2]); // nconst
                    stmt.setString(4, fields[3]); // category
                    stmt.setString(5, fields[4]); // job
                    stmt.setString(6, fields[5]); // characters
                    
                    stmt.addBatch();
                    count++;
                    
                    if (count % BATCH_SIZE == 0) {
                        stmt.executeBatch();
                        log.info("Processed {} title_principals records", count);
                    }
                }
            }
            
            stmt.executeBatch(); // Execute remaining batch
            log.info("✅ Loaded {} title_principals records", count);
        }
    }

    private void loadTitleAkas(Path filePath, Connection conn) throws SQLException, IOException {
        if (!Files.exists(filePath)) {
            log.warn("⚠️ File not found: {}", filePath);
            return;
        }

        log.info("Loading title_akas from: {}", filePath);
        
        String sql = """
            INSERT INTO title_akas (title_id, ordering, title, region, language, types, attributes, is_original_title)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (title_id, ordering) DO NOTHING
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             BufferedReader reader = Files.newBufferedReader(filePath)) {
            
            String line = reader.readLine(); // Skip header
            int count = 0;
            
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\t", -1);
                if (fields.length >= 8) {
                    stmt.setString(1, fields[0]); // title_id
                    stmt.setInt(2, Integer.parseInt(fields[1])); // ordering
                    stmt.setString(3, fields[2]); // title
                    stmt.setString(4, fields[3]); // region
                    stmt.setString(5, fields[4]); // language
                    stmt.setString(6, fields[5]); // types
                    stmt.setString(7, fields[6]); // attributes
                    stmt.setBoolean(8, "1".equals(fields[7])); // is_original_title
                    
                    stmt.addBatch();
                    count++;
                    
                    if (count % BATCH_SIZE == 0) {
                        stmt.executeBatch();
                        log.info("Processed {} title_akas records", count);
                    }
                }
            }
            
            stmt.executeBatch(); // Execute remaining batch
            log.info("✅ Loaded {} title_akas records", count);
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty() || "\\N".equals(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
