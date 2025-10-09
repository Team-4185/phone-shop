package com.challengeteam.shop.testData;

import com.challengeteam.shop.entity.user.Role;

public interface RoleTestData {
    Long ROLE_USER_ID = 1L;
    String ROLE_USER_NAME = "USER";

    Long ROLE_ADMIN_ID = 2L;
    String ROLE_ADMIN_NAME = "ADMIN";

    Long ROLE_OTHER_ID = 3L;
    String ROLE_OTHER_NAME = "OTHER";

    static Role getUserRole() {
        Role userRole = Role.builder()
                .name(ROLE_USER_NAME)
                .build();
        userRole.setId(ROLE_USER_ID);
        return userRole;
    }

    static Role getAdminRole() {
        Role adminRole = Role.builder()
                .name(ROLE_ADMIN_NAME)
                .build();
        adminRole.setId(ROLE_ADMIN_ID);
        return adminRole;
    }

    static Role getOtherRole() {
        Role otherRole = Role.builder()
                .name(ROLE_OTHER_NAME)
                .build();
        otherRole.setId(ROLE_OTHER_ID);
        return otherRole;
    }

}
