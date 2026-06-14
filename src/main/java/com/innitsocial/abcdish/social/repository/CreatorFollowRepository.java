package com.innitsocial.abcdish.social.repository;

import com.innitsocial.abcdish.social.entity.CreatorFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CreatorFollowRepository extends JpaRepository<CreatorFollow, Long> {

    boolean existsByCreatorKeyAndUserId(String creatorKey, Long userId);

    Optional<CreatorFollow> findByCreatorKeyAndUserId(String creatorKey, Long userId);

    @Query("select follow.creatorKey from CreatorFollow follow where follow.userId = :userId and follow.creatorKey in :creatorKeys")
    List<String> findFollowedCreatorKeys(@Param("userId") Long userId, @Param("creatorKeys") Collection<String> creatorKeys);

    void deleteByUserId(Long userId);
}
