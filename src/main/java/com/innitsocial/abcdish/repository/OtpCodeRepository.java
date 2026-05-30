package com.innitsocial.abcdish.repository;

import com.innitsocial.abcdish.entity.OtpCode;
import com.innitsocial.abcdish.entity.OtpPurpose;
import com.innitsocial.abcdish.entity.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findTopByDestinationAndOtpAndTypeAndPurposeAndUsedFalseOrderByCreatedAtDesc(
            String destination,
            String otp,
            OtpType type,
            OtpPurpose purpose
    );

    long countByDestinationAndTypeAndCreatedAtAfter(
            String destination,
            OtpType type,
            LocalDateTime createdAt
    );
}