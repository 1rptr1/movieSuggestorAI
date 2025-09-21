package com.yourorg.imdbloader.service;

import com.yourorg.imdbloader.dto.*;
import com.yourorg.imdbloader.entity.UserPreferenceEntity;
import com.yourorg.imdbloader.entity.UserProfileEntity;
import com.yourorg.imdbloader.model.Movie;
import com.yourorg.imdbloader.repository.UserPreferenceRepository;
import com.yourorg.imdbloader.repository.UserProfileRepository;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SuggestService {

    private final UserProfileRepository profileRepo;
    private final UserPreferenceRepository prefRepo;
    private final MovieService movieService;

    public SuggestService(UserProfileRepository profileRepo,
                          UserPreferenceRepository prefRepo,
                          MovieService movieService) {
        this.profileRepo = profileRepo;
        this.prefRepo = prefRepo;
        this.movieService = movieService;
    }

    public SuggestResponse startSession(StartRequest request) {
        String userId = UUID.randomUUID().toString();
        profileRepo.save(new UserProfileEntity(userId, request.getQuery()));

        // initial recommendations from query
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
            // If no preferences yet, get user's initial query
            Optional<UserProfileEntity> profile = profileRepo.findById(userId);
            if (profile.isPresent()) {
                List<Movie> recommendations = movieService.searchMovies(profile.get().getInitialQuery());
                return new SuggestResponse(userId, recommendations);
            } else {
                // Fallback to popular movies
                return new SuggestResponse(userId, movieService.getAllMovies().subList(0, Math.min(10, movieService.getAllMovies().size())));
            }
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
}
