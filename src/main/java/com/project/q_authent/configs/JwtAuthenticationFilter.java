package com.project.q_authent.configs;
import com.project.q_authent.constances.PublicEndpoint;
import com.project.q_authent.repositories.AccountRepository;
import com.project.q_authent.services.authent_services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import org.springframework.util.AntPathMatcher;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AccountRepository accountRepository;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String accountId;

        String path = request.getServletPath();
        for (String publicPattern : PublicEndpoint.endpoints) {
            if (pathMatcher.match(publicPattern, path)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            response.setContentType("application/json");
            response.getWriter().write("{\"code\": \"403\",\"message\": \"Missing or invalid Authorization header\"}");
            return;
        }

        jwt = authHeader.substring(7);
        try {
            accountId = jwtService.extractAccountId(jwt, false);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            response.setContentType("application/json");
            response.getWriter().write("{\"code\": \"403\",\"message\": \"Invalid or expired token\"}");
            return;
        }

        if (accountId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var user = accountRepository.findById(accountId).orElse(null);

            if (user != null && jwtService.isTokenValid(jwt, user, false)) {
                var authToken = new UsernamePasswordAuthenticationToken(
                        user.getAccountId(), null, null
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\": \"403\",\"message\": \"Invalid token or user not found\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
