package com.innitsocial.abcdish.auth.repository;

import com.innitsocial.abcdish.auth.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    List<UserSession> findByUserIdAndActiveTrue(Long userId);
}