package com.challengeteam.shop.dto.user;

import lombok.Data;

@Data
public class UserRegisterRequest {

    private String username;

    private String password;

    private String passwordConfirmation;

}
