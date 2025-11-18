package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.phone.PhoneCreateRequestDto;
import com.challengeteam.shop.dto.user.CreateUserDto;
import com.challengeteam.shop.dto.user.UpdateProfileDto;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.CriticalSystemException;
import com.challengeteam.shop.exceptionHandling.exception.TestDataGeneratorOutOfLimitException;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
public class TestDataServiceImpl implements TestDataService {
    private final UserService userService;
    private final PhoneService phoneService;
    private final UserGenerator userGenerator = new UserGenerator();
    private final PhoneGenerator phoneGenerator = new PhoneGenerator();

    @Override
    public int generateUsers(int amount) {
        List<CreateUserDto> createdUsers = new ArrayList<>(amount);

        // generate users
        List<User> fakeUsers = userGenerator.generateFakeUsers(amount);
        for (User fakeUser : fakeUsers) {
            try {
                var user = new CreateUserDto(
                        fakeUser.getEmail(),
                        fakeUser.getPassword()
                );
                var profile = new UpdateProfileDto(
                        fakeUser.getFirstName(),
                        fakeUser.getLastName(),
                        fakeUser.getCity(),
                        fakeUser.getPhoneNumber()
                );

                Long id = userService.createDefaultUser(user);
                userService.updateProfile(id, profile);
                createdUsers.add(user);
            } catch (Exception e) {
                log.debug("Test data exception: failed to create fake user {}, because {}", fakeUser, e.getMessage());
            }
        }

        // report
        String createdUsersData = concatToStrings(createdUsers);
        log.info("Generated {} fake users: {}", createdUsers.size(), createdUsersData);

        return createdUsers.size();
    }

    @Override
    public int generatePhones(int amount) {
        List<Long> createdPhones = new ArrayList<>(amount);

        // generate phones
        List<PhoneCreateRequestDto> phones = phoneGenerator.generatePhones(amount);
        for (PhoneCreateRequestDto phone : phones) {
            try {
                Long id = phoneService.create(phone);
                createdPhones.add(id);
            } catch (Exception e) {
                log.debug("Test data exception: failed to create fake phone {}, because {}", phone, e.getMessage());
            }
        }

        // report
        String createdPhonesData = concatToStrings(createdPhones);
        log.info("Generated {} fake phones: {}",createdPhones.size(), createdPhonesData);

        return createdPhones.size();
    }

    private String concatToStrings(List<?> objects) {
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        for (Object object : objects) {
            sb.append(object.toString()).append(",");
        }
        sb.append("]");

        return sb.toString();
    }

    private static class UserGenerator {
        public final static String GENERATOR_URL = "https://randomuser.me";
        public final static int GENERATOR_API_LIMIT = 1000;
        public final static String DEFAULT_PASSWORD = "pass1234";
        private final RestClient client;
        private final ObjectMapper objectMapper;


        public UserGenerator() {
            this.client = RestClient.builder()
                    .baseUrl(GENERATOR_URL)
                    .build();
            this.objectMapper = new ObjectMapper();
        }

        public List<User> generateFakeUsers(int amount) {
            if (amount > GENERATOR_API_LIMIT) {
                throw new TestDataGeneratorOutOfLimitException("User", GENERATOR_API_LIMIT);
            }

            List<User> result = new ArrayList<>(amount);
            String json = client.get()
                    .uri("/api?results={amount}&inc=email,name,location,phone", amount)
                    .retrieve()
                    .body(String.class);

            try {
                JsonNode node = objectMapper.readTree(json);
                for (JsonNode row : node.withArray("results")) {
                    var user = User.builder()
                            .email(row.path("email").asText())
                            .password(DEFAULT_PASSWORD)
                            .firstName(row.path("name").path("first").asText())
                            .lastName(row.path("name").path("last").asText())
                            .city(row.path("location").path("city").asText())
                            .phoneNumber(row.path("phone").asText())
                            .build();

                    result.add(user);
                }
            } catch (JsonProcessingException e) {
                throw new CriticalSystemException("Failed to generate tests data, for reason: objectMapper doesn't work", e);
            }

            return result;
        }
    }

    private static class PhoneGenerator {
        public final static int GENERATOR_LIMIT = 10_000;
        public final static String DEFAULT_DESCRIPTION = "is a modern phone designed to make your life better.";
        public final static int PRICE_MIN = 3_000;
        public final static int PRICE_MAX = 500_000;
        public final static int RELEASE_YEAR_TOP = LocalDate.now().getYear();
        public final static int RELEASE_YEAR_FLOOR = 1990;
        public final static int DESCRIPTION_CHANCE = 80;
        private final Random random = new Random();


