package com.challengeteam.shop.security.filter;

import com.challengeteam.shop.exceptionHandling.exception.security.InvalidTokenException;
import com.challengeteam.shop.service.security.auth.jwt.JwtService;
import com.challengeteam.shop.service.security.auth.logout.blackListTokenCache.TokenRevocationService;
import com.challengeteam.shop.utility.web.headers.AccessTokenHeaderExtractor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenRevocationService tokenRevocationService;

    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String bearerToken = AccessTokenHeaderExtractor.extractAccessToken(request);

        if (bearerToken != null && jwtService.isValid(bearerToken)) {
            if (!jwtService.isAccessToken(bearerToken)) {
                throw new InvalidTokenException("Invalid token type. Access token required.");
            }
            if (tokenRevocationService.isRevoked(bearerToken)) {
                logger.warn("Token is revoked");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("User was logged out");
                return;
            }
            authenticateByToken(bearerToken);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateByToken(String bearerToken) {
        String username = jwtService.getEmailFromToken(bearerToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}