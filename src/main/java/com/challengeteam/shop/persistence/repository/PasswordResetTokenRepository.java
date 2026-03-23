package com.challengeteam.shop.persistence.repository;

import com.challengeteam.shop.entity.token.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    List<PasswordResetToken> findAllByUserId(Long userId);

    Optional<PasswordResetToken> findByToken(String token);

}
