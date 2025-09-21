package com.yourorg.imdbloader.dto;

import java.util.List;

public class FeedbackRequest {
    private String userId;
    private List<String> likedMovieIds;

    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getLikedMovieIds() {
        return likedMovieIds;
    }
    
    public void setLikedMovieIds(List<String> likedMovieIds) {
        this.likedMovieIds = likedMovieIds;
    }
}
