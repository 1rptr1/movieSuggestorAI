package com.yourorg.imdbloader.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_preferences")
public class UserPreferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String likedMovieId;

    public UserPreferenceEntity() {}

    public UserPreferenceEntity(String userId, String likedMovieId) {
        this.userId = userId;
        this.likedMovieId = likedMovieId;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLikedMovieId() {
        return likedMovieId;
    }
    
    public void setLikedMovieId(String likedMovieId) {
        this.likedMovieId = likedMovieId;
    }
}
