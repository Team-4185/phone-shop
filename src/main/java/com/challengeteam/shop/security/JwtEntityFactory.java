package com.challengeteam.shop.security;

import com.challengeteam.shop.entity.user.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class JwtEntityFactory {

    public static JwtEntity create(User user) {
        return new JwtEntity(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

}
