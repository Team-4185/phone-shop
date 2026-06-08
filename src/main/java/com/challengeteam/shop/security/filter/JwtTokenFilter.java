package com.challengeteam.shop.security.filter;

import com.challengeteam.shop.exceptionHandling.exception.security.InvalidTokenException;
import com.challengeteam.shop.security.SimpleUserDetailsService.SimpleUserDetails;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
            if (!authenticateByToken(bearerToken, response)) {
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean authenticateByToken(String bearerToken, HttpServletResponse response) throws IOException {
        String username = jwtService.getEmailFromToken(bearerToken);
        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            logger.warn("Token subject does not match an existing user");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token subject is outdated");
            return false;
        }

        if (userDetails instanceof SimpleUserDetails simpleUserDetails
                && !simpleUserDetails.getTokenVersion().equals(jwtService.getTokenVersion(bearerToken))) {
            logger.warn("Token version is outdated");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token is outdated");
            return false;
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return true;
    }
}
