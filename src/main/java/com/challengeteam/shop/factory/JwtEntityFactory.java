package com.challengeteam.shop.factory;

import com.challengeteam.shop.entity.jwt.JwtEntity;
import com.challengeteam.shop.entity.user.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class JwtEntityFactory {

    public static JwtEntity create(User user) {
        return new JwtEntity(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()))
        );
    }

}
