package com.challengeteam.shop.constants.security.jwt;

import lombok.experimental.UtilityClass;

@UtilityClass
public class JwtTokenNameConstants {
    public final String ACCESS_TOKEN_HEADER = "Authorization";
    public final String BEARER_TOKEN_PREFIX = "Bearer ";

    public final String TOKEN_TYPE_CLAIM = "tokenType";
    public final String REFRESH_TOKEN_HEADER = "refreshToken";
    public final String ACCESS_TOKEN_TYPE = "ACCESS";
    public final String REFRESH_TOKEN_TYPE = "REFRESH";
    public final String RESET_TOKEN_TYPE = "RESET";
}