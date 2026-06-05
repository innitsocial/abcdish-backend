package com.innitsocial.abcdish.social.repository;

import com.innitsocial.abcdish.social.entity.CreatorFollow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreatorFollowRepository extends JpaRepository<CreatorFollow, Long> {

    boolean existsByCreatorKeyAndUserId(String creatorKey, Long userId);

    Optional<CreatorFollow> findByCreatorKeyAndUserId(String creatorKey, Long userId);
}
