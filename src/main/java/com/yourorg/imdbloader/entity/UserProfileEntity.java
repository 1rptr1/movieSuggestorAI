package com.yourorg.imdbloader.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "user_profiles")
public class UserProfileEntity {

    @Id
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    // Using custom JSONB converter for PostgreSQL
    @Convert(converter = JsonbConverter.class)
    @Column(name = "preferences", columnDefinition = "jsonb")
    private Map<String, Object> preferences;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public UserProfileEntity() {}

    public UserProfileEntity(String userId, Map<String, Object> preferences) {
        this.userId = userId;
        this.preferences = preferences;
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public String getUserId() { 
        return userId; 
    }
    
    public void setUserId(String userId) { 
        this.userId = userId; 
    }

    public Map<String, Object> getPreferences() { 
        return preferences; 
    }
    
    public void setPreferences(Map<String, Object> preferences) { 
        this.preferences = preferences; 
    }

    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }
}
