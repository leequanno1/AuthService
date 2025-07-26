package com.project.q_authent.services.authent_services;
import com.project.q_authent.models.sqls.Account;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * Jwt service for handle jwt process
 * @since 1.00
 * @author leequanno1
 */
@Service
public class JwtService {

    // Server access key value
    @Value("${custom.access-key}")
    private String accessKey;

    // Server refresh key value
    @Value("${custom.refresh-key}")
    private String refreshKey;

    // Server access key expired min
    @Value("${custom.access-expired-minute}")
    private long accessExpirationMin;

    // Server refresh key expired day
    @Value("${custom.refresh-expired-day}")
    private long refreshExpirationDay;

    /**
     * Handle generate access token
     * @param account DB model
     * @return String access token
     * @since 1.00
     */
    public String generateAccessToken(Account account) {
        return Jwts.builder()
                .setSubject(account.getAccountId())
                .claim("username", account.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpirationMin * 60 * 1000))
                .signWith(getSigningKey(accessKey))
                .compact();
    }

    /**
     * Handle generate refresh token
     * @param account DB model
     * @return String refresh token
     * @since 1.00
     */
    public String generateRefreshToken(Account account) {
        return Jwts.builder()
                .setSubject(account.getAccountId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationDay * 24 * 60 * 60 * 1000))
                .signWith(getSigningKey(refreshKey))
                .compact();
    }

    /**
     * Extract token subject (userId)
     * @param token {@link String} token, can be access token or refresh token
     * @param isRefresh {@link Boolean} if token is refresh token then true, otherwise false
     * @return String subject (userid)
     * @since 1.00
     */
    public String extractAccountId(String token, boolean isRefresh) {
        return extractClaim(token, Claims::getSubject, isRefresh);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver, boolean isRefresh) {
        final Claims claims = extractAllClaims(token, isRefresh);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token, boolean isRefresh) {
        return Jwts.parserBuilder()
                .setSigningKey(isRefresh ? getSigningKey(refreshKey) : getSigningKey(accessKey))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token, Account account, boolean isRefresh) {
        final String accountId = extractAccountId(token, isRefresh);
        return accountId.equals(account.getAccountId()) && !isTokenExpired(token, isRefresh);
    }

    private boolean isTokenExpired(String token, boolean isRefresh) {
        return extractClaim(token, Claims::getExpiration, isRefresh).before(new Date());
    }

    private Key getSigningKey(String keyString) {
        byte[] keyBytes = Decoders.BASE64.decode(keyString);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
