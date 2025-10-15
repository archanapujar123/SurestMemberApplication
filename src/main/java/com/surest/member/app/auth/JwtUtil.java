package com.surest.member.app.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class JwtUtil {
    private final SecretKey key = Keys.hmacShaKeyFor(
            "ThisIsASecretKeyForJwtSigningChangeItToSomethingSecure1234567890".getBytes()
    );

    // Generate token with username + roles
    public String generateToken(String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    @SuppressWarnings("unchecked")
    public Set<String> getRoles(String token) {
        Object rolesObj = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("roles");

        if (rolesObj instanceof List<?>) {
            List<?> rolesList = (List<?>) rolesObj;
            // Ensure all elements are strings
            List<String> stringRoles = rolesList.stream()
                    .map(Object::toString)
                    .toList();
            return Set.copyOf(stringRoles);
        }

        // If roles claim is missing or not a list, return empty set
        return Collections.emptySet();

    }

}