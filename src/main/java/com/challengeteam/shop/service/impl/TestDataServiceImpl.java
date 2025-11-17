package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.user.CreateUserDto;
import com.challengeteam.shop.dto.user.UpdateProfileDto;
import com.challengeteam.shop.exceptionHandling.exception.CriticalSystemException;
import com.challengeteam.shop.service.PhoneService;
import com.challengeteam.shop.service.TestDataService;
import com.challengeteam.shop.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

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
    public void generateUsers(int amount) {
        List<CreateUserDto> createdUsers = new ArrayList<>();

        // generate users
        List<UserGenerator.FakeUser> fakeUsers = userGenerator.generateFakeUsers(amount);
        for (UserGenerator.FakeUser fakeUser : fakeUsers) {
            try {
                var user = new CreateUserDto(
                        fakeUser.email(),
                        fakeUser.password()
                );
                var profile = new UpdateProfileDto(
                        fakeUser.firstname(),
                        fakeUser.lastname(),
                        fakeUser.city(),
                        fakeUser.phoneNumber()
                );

                Long id = userService.createDefaultUser(user);
                userService.updateProfile(id, profile);
                createdUsers.add(user);
            } catch (Exception e) {
                log.debug("Failed to create test user: {}", e.getMessage());
            }
        }

        // report
        String createdUsersData = concatToStrings(createdUsers);
        log.info("Generated {} users: {}", createdUsers.size(), createdUsersData);
    }

    @Override
    public void generatePhones(int amount) {
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
        public final static String GENERATOR_URL = "https://randomuser.me";
        public final static int GENERATOR_API_LIMIT = 5000;
        public final static String DEFAULT_PASSWORD = "pass1234";
        private final RestClient client;
        private final ObjectMapper objectMapper;

        public UserGenerator() {
            this.client = RestClient.builder()
                    .baseUrl(GENERATOR_URL)
                    .build();
            this.objectMapper = new ObjectMapper();
        }

        public List<FakeUser> generateFakeUsers(int amount) {
            if (amount > GENERATOR_API_LIMIT) {
                throw new RuntimeException();   // todo: replace here
            }

            List<FakeUser> result = new ArrayList<>(amount);
            String json = client.get()
                    .uri("/api?results={amount}&inc=email,name,location,phone", amount)
                    .retrieve()
                    .body(String.class);

            try {
                JsonNode node = objectMapper.readTree(json);
                for (JsonNode row : node.withArray("results")) {
                    var user = new FakeUser(
                            row.path("email").asText(),
                            DEFAULT_PASSWORD,
                            row.path("name").path("first").asText(),
                            row.path("name").path("last").asText(),
                            row.path("location").path("city").asText(),
                            row.path("phone").asText()
                    );
                    result.add(user);
                }
            } catch (JsonProcessingException e) {
                throw new CriticalSystemException("Failed to generate tests data, for reason: objectMapper doesn't work", e);
            }

            return result;
        }

        record FakeUser(
                String email,
                String password,
                String firstname,
                String lastname,
                String city,
                String phoneNumber
        ) {
        }

    }

}
