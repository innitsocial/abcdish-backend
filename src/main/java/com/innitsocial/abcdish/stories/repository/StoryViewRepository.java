package com.innitsocial.abcdish.stories.repository;

import com.innitsocial.abcdish.stories.entity.StoryView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryViewRepository extends JpaRepository<StoryView, Long> {

    long countByStoryId(Long storyId);

    boolean existsByStoryIdAndUserId(Long storyId, Long userId);

    List<StoryView> findByStoryIdOrderByViewedAtDesc(Long storyId);

    void deleteByStoryId(Long storyId);
}
