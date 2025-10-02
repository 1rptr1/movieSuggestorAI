package com.yourorg.imdbloader.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class StartupRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);

    private final DatabaseInitializer dbInitializer;
    
    @Value("${imdb.data.directory:#{null}}")
    private String imdbDataDirectory;
    
    @Value("${imdb.data.auto-load:false}")
    private boolean autoLoadData;

    public StartupRunner(DatabaseInitializer dbInitializer) {
        this.dbInitializer = dbInitializer;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 Starting IMDB Loader Application...");
        
        if (!autoLoadData) {
            log.info("⏭️ Auto-load is disabled (imdb.data.auto-load=false). Skipping data loading.");
            log.info("💡 To enable auto-loading, set imdb.data.auto-load=true and imdb.data.directory in application.properties");
            return;
        }
        
        if (imdbDataDirectory == null || imdbDataDirectory.trim().isEmpty()) {
            log.warn("⚠️ IMDB data directory not configured. Please set 'imdb.data.directory' in application.properties");
            log.info("💡 Example: imdb.data.directory=C:/path/to/imdb/data");
            return;
        }

        Path imdbDir = Paths.get(imdbDataDirectory);
        
        if (!Files.exists(imdbDir)) {
            log.error("❌ IMDB data directory does not exist: {}", imdbDir);
            log.info("💡 Please ensure the directory exists and contains IMDB TSV files:");
            log.info("   - name.basics.tsv");
            log.info("   - title.basics.tsv");
            log.info("   - title.principals.tsv");
            log.info("   - title.akas.tsv");
            log.info("   - title.crew.tsv");
            return;
        }

        if (!Files.isDirectory(imdbDir)) {
            log.error("❌ IMDB data path is not a directory: {}", imdbDir);
            return;
        }

        log.info("📁 Using IMDB data directory: {}", imdbDir);
        
        // Check for required files
        String[] requiredFiles = {"name.basics.tsv", "title.basics.tsv", "title.principals.tsv", "title.akas.tsv", "title.crew.tsv"};
        boolean allFilesExist = true;
        
        for (String fileName : requiredFiles) {
            Path filePath = imdbDir.resolve(fileName);
            if (!Files.exists(filePath)) {
                log.warn("⚠️ Missing required file: {}", filePath);
                allFilesExist = false;
            } else {
                log.info("✅ Found: {}", fileName);
            }
        }
        
        if (!allFilesExist) {
            log.error("❌ Some required IMDB files are missing. Please ensure all TSV files are present.");
            return;
        }

        try {
            dbInitializer.init(imdbDir);
            log.info("🎉 IMDB data loaded successfully!");
            log.info("🔗 API endpoints available at:");
            log.info("   - GET  /api/movies - Get all movies");
            log.info("   - GET  /api/movies/{id} - Get movie by ID");
            log.info("   - GET  /api/movies/search?query=... - Search movies");
            log.info("   - POST /api/suggest/start - Start suggestion session");
            log.info("   - POST /api/suggest/feedback - Provide feedback");
            log.info("   - GET  /api/suggest/{userId} - Get recommendations");
        } catch (Exception e) {
            log.error("❌ Failed to initialize database with IMDB data", e);
            throw e;
        }
    }
}
