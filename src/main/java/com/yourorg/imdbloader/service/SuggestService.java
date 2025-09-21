package com.yourorg.imdbloader.service;

import com.yourorg.imdbloader.dto.*;
import com.yourorg.imdbloader.entity.UserPreferenceEntity;
import com.yourorg.imdbloader.entity.UserProfileEntity;
import com.yourorg.imdbloader.model.Movie;
import com.yourorg.imdbloader.repository.UserPreferenceRepository;
import com.yourorg.imdbloader.repository.UserProfileRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SuggestService {

    private final UserProfileRepository profileRepo;
    private final UserPreferenceRepository prefRepo;
    private final MovieService movieService;
    private final JdbcTemplate jdbcTemplate;

    public SuggestService(UserProfileRepository profileRepo,
                          UserPreferenceRepository prefRepo,
                          MovieService movieService,
                          JdbcTemplate jdbcTemplate) {
        this.profileRepo = profileRepo;
        this.prefRepo = prefRepo;
        this.movieService = movieService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public SuggestResponse startSession(StartRequest request) {
        String userId = UUID.randomUUID().toString();
        
        // Create initial preferences map with the query
        Map<String, Object> initialPrefs = new HashMap<>();
        initialPrefs.put("initialQuery", request.getQuery());
        initialPrefs.put("preferredGenres", new ArrayList<>());
        initialPrefs.put("preferredActors", new ArrayList<>());
        
        // Save user profile with preferences using JdbcTemplate with JSONB casting
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String prefsJson = mapper.writeValueAsString(initialPrefs);
            
            // First ensure the table exists
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS user_profiles (
                    user_id VARCHAR(50) PRIMARY KEY,
                    preferences JSONB DEFAULT '{}'::jsonb,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
            
            // Insert with JSONB casting
            jdbcTemplate.update(
                "INSERT INTO user_profiles (user_id, preferences, created_at) VALUES (?, ?::jsonb, ?) " +
                "ON CONFLICT (user_id) DO UPDATE SET preferences = ?::jsonb, created_at = ?",
                userId, prefsJson, java.time.LocalDateTime.now(), prefsJson, java.time.LocalDateTime.now()
            );
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Error converting preferences to JSON", e);
        }

        // Get initial recommendations from query
        List<Movie> recommendations = movieService.searchMovies(request.getQuery());

        return new SuggestResponse(userId, recommendations);
    }

    public SuggestResponse recordFeedback(String userId, List<String> likedMovieIds) {
        for (String movieId : likedMovieIds) {
            prefRepo.save(new UserPreferenceEntity(userId, movieId));
        }
        return getRecommendations(userId);
    }

    public SuggestResponse getRecommendations(String userId) {
        List<UserPreferenceEntity> prefs = prefRepo.findByUserId(userId);

        if (prefs.isEmpty()) {
            // If no preferences yet, get user's initial query from preferences
            Optional<UserProfileEntity> profile = profileRepo.findById(userId);
            if (profile.isPresent() && profile.get().getPreferences() != null) {
                String initialQuery = (String) profile.get().getPreferences().get("initialQuery");
                if (initialQuery != null) {
                    List<Movie> recommendations = movieService.searchMovies(initialQuery);
                    return new SuggestResponse(userId, recommendations);
                }
            }
            // Fallback to popular movies
            return new SuggestResponse(userId, movieService.getAllMovies().subList(0, Math.min(10, movieService.getAllMovies().size())));
        }

        // Enhanced recommender: analyze liked movies and suggest similar ones
        List<Movie> recommendations = new ArrayList<>();
        Set<String> likedGenres = new HashSet<>();
        Set<String> likedActors = new HashSet<>();
        Set<String> alreadyLikedIds = new HashSet<>();

        // Analyze user preferences
        for (UserPreferenceEntity pref : prefs) {
            Movie liked = movieService.getMovieById(pref.getLikedMovieId());
            if (liked != null) {
                alreadyLikedIds.add(liked.getId());
                likedGenres.addAll(liked.getGenres());
                likedActors.addAll(liked.getActors());
            }
        }

        // Get all movies and score them based on similarity
        List<Movie> allMovies = movieService.getAllMovies();
        List<Movie> scoredMovies = allMovies.stream()
                .filter(movie -> !alreadyLikedIds.contains(movie.getId())) // Exclude already liked movies
                .map(movie -> {
                    double score = calculateSimilarityScore(movie, likedGenres, likedActors);
                    movie.setScore(score);
                    return movie;
                })
                .sorted((m1, m2) -> Double.compare(m2.getScore(), m1.getScore())) // Sort by score descending
                .limit(10)
                .collect(Collectors.toList());

        return new SuggestResponse(userId, scoredMovies);
    }

    private double calculateSimilarityScore(Movie movie, Set<String> likedGenres, Set<String> likedActors) {
        double score = 0.0;

        // Genre similarity (weight: 0.4)
        long genreMatches = movie.getGenres().stream()
                .mapToLong(genre -> likedGenres.contains(genre) ? 1 : 0)
                .sum();
        score += (genreMatches / (double) Math.max(movie.getGenres().size(), 1)) * 0.4;

        // Actor similarity (weight: 0.3)
        long actorMatches = movie.getActors().stream()
                .mapToLong(actor -> likedActors.contains(actor) ? 1 : 0)
                .sum();
        score += (actorMatches / (double) Math.max(movie.getActors().size(), 1)) * 0.3;

        // Rating boost (weight: 0.3)
        score += (movie.getRating() / 10.0) * 0.3;

        return score;
    }

    // ============ USAGE EXAMPLE METHODS FOR JSONB PREFERENCES ============
    
    /**
     * Example method showing how to add or update user preferences using JSONB
     */
    public void addOrUpdateUserPreferences(String userId, Map<String, Object> newPreferences) {
        Optional<UserProfileEntity> existingProfile = profileRepo.findById(userId);
        
        if (existingProfile.isPresent()) {
            UserProfileEntity profile = existingProfile.get();
            Map<String, Object> currentPrefs = profile.getPreferences();
            if (currentPrefs == null) {
                currentPrefs = new HashMap<>();
            }
            
            // Merge new preferences with existing ones
            currentPrefs.putAll(newPreferences);
            profile.setPreferences(currentPrefs);
            profileRepo.save(profile);
        } else {
            // Create new profile with preferences
            UserProfileEntity newProfile = new UserProfileEntity(userId, newPreferences);
            profileRepo.save(newProfile);
        }
    }
    
    /**
     * Example method showing how to update specific preference fields
     */
    public void updateUserGenrePreferences(String userId, List<String> preferredGenres) {
        Optional<UserProfileEntity> profile = profileRepo.findById(userId);
        if (profile.isPresent()) {
            Map<String, Object> prefs = profile.get().getPreferences();
            if (prefs == null) {
                prefs = new HashMap<>();
            }
            prefs.put("preferredGenres", preferredGenres);
            profile.get().setPreferences(prefs);
            profileRepo.save(profile.get());
        }
    }
    
    /**
     * Example method showing how to retrieve specific preferences
     */
    @SuppressWarnings("unchecked")
    public List<String> getUserGenrePreferences(String userId) {
        Optional<UserProfileEntity> profile = profileRepo.findById(userId);
        if (profile.isPresent() && profile.get().getPreferences() != null) {
            Object genres = profile.get().getPreferences().get("preferredGenres");
            if (genres instanceof List) {
                return (List<String>) genres;
            }
        }
        return new ArrayList<>();
    }
}
