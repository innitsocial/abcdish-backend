package com.innitsocial.abcdish.shopping.repository;

import com.innitsocial.abcdish.shopping.entity.ShoppingListItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {

    List<ShoppingListItem> findByUserIdOrderByCreatedAtDesc(Long userId);
}