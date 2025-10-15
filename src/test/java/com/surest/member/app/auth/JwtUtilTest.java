package com.surest.member.app.auth;

import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        Key testKey = Keys.hmacShaKeyFor("mysecretkeymysecretkeymysecretkey12".getBytes());

        ReflectionTestUtils.setField(jwtUtil, "key", testKey);
    }

    @Test
    void testGenerateAndParseToken_Success() {
        String username = "testuser";
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");

        String token = jwtUtil.generateToken(username, roles);
        String extractedUsername = jwtUtil.getUsernameFromToken(token);
        Set<String> extractedRoles = jwtUtil.getRoles(token);

        // Assert
        assertNotNull(token);
        assertEquals(username, extractedUsername);
        assertTrue(extractedRoles.contains("ROLE_USER"));
        assertTrue(extractedRoles.contains("ROLE_ADMIN"));
        assertEquals(2, extractedRoles.size());
    }

    @Test
    void testGetRoles_WhenRolesMissing_ReturnsEmptySet() {
        // Arrange
        String username = "nobody";
        String token = jwtUtil.generateToken(username, List.of()); // no roles

        // Act
        Set<String> roles = jwtUtil.getRoles(token);

        // Assert
        assertNotNull(roles);
        assertTrue(roles.isEmpty());
    }

    @Test
    void testGetUsernameFromToken_InvalidToken_ThrowsException() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtUtil.getUsernameFromToken(invalidToken));
    }

    @Test
    void testGetRoles_InvalidToken_ThrowsException() {
        // Arrange
        String invalidToken = "bad.token";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtUtil.getRoles(invalidToken));
    }
}

