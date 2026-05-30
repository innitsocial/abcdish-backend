package com.innitsocial.abcdish.repository;

import com.innitsocial.abcdish.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    List<UserSession> findByUserIdAndActiveTrue(Long userId);
}