package com.yourorg.imdbloader.controller;

import com.yourorg.imdbloader.model.Movie;
import com.yourorg.imdbloader.service.MovieService;
import com.yourorg.imdbloader.service.SuggestService;
import com.yourorg.imdbloader.dto.*;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final MovieService movieService;
    private final SuggestService suggestService;

    public ApiController(MovieService movieService, SuggestService suggestService) {
        this.movieService = movieService;
        this.suggestService = suggestService;
    }

    // ---------------- EXISTING MOVIE ENDPOINTS ----------------

    @GetMapping("/movies")
    public List<Movie> getAllMovies() {
        return movieService.getAllMovies();
    }

    @GetMapping("/movies/{id}")
    public Movie getMovieById(@PathVariable String id) {
        return movieService.getMovieById(id);
    }

    @GetMapping("/movies/search")
    public List<Movie> searchMovies(@RequestParam String query) {
        return movieService.searchMovies(query);
    }

    // ---------------- NEW SUGGESTION ENDPOINTS ----------------

    @PostMapping("/suggest/start")
    public SuggestResponse start(@RequestBody StartRequest request) {
        return suggestService.startSession(request);
    }

    @PostMapping("/suggest/feedback")
    public SuggestResponse feedback(@RequestBody FeedbackRequest request) {
        return suggestService.recordFeedback(request.getUserId(), request.getLikedMovieIds());
    }

    @GetMapping("/suggest/{userId}")
    public SuggestResponse getSuggestions(@PathVariable String userId) {
        return suggestService.getRecommendations(userId);
    }
}
