package com.project.q_authent.services.authify_services;

import com.project.q_authent.models.nosqls.User;
import com.project.q_authent.utils.JsonUtils;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
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
public class AuthifyJwtService {

    /**
     * Handle generate access token
     * @param user {@link User}
     * @param accessKey {@link String}
     * @param accessExpirationMin {@link Long}
     * @return {@link String} access token
     */
    public String generateAccessToken(User user, String accessKey, long accessExpirationMin) {
        return Jwts.builder()
                .setSubject(JsonUtils.toJson(user))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpirationMin * 60 * 1000))
                .signWith(getSigningKey(accessKey))
                .compact();
    }

    /**
     * Handle generate refresh token
     * @param user {@link User}
     * @param refreshKey {@link String}
     * @param refreshExpirationDay {@link Long}
     * @return refresh key
     */
    public String generateRefreshToken(User user, String refreshKey, long refreshExpirationDay) {
        return Jwts.builder()
                .setSubject(JsonUtils.toJson(user))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationDay * 24 * 60 * 60 * 1000))
                .signWith(getSigningKey(refreshKey))
                .compact();
    }

    /**
     * extractUser
     * @param token {@link String}
     * @param accessKey {@link String}
     * @return User object
     */
    public User extractUser(String token, String accessKey) {
        String userJson = extractClaim(token, Claims::getSubject, accessKey);
        return JsonUtils.fromJson(userJson);
    }


    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver, String signingKey) {
        final Claims claims = extractAllClaims(token, signingKey);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token, String signingKey) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey(signingKey))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token, User user, String accessKey) {
        final User userExtraction = extractUser(token, accessKey);
        return userExtraction.getUserId().equals(user.getUserId()) && !isTokenExpired(token, accessKey);
    }

    public boolean isTokenExpired(String token, String accessKey) {
        return extractClaim(token, Claims::getExpiration, accessKey).before(new Date());
    }

    private Key getSigningKey(String keyString) {
        byte[] keyBytes = Decoders.BASE64.decode(keyString);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
