package com.dealershop.dealer_shop_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dealershop.dealer_shop_backend.dto.LoginRequest;
import com.dealershop.dealer_shop_backend.dto.LoginResponse;
import com.dealershop.dealer_shop_backend.entity.Role;
import com.dealershop.dealer_shop_backend.entity.User;
import com.dealershop.dealer_shop_backend.exception.UnauthorizedException;
import com.dealershop.dealer_shop_backend.mapper.UserMapper;
import com.dealershop.dealer_shop_backend.repository.UserRepository;
import com.dealershop.dealer_shop_backend.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(anyString())).thenReturn("dummy-hash");
        authService = new AuthService(userRepository, passwordEncoder, new UserMapper(), jwtService, refreshTokenService);
    }

    private User dealer() {
        User user = new User();
        user.setName("Ramesh Kumar");
        user.setEmail("ramesh@example.com");
        user.setPasswordHash("stored-hash");
        user.setRole(Role.DEALER);
        return user;
    }

    @Test
    void loginSucceedsWithCorrectPassword() {
        when(userRepository.findByEmail("ramesh@example.com")).thenReturn(Optional.of(dealer()));
        when(passwordEncoder.matches("Secret@123", "stored-hash")).thenReturn(true);
        when(jwtService.generateAccessToken(any(), eq("ramesh@example.com"), eq(Role.DEALER)))
                .thenReturn("fake-token");
        when(jwtService.getAccessTokenSeconds()).thenReturn(900L);
        when(refreshTokenService.createToken(any())).thenReturn("fake-refresh-token");

        // spaces and capital letters in the email should not matter
        LoginResponse response = authService.login(new LoginRequest("  Ramesh@Example.com ", "Secret@123"));

        assertEquals("fake-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(900L, response.expiresInSeconds());
        assertEquals("ramesh@example.com", response.user().email());
        assertEquals("fake-refresh-token", response.refreshToken());
    }

    @Test
    void loginFailsWithWrongPassword() {
        when(userRepository.findByEmail("ramesh@example.com")).thenReturn(Optional.of(dealer()));
        when(passwordEncoder.matches("WrongPass1", "stored-hash")).thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> authService.login(new LoginRequest("ramesh@example.com", "WrongPass1")));

        verify(jwtService, never()).generateAccessToken(any(), any(), any());
    }

    @Test
    void loginFailsForUnknownEmail() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class,
                () -> authService.login(new LoginRequest("nobody@example.com", "Secret@123")));

        verify(jwtService, never()).generateAccessToken(any(), any(), any());
    }

    @Test
    void loginFailsForInactiveUserEvenWithCorrectPassword() {
        User inactive = dealer();
        inactive.setActive(false);
        when(userRepository.findByEmail("ramesh@example.com")).thenReturn(Optional.of(inactive));
        when(passwordEncoder.matches("Secret@123", "stored-hash")).thenReturn(true);

        assertThrows(UnauthorizedException.class,
                () -> authService.login(new LoginRequest("ramesh@example.com", "Secret@123")));

        verify(jwtService, never()).generateAccessToken(any(), any(), any());
    }
}