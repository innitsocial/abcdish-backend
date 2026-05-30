package com.innitsocial.abcdish.feed.service;

import com.innitsocial.abcdish.content.repository.MealRepository;
import com.innitsocial.abcdish.feed.dto.FeedItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final MealRepository mealRepository;

    @Transactional(readOnly = true)
    public List<FeedItemResponse> getHomeFeed() {
        return mealRepository.findAll()
                .stream()
                .map(FeedItemResponse::fromMeal)
                .toList();
    }
}
