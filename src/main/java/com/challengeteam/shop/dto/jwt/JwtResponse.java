package com.challengeteam.shop.dto.jwt;

import lombok.Data;

@Data
public class JwtResponse {

    private Long userId;

    private String username;

    private String accessToken;

    private String refreshToken;

}
