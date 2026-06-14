package com.innitsocial.abcdish.content.service;

import com.innitsocial.abcdish.content.repository.MealRepository;
import com.innitsocial.abcdish.moderation.ModerationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Year;

@Service
@RequiredArgsConstructor
public class CatalogCapacityService {

    private final MealRepository mealRepository;

    @Value("${app.catalog.base-capacity:10000}")
    private long baseCapacity;

    @Value("${app.catalog.annual-capacity-increment:1000}")
    private long annualCapacityIncrement;

    @Value("${app.catalog.base-year:2026}")
    private int baseYear;

    public void ensureCapacityAvailable() {
        long currentAcceptedRecipes = mealRepository.countByModerationStatus(ModerationStatus.APPROVED);
        long limit = currentLimit();
        if (currentAcceptedRecipes >= limit) {
            throw new RuntimeException("ABCDish catalog is full for this year. Current limit is "
                    + limit + " accepted recipes.");
        }
    }

    public long currentLimit() {
        int currentYear = Year.now().getValue();
        long completedYears = Math.max(0, currentYear - baseYear);
        return baseCapacity + (completedYears * annualCapacityIncrement);
    }
}
