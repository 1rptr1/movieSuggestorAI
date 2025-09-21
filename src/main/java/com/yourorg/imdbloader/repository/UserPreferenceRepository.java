package com.yourorg.imdbloader.repository;

import com.yourorg.imdbloader.entity.UserPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPreferenceRepository extends JpaRepository<UserPreferenceEntity, Long> {
    List<UserPreferenceEntity> findByUserId(String userId);
}
