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


    @Override

        // generate users
            try {
                userService.updateProfile(id, profile);
            } catch (Exception e) {
            }
        }

        // report
        String createdUsersData = concatToStrings(createdUsers);
    }

    @Override
    }

    private String concatToStrings(List<?> objects) {
        StringBuilder sb = new StringBuilder();

        for (Object object : objects) {
        }

        return sb.toString();
    }

    private static class UserGenerator {
        public final static String DEFAULT_PASSWORD = "pass1234";

        }

            return null;
        }

    }

}
