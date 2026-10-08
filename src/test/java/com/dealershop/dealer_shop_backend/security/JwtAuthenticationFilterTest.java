package com.dealershop.dealer_shop_backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.dealershop.dealer_shop_backend.entity.Role;
import com.dealershop.dealer_shop_backend.entity.User;
import com.dealershop.dealer_shop_backend.repository.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    private JwtService jwtService;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        String secret = Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded());
        jwtService = new JwtService(secret, 15);
        filter = new JwtAuthenticationFilter(jwtService, userRepository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private User dealer(boolean active) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setEmail("ramesh@example.com");
        user.setRole(Role.DEALER);
        user.setActive(active);
        return user;
    }

    private MockHttpServletRequest requestWithAuthHeader(String value) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", value);
        return request;
    }

    @Test
    void validTokenAuthenticatesTheUser() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(dealer(true)));
        String token = jwtService.generateAccessToken(1L, "ramesh@example.com", Role.DEALER);

        filter.doFilter(requestWithAuthHeader("Bearer " + token), new MockHttpServletResponse(), filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        assertEquals(1L, principal.id());
        assertEquals(Role.DEALER, principal.role());
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void requestWithoutHeaderStaysUnauthenticated() throws Exception {
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void garbageTokenStaysUnauthenticated() throws Exception {
        filter.doFilter(requestWithAuthHeader("Bearer not-a-real-token"), new MockHttpServletResponse(), filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void inactiveUserIsNotAuthenticatedEvenWithValidToken() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(dealer(false)));
        String token = jwtService.generateAccessToken(1L, "ramesh@example.com", Role.DEALER);

        filter.doFilter(requestWithAuthHeader("Bearer " + token), new MockHttpServletResponse(), filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void tokenOfDeletedUserIsNotAuthenticated() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        String token = jwtService.generateAccessToken(1L, "ramesh@example.com", Role.DEALER);

        filter.doFilter(requestWithAuthHeader("Bearer " + token), new MockHttpServletResponse(), filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}