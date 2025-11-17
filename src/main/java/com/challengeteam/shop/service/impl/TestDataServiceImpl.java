package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.user.CreateUserDto;
import com.challengeteam.shop.dto.user.UpdateProfileDto;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.service.PhoneService;
import com.challengeteam.shop.service.TestDataService;
import com.challengeteam.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class TestDataServiceImpl implements TestDataService {
    private final UserService userService;
    private final PhoneService phoneService;

    private UserGenerator userGenerator = new UserGenerator();

    @Override
    public void generateUsers(Long amount) {
        List<CreateUserDto> createdUsers = new ArrayList<>();

        // generate users
        while (createdUsers.size() < amount) {
            try {
                CreateUserDto newUser = userGenerator.generateUser();
                UpdateProfileDto profile = userGenerator.generateUserProfile();
                Long id = userService.createDefaultUser(newUser);
                userService.updateProfile(id, profile);

                createdUsers.add(newUser);
            } catch (Exception e) {
                log.debug("Failed to create test user: {}", e.getMessage());
            }
        }

        // report
        String createdUsersData = concatToStrings(createdUsers);
        log.info("Generated {} users: {}", createdUsers, createdUsersData);
    }

    @Override
    public void generatePhones(Long amount) {
        // todo: implement
    }

    private String concatToStrings(List<?> objects) {
        StringBuilder sb = new StringBuilder();

        for (Object object : objects) {
            sb.append(object.toString());
        }

        return sb.toString();
    }

    private static class UserGenerator {
        public final static String DEFAULT_PASSWORD = "pass1234";

        public CreateUserDto generateUser() {
            // todo: find way to generate realistic user
            // todo: implement
            return null;
        }

        public UpdateProfileDto generateUserProfile() {
            // todo: find way to generate realistic user
            // todo: implement
            return null;
        }

    }

}
