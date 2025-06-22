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

@Service
public class JwtService {

    @Value("${custom.access-key}")
    private String accessKey;

    @Value("${custom.refresh-key}")
    private String refreshKey;
    private long accessExpirationMs = 15 * 60 * 1000;
    private long refreshExpirationMs = 7 * 24 * 60 * 60 * 1000;

    public String generateAccessToken(Account account) {
        return Jwts.builder()
                .setSubject(account.getAccountId())
                .claim("username", account.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpirationMs))
                .signWith(getSigningKey(accessKey))
                .compact();
    }

    public String generateRefreshToken(Account account) {
        return Jwts.builder()
                .setSubject(account.getAccountId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(getSigningKey(refreshKey))
                .compact();
    }

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
