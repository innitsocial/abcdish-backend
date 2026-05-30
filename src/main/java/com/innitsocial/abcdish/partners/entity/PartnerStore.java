package com.innitsocial.abcdish.partners.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "partner_stores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String storeName;

    private String postcode;

    private String websiteUrl;

    private boolean active;
}