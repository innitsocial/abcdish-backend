package com.innitsocial.abcdish.stories.repository;

import com.innitsocial.abcdish.stories.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StoryRepository extends JpaRepository<Story, Long> {

    List<Story> findTop30ByExpiresAtAfterOrderByCreatedAtDesc(LocalDateTime now);
}
