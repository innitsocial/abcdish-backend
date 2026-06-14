package com.innitsocial.abcdish.stories.repository;

import com.innitsocial.abcdish.stories.entity.StoryLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryLikeRepository extends JpaRepository<StoryLike, Long> {

    long countByStoryId(Long storyId);

    boolean existsByStoryIdAndUserId(Long storyId, Long userId);

    void deleteByStoryIdAndUserId(Long storyId, Long userId);

    void deleteByStoryId(Long storyId);
}
