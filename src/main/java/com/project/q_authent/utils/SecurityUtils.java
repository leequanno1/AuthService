package com.project.q_authent.utils;

import com.nimbusds.jwt.SignedJWT;
import com.project.q_authent.exceptions.BadException;
import com.project.q_authent.exceptions.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.text.ParseException;
import java.util.Objects;

public class SecurityUtils {

    /**
     * Retrieves the user id of the currently authenticated user.
     *
     * @return the user ID of the current user or null if no authentication is present.
     * @since 1.00
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (ObjectUtils.isEmpty(authentication) || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    /**
     * Get current jwt token.
     *
     * @return jwt token
     * @since 1.00
     */
    public static SignedJWT getCurrentJWTToken(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (ObjectUtils.isEmpty(authentication) || !authentication.isAuthenticated()) {
            return null;
        }
        Jwt jwt = (Jwt) authentication.getCredentials();
        String jwtToken = jwt.getTokenValue();
        try {
            return SignedJWT.parse(jwtToken);
        }catch (ParseException e){
            throw new BadException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public static String getPoolKeyHeader() {
        return getRequestHeader("ATF-Pool-Key");
    }

    public static String getAuthorizationHeader() {
        return getRequestHeader("Authorization");
    }

    public static String getRequestHeader(String headerName) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return null;

        HttpServletRequest request = attributes.getRequest();
        String headerVal = request.getHeader(headerName);
        if (Objects.isNull(headerVal) || headerVal.isBlank()) {
            return null;
        }

        return headerVal;
    }

    public static String getCookieValue(HttpServletRequest servletRequest, String cookieName) {
        Cookie[] cookies = servletRequest.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public static final String COOKIE_REFRESH_TOKEN = "ATF_refresh_token";

    public static final String COOKIE_NEED_ACTIVE_ID = "ATF_need_active_id";

    public static final String COOKIE_NEED_RESET_ID = "ATF_need_reset_id";

    public static final String COOKIE_RESET_CODE_ID = "ATF_reset_code_id";

}