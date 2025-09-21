package com.yourorg.imdbloader.repository;

import com.yourorg.imdbloader.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, String> {
    
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_profiles (user_id, preferences, created_at) VALUES (:userId, :preferences::jsonb, :createdAt) " +
                   "ON CONFLICT (user_id) DO UPDATE SET preferences = :preferences::jsonb, created_at = :createdAt", 
           nativeQuery = true)
    void saveWithJsonbCast(@Param("userId") String userId, 
                          @Param("preferences") String preferences, 
                          @Param("createdAt") java.time.LocalDateTime createdAt);
}
