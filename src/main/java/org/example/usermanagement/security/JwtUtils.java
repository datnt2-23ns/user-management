package org.example.usermanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {

        private final SecretKey signingKey;
        private final long expirationMs;

        public JwtUtils(
                        @Value("${app.jwt.secret}") String jwtSecret,
                        @Value("${app.jwt.expiration-ms}") long expirationMs) {
                if (jwtSecret == null || jwtSecret.isBlank()) {
                        throw new IllegalArgumentException(
                                        "JWT_SECRET không được để trống");
                }

                if (expirationMs <= 0) {
                        throw new IllegalArgumentException(
                                        "JWT_EXPIRATION_MS phải lớn hơn 0");
                }

                byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);

                this.signingKey = Keys.hmacShaKeyFor(keyBytes);

                this.expirationMs = expirationMs;
        }

        public String generateToken(UserDetails userDetails) {
                Instant issuedAt = Instant.now();

                Instant expiresAt = issuedAt.plusMillis(expirationMs);

                List<String> authorities = userDetails.getAuthorities()
                                .stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList();

                return Jwts.builder()
                                .subject(userDetails.getUsername())
                                .claim("authorities", authorities)
                                .issuedAt(Date.from(issuedAt))
                                .expiration(Date.from(expiresAt))
                                .signWith(signingKey)
                                .compact();
        }

        public String extractUsername(String token) {
                return extractAllClaims(token).getSubject();
        }

        public boolean isTokenValid(
                        String token,
                        UserDetails userDetails) {
                Claims claims = extractAllClaims(token);

                String username = claims.getSubject();
                Date expiration = claims.getExpiration();

                return username != null
                                && username.equalsIgnoreCase(
                                                userDetails.getUsername())
                                && expiration != null
                                && expiration.after(new Date());
        }

        public long getExpirationSeconds() {
                return expirationMs / 1000;
        }

        private Claims extractAllClaims(String token) {
                return Jwts.parser()
                                .verifyWith(signingKey)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload();
        }
}