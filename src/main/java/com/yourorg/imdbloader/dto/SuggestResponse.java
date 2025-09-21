package com.yourorg.imdbloader.dto;

import com.yourorg.imdbloader.model.Movie;
import java.util.List;

public class SuggestResponse {
    private String userId;
    private List<Movie> recommendations;

    public SuggestResponse(String userId, List<Movie> recommendations) {
        this.userId = userId;
        this.recommendations = recommendations;
    }

    public String getUserId() {
        return userId;
    }

    public List<Movie> getRecommendations() {
        return recommendations;
    }
}
