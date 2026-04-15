package com.contracthub.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import com.contracthub.service.SessionConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;
    
    private final SessionConfigService sessionConfigService;

    public JwtUtils(SessionConfigService sessionConfigService) {
        this.sessionConfigService = sessionConfigService;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            return Keys.hmacShaKeyFor(paddedKey);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + getEffectiveExpirationMillis(userId));
        
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            Object userId = claims.get("userId");
            if (userId instanceof Number) {
                return ((Number) userId).longValue();
            }
        }
        return null;
    }

    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("role") : null;
    }

    public boolean validateToken(String token) {
        Claims claims = parseToken(token);
        return claims != null;
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims == null) {
                return true;
            }
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    private Claims parseToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public String refreshToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        if (!canRefreshToken(token)) {
            return null;
        }
        
        Long userId = getUserIdFromToken(token);
        String username = getUsernameFromToken(token);
        String role = getRoleFromToken(token);
        
        return generateToken(userId, username, role);
    }

    public boolean canRefreshToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        Date issuedAt = claims.getIssuedAt();
        if (issuedAt == null) {
            return false;
        }
        Long userId = getUserIdFromToken(token);
        long refreshWindowMillis = getEffectiveRefreshExpirationMillis(userId);
        if (refreshWindowMillis <= 0) {
            return false;
        }
        long age = System.currentTimeMillis() - issuedAt.getTime();
        return age <= refreshWindowMillis;
    }

    private long getEffectiveExpirationMillis(Long userId) {
        try {
            int tokenExpiryHours = sessionConfigService.getTokenExpiry(userId);
            if (tokenExpiryHours > 0) {
                return tokenExpiryHours * 60L * 60L * 1000L;
            }
        } catch (Exception ignored) {
        }
        return expiration != null ? expiration : 24L * 60L * 60L * 1000L;
    }

    private long getEffectiveRefreshExpirationMillis(Long userId) {
        try {
            int refreshExpiryHours = sessionConfigService.getRefreshTokenExpiry(userId);
            if (refreshExpiryHours > 0) {
                return refreshExpiryHours * 60L * 60L * 1000L;
            }
        } catch (Exception ignored) {
        }
        return 168L * 60L * 60L * 1000L;
    }
}
