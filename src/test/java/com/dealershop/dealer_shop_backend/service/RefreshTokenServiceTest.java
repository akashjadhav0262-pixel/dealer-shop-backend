package com.dealershop.dealer_shop_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.dealershop.dealer_shop_backend.entity.RefreshToken;
import com.dealershop.dealer_shop_backend.entity.User;
import com.dealershop.dealer_shop_backend.exception.UnauthorizedException;
import com.dealershop.dealer_shop_backend.repository.RefreshTokenRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(refreshTokenRepository, 7);
    }

    private User user(boolean active) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setEmail("ramesh@example.com");
        user.setActive(active);
        return user;
    }

    private RefreshToken tokenFor(User user, boolean revoked, LocalDateTime expiresAt) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash("some-hash");
        token.setRevoked(revoked);
        token.setExpiresAt(expiresAt);
        return token;
    }

    @Test
    void createTokenSavesOnlyAHashOfTheToken() {
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);

        String rawToken = service.createToken(user(true));

        verify(refreshTokenRepository).save(captor.capture());
        assertNotEquals(rawToken, captor.getValue().getTokenHash());
        assertEquals(64, captor.getValue().getTokenHash().length());
    }

    @Test
    void rotateRevokesOldTokenAndIssuesNewOne() {
        RefreshToken old = tokenFor(user(true), false, LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByTokenHashWithUser(anyString())).thenReturn(Optional.of(old));

        RefreshTokenService.RotatedToken result = service.rotate("old-raw-token");

        assertTrue(old.isRevoked());
        assertNotNull(result.newRawToken());
        assertEquals("ramesh@example.com", result.user().getEmail());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void reusingARevokedTokenRevokesAllTokensOfTheUser() {
        RefreshToken used = tokenFor(user(true), true, LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByTokenHashWithUser(anyString())).thenReturn(Optional.of(used));

        assertThrows(UnauthorizedException.class, () -> service.rotate("old-raw-token"));

        verify(refreshTokenRepository).revokeAllForUser(1L);
    }

    @Test
    void expiredTokenIsRejected() {
        RefreshToken expired = tokenFor(user(true), false, LocalDateTime.now().minusMinutes(1));
        when(refreshTokenRepository.findByTokenHashWithUser(anyString())).thenReturn(Optional.of(expired));

        assertThrows(UnauthorizedException.class, () -> service.rotate("old-raw-token"));

        verify(refreshTokenRepository, never()).revokeAllForUser(anyLong());
    }

    @Test
    void unknownTokenIsRejected() {
        when(refreshTokenRepository.findByTokenHashWithUser(anyString())).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> service.rotate("made-up-token"));
    }

    @Test
    void tokenOfInactiveUserIsRejected() {
        RefreshToken token = tokenFor(user(false), false, LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByTokenHashWithUser(anyString())).thenReturn(Optional.of(token));

        assertThrows(UnauthorizedException.class, () -> service.rotate("old-raw-token"));
    }

    @Test
    void revokeMarksTheTokenAsRevoked() {
        RefreshToken token = tokenFor(user(true), false, LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByTokenHashWithUser(anyString())).thenReturn(Optional.of(token));

        service.revoke("raw-token");

        assertTrue(token.isRevoked());
    }
}