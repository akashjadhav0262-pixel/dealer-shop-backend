package com.dealershop.dealer_shop_backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dealershop.dealer_shop_backend.entity.RefreshToken;
import com.dealershop.dealer_shop_backend.entity.User;
import com.dealershop.dealer_shop_backend.exception.UnauthorizedException;
import com.dealershop.dealer_shop_backend.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

    private static final String INVALID_MESSAGE = "Invalid or expired refresh token";
    private static final SecureRandom RANDOM = new SecureRandom();

    public record RotatedToken(User user, String newRawToken) {
    }

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenDays;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               @Value("${app.jwt.refresh-token-days}") long refreshTokenDays) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenDays = refreshTokenDays;
    }

    // Returns the raw token. Only its hash is saved, so this is the only moment the raw value exists.
    @Transactional
    public String createToken(User user) {
        String rawToken = generateRawToken();

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(hash(rawToken));
        token.setExpiresAt(LocalDateTime.now().plusDays(refreshTokenDays));
        refreshTokenRepository.save(token);

        return rawToken;
    }

    // noRollbackFor: when we detect reuse we revoke all tokens and THEN throw.
    // Without this, the exception would roll back the revoke as well.
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public RotatedToken rotate(String rawToken) {
        RefreshToken existing = refreshTokenRepository.findByTokenHashWithUser(hash(rawToken))
                .orElseThrow(() -> new UnauthorizedException(INVALID_MESSAGE));

        if (existing.isRevoked()) {
            // An already-used token came back: possible theft. Log out every session of this user.
            refreshTokenRepository.revokeAllForUser(existing.getUser().getId());
            throw new UnauthorizedException(INVALID_MESSAGE);
        }

        if (existing.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException(INVALID_MESSAGE);
        }

        User user = existing.getUser();
        if (!user.isActive()) {
            throw new UnauthorizedException(INVALID_MESSAGE);
        }

        existing.setRevoked(true);
        String newRawToken = createToken(user);
        return new RotatedToken(user, newRawToken);
    }

    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHashWithUser(hash(rawToken))
                .ifPresent(token -> token.setRevoked(true));
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}