package com.yourorg.imdbloader.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_profiles")
public class UserProfileEntity {

    @Id
    private String userId;

    private String initialQuery;

    public UserProfileEntity() {}

    public UserProfileEntity(String userId, String initialQuery) {
        this.userId = userId;
        this.initialQuery = initialQuery;
    }

    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getInitialQuery() {
        return initialQuery;
    }
    
    public void setInitialQuery(String initialQuery) {
        this.initialQuery = initialQuery;
    }
}
