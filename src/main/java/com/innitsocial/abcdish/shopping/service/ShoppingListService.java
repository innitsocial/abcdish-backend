package com.innitsocial.abcdish.shopping.service;

import com.innitsocial.abcdish.partners.repository.PartnerStoreRepository;
import com.innitsocial.abcdish.shopping.dto.ShoppingCheckoutOptionResponse;
import com.innitsocial.abcdish.shopping.dto.ShoppingListRequest;
import com.innitsocial.abcdish.shopping.dto.ShoppingListResponse;
import com.innitsocial.abcdish.shopping.entity.ShoppingListItem;
import com.innitsocial.abcdish.shopping.repository.ShoppingListItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final ShoppingListItemRepository repository;
    private final PartnerStoreRepository partnerStoreRepository;

    public List<ShoppingListResponse> getItems(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ShoppingListResponse addItem(Long userId, ShoppingListRequest request) {
        ShoppingListItem item = ShoppingListItem.builder()
                .userId(userId)
                .ingredientName(request.ingredientName())
                .quantity(request.quantity())
                .purchased(false)
                .build();

        return toResponse(repository.save(item));
    }

    public void deleteItem(Long userId, Long itemId) {
        ShoppingListItem item = repository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Shopping item not found"));

        if (!item.getUserId().equals(userId)) {
            throw new RuntimeException("You cannot delete this item");
        }

        repository.delete(item);
    }


    public List<ShoppingCheckoutOptionResponse> getCheckoutOptions(Long userId) {
        List<ShoppingListItem> items = repository.findByUserIdOrderByCreatedAtDesc(userId);

        return partnerStoreRepository.findByActiveTrue()
                .stream()
                .map(store -> new ShoppingCheckoutOptionResponse(
                        store.getId(),
                        store.getStoreName(),
                        store.getPostcode(),
                        store.getWebsiteUrl(),
                        items.size(),
                        "Partner checkout integration placeholder. Redirect user to partner store website for now."
                ))
                .toList();
    }

    private ShoppingListResponse toResponse(ShoppingListItem item) {
        return new ShoppingListResponse(
                item.getId(),
                item.getIngredientName(),
                item.getQuantity(),
                item.isPurchased()
        );
    }
}