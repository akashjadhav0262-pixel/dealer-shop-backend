package com.dealershop.dealer_shop_backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.dealershop.dealer_shop_backend.entity.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;

class JwtServiceTest {

    // A random key made fresh for the tests, so no real secret is ever written in code
    private static String randomSecret() {
        return Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded());
    }

    @Test
    void generatedTokenCanBeParsed() {
        JwtService service = new JwtService(randomSecret(), 15);

        String token = service.generateAccessToken(5L, "ramesh@example.com", Role.DEALER);
        Claims claims = service.parseToken(token);

        assertEquals("5", claims.getSubject());
        assertEquals("ramesh@example.com", claims.get("email", String.class));
        assertEquals("DEALER", claims.get("role", String.class));
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService service = new JwtService(randomSecret(), -1);

        String token = service.generateAccessToken(5L, "ramesh@example.com", Role.DEALER);

        assertThrows(ExpiredJwtException.class, () -> service.parseToken(token));
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtService service = new JwtService(randomSecret(), 15);
        String token = service.generateAccessToken(5L, "ramesh@example.com", Role.DEALER);

        int position = token.lastIndexOf('.') + 5;
        char original = token.charAt(position);
        char replacement = original == 'A' ? 'B' : 'A';
        String tampered = token.substring(0, position) + replacement + token.substring(position + 1);

        assertThrows(JwtException.class, () -> service.parseToken(tampered));
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtService creator = new JwtService(randomSecret(), 15);
        JwtService checker = new JwtService(randomSecret(), 15);

        String token = creator.generateAccessToken(5L, "ramesh@example.com", Role.DEALER);

        assertThrows(JwtException.class, () -> checker.parseToken(token));
    }
}