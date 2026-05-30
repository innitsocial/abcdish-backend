package com.innitsocial.abcdish.partners.service;

import com.innitsocial.abcdish.partners.dto.PartnerStoreResponse;
import com.innitsocial.abcdish.partners.repository.PartnerStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartnerStoreService {

    private final PartnerStoreRepository repository;

    public List<PartnerStoreResponse> getActiveStores() {
        return repository.findByActiveTrue()
                .stream()
                .map(store -> new PartnerStoreResponse(
                        store.getId(),
                        store.getStoreName(),
                        store.getPostcode(),
                        store.getWebsiteUrl()
                ))
                .toList();
    }
}