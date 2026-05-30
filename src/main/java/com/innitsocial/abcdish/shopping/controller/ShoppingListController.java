package com.innitsocial.abcdish.shopping.controller;

import com.innitsocial.abcdish.common.security.SecurityUtils;
import com.innitsocial.abcdish.shopping.dto.ShoppingListRequest;
import com.innitsocial.abcdish.shopping.dto.ShoppingListResponse;
import com.innitsocial.abcdish.shopping.service.ShoppingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shopping-list")
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListService service;

    @GetMapping
    public List<ShoppingListResponse> getItems() {
        return service.getItems(SecurityUtils.currentUserId());
    }

    @PostMapping
    public ShoppingListResponse addItem(@RequestBody ShoppingListRequest request) {
        return service.addItem(SecurityUtils.currentUserId(), request);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable Long itemId) {
        service.deleteItem(SecurityUtils.currentUserId(), itemId);
    }
}