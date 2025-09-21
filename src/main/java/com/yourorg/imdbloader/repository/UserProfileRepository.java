package com.yourorg.imdbloader.repository;

import com.yourorg.imdbloader.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfileEntity, String> {
}
