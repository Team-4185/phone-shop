package com.challengeteam.shop.utility.web.headers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
/**
 * Utility for extracting Bearer tokens from HTTP request headers.
 */
@UtilityClass
public class AccessTokenHeaderExtractor {
    public static final String ACCESS_TOKEN_HEADER = "Authorization";
    public static final String BEARER_TOKEN_PREFIX = "Bearer ";
    /**
     * Extracts the Bearer token from the {@code Authorization} header.
     *
     * @param request the incoming HTTP request
     * @return the token string, or {@code null} if the header is missing or not a Bearer token
     */
    public String extractAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(ACCESS_TOKEN_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER_TOKEN_PREFIX)) {
            return bearerToken.substring(BEARER_TOKEN_PREFIX.length());
        } else {
            return null;
        }
    }
}