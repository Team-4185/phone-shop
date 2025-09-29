package com.challengeteam.shop.factory;

import com.challengeteam.shop.entity.jwt.JwtEntity;
import com.challengeteam.shop.entity.user.Role;
import com.challengeteam.shop.entity.user.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JwtEntityFactoryTest {

    @Test
    void testCreate() {
        User user = new User();
        user.setId(1L);
        user.setUsername("challengeteam");
        user.setPassword("123456");
        user.setRole(Role.USER);

        JwtEntity jwtEntity = JwtEntityFactory.create(user);

        assertNotNull(jwtEntity);
        assertEquals(user.getId(), jwtEntity.getId());
        assertEquals(user.getUsername(), jwtEntity.getUsername());
        assertEquals(user.getPassword(), jwtEntity.getPassword());
        assertEquals(1, jwtEntity.getAuthorities().size());
        assertEquals("ROLE_USER", jwtEntity.getAuthorities().iterator().next().getAuthority());
    }

}
