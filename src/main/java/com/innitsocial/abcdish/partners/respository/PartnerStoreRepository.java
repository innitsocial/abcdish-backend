package com.innitsocial.abcdish.partners.repository;

import com.innitsocial.abcdish.partners.entity.PartnerStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartnerStoreRepository extends JpaRepository<PartnerStore, Long> {

    List<PartnerStore> findByActiveTrue();
}