package com.project.q_authent.utils;

import com.nimbusds.jwt.SignedJWT;
import com.project.q_authent.exceptions.BadException;
import com.project.q_authent.exceptions.ErrorCode;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.text.ParseException;

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

}