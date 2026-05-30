package com.innitsocial.abcdish.partners.controller;

import com.innitsocial.abcdish.partners.dto.PartnerStoreResponse;
import com.innitsocial.abcdish.partners.service.PartnerStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partners/stores")
@RequiredArgsConstructor
public class PartnerStoreController {

    private final PartnerStoreService service;

    @GetMapping
    public List<PartnerStoreResponse> getStores() {
        return service.getActiveStores();
    }
}