        public List<PhoneCreateRequestDto> generatePhones(int amount) {
            if (amount > GENERATOR_LIMIT) {
                throw new TestDataGeneratorOutOfLimitException("Phone", GENERATOR_LIMIT);
            }

            List<PhoneCreateRequestDto> result = new ArrayList<>(amount);
            for (int i = 0; i < amount; i++) {
                PhoneBrand brand = getRandomBrand();
                String name = getRandomName(brand);
                String description = getRandomDescription(name);
                double price = getRandomPrice();
                int releaseYear = getRandomReleaseYear();

                var phone = new PhoneCreateRequestDto(
                        name,
                        description,
                        new BigDecimal(price),
                        brand.name(),
                        releaseYear
                );
                result.add(phone);
            }

            return result;
        }

        private int getRandomReleaseYear() {
            return RELEASE_YEAR_FLOOR + random.nextInt(RELEASE_YEAR_TOP - RELEASE_YEAR_FLOOR);
        }

        private double getRandomPrice() {
            return PRICE_MIN + random.nextInt(PRICE_MAX - PRICE_MIN);
        }

        private String getRandomDescription(String name) {
            if (DESCRIPTION_CHANCE > random.nextInt(100)) {
                return name + " " + DEFAULT_DESCRIPTION;
            } else {
                return null;
            }
        }

        private String getRandomName(PhoneBrand brand) {
            return brand.models[random.nextInt(brand.count)];
        }

        private PhoneBrand getRandomBrand() {
            PhoneBrand[] brands = PhoneBrand.values();
            return brands[random.nextInt(brands.length)];
        }

        enum PhoneBrand {
            Apple("iPhone 11",
                    "iPhone 11 Pro",
                    "iPhone 12",
                    "iPhone 12 Pro",
                    "iPhone 13",
                    "iPhone 13 mini",
                    "iPhone 14",
                    "iPhone 14 Pro Max",
                    "iPhone 15",
                    "iPhone 15 Pro"
            ),
            Samsung("Galaxy S21",
                    "Galaxy S21 Ultra",
                    "Galaxy S22",
                    "Galaxy S22+",
                    "Galaxy S23",
                    "Galaxy S23 Ultra",
                    "Galaxy A52",
                    "Galaxy A53",
                    "Galaxy A72",
                    "Galaxy Z Flip 4"
            ),
            Xiaomi("Redmi Note 1",
                    "Redmi Note 12",
                    "Redmi Note 12 Pro",
                    "Mi 11",
                    "Mi 11 Lite",
                    "Xiaomi 12",
                    "Xiaomi 12X",
                    "Poco X3 Pro",
                    "Poco F4",
                    "Poco M6"
            ),
            OnePlus("OnePlus 8",
                    "OnePlus 8T",
                    "OnePlus 9",
                    "OnePlus 9 Pro",
                    "OnePlus 10 Pro",
                    "OnePlus 11",
                    "OnePlus Nord",
                    "OnePlus Nord 2",
                    "OnePlus Ace",
                    "OnePlus CE 3 Lite"
            ),
            Google("Pixel 4a",
                    "Pixel 5",
                    "Pixel 6",
                    "Pixel 6a",
                    "Pixel 7",
                    "Pixel 7a",
                    "Pixel 8",
                    "Pixel 8 Pro",
                    "Pixel 8a",
                    "Pixel Fold"
            ),
            Huawei("P30",
                    "P30 Pro",
                    "P40",
                    "P50",
                    "P50 Pro",
                    "Mate 40",
                    "Mate 50",
                    "Nova 9",
                    "Nova 10",
                    "Honor 20"
            ),
            OPPO("OPPO Reno 6",
                    "OPPO Reno 7",
                    "OPPO Reno 8",
                    "OPPO Reno 10",
                    "OPPO A54",
                    "OPPO A74",
                    "OPPO A98",
                    "OPPO Find X3",
                    "OPPO Find X5",
                    "OPPO Find N2"
            ),
            vivo("vivo V21",
                    "vivo V23",
                    "vivo V25",
                    "vivo X50",
                    "vivo X60",
                    "vivo X70",
                    "vivo X80",
                    "vivo Y20",
                    "vivo Y33",
                    "vivo Y55"
            ),
            Motorola("Moto G31",
                    "Moto G41",
                    "Moto G52",
                    "Moto G72",
                    "Moto Edge 20",
                    "Moto Edge 30",
                    "Moto Edge 40",
                    "Moto E22",
                    "Moto E32",
                    "Moto Razr 2022"
            ),
            Realme("Realme 7",
                    "Realme 8",
                    "Realme 9",
                    "Realme 10",
                    "Realme 11",
                    "Realme GT",
                    "Realme GT Neo 2",
                    "Realme C25",
                    "Realme C33",
                    "Realme C51"
            );

            private final String[] models;
            private final int count;

            PhoneBrand(String... models) {
                this.models = models;
                this.count = models.length;
            }
        }
    }

}
