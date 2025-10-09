package com.challengeteam.shop.testData;

import com.challengeteam.shop.entity.user.User;

public interface UserTestData {
    Long GARRY_ID = 1L;
    String GARRY_EMAIL = "garry@gmail.com";
    String GARRY_PASSWORD = "password1";
    String GARRY_FIRSTNAME = "Garry";
    String GARRY_LASTNAME = "Smith";
    String GARRY_CITY = "Tokio";
    String GARRY_PHONE_NUMBER = "9945239";

    Long CHARLEY_ID = 2L;
    String CHARLEY_EMAIL = "charley@gmail.com";
    String CHARLEY_PASSWORD = "password2";
    String CHARLEY_FIRSTNAME = "Charley";
    String CHARLEY_LASTNAME = "Brown";
    String CHARLEY_CITY = "New York";
    String CHARLEY_PHONE_NUMBER = "1234567";

    Long JEREMY_ID = 3L;
    String JEREMY_EMAIL = "jeremy@gmail.com";
    String JEREMY_PASSWORD = "password3";
    String JEREMY_FIRSTNAME = "Jeremy";
    String JEREMY_LASTNAME = "Clark";
    String JEREMY_CITY = "London";
    String JEREMY_PHONE_NUMBER = "2345678";

    Long BAKER_ID = 4L;
    String BAKER_EMAIL = "baker@gmail.com";
    String BAKER_PASSWORD = "password4";
    String BAKER_FIRSTNAME = "Baker";
    String BAKER_LASTNAME = "Lee";
    String BAKER_CITY = "Paris";
    String BAKER_PHONE_NUMBER = "3456789";

    Long LORY_ID = 5L;
    String LORY_EMAIL = "lory@gmail.com";
    String LORY_PASSWORD = "password5";
    String LORY_FIRSTNAME = "Lory";
    String LORY_LASTNAME = "Adams";
    String LORY_CITY = "Berlin";
    String LORY_PHONE_NUMBER = "4567890";

    Long MESSY_ID = 6L;
    String MESSY_EMAIL = "messy@gmail.com";
    String MESSY_PASSWORD = "password6";
    String MESSY_FIRSTNAME = "Messy";
    String MESSY_LASTNAME = "Johnson";
    String MESSY_CITY = "Rome";
    String MESSY_PHONE_NUMBER = "5678901";

    Long BRANDON_ID = 7L;
    String BRANDON_EMAIL = "brandon@gmail.com";
    String BRANDON_PASSWORD = "password7";
    String BRANDON_FIRSTNAME = "Brandon";
    String BRANDON_LASTNAME = "Taylor";
    String BRANDON_CITY = "Madrid";
    String BRANDON_PHONE_NUMBER = "6789012";


    static User getGarry() {
        User garry = User.builder()
                .email(GARRY_EMAIL)
                .password(GARRY_PASSWORD)
                .firstName(GARRY_FIRSTNAME)
                .lastName(GARRY_LASTNAME)
                .city(GARRY_CITY)
                .phoneNumber(GARRY_PHONE_NUMBER)
                .build();
        garry.setId(GARRY_ID);
        garry.setRole(RoleTestData.getUserRole());
        return garry;
    }

    static User getCharley() {
        User charley = User.builder()
                .email(CHARLEY_EMAIL)
                .password(CHARLEY_PASSWORD)
                .firstName(CHARLEY_FIRSTNAME)
                .lastName(CHARLEY_LASTNAME)
                .city(CHARLEY_CITY)
                .phoneNumber(CHARLEY_PHONE_NUMBER)
                .build();
        charley.setId(CHARLEY_ID);
        charley.setRole(RoleTestData.getUserRole());
        return charley;
    }

    static User getJeremy() {
        User jeremy = User.builder()
                .email(JEREMY_EMAIL)
                .password(JEREMY_PASSWORD)
                .firstName(JEREMY_FIRSTNAME)
                .lastName(JEREMY_LASTNAME)
                .city(JEREMY_CITY)
                .phoneNumber(JEREMY_PHONE_NUMBER)
                .build();
        jeremy.setId(JEREMY_ID);
        jeremy.setRole(RoleTestData.getUserRole());
        return jeremy;
    }

    static User getBaker() {
        User baker = User.builder()
                .email(BAKER_EMAIL)
                .password(BAKER_PASSWORD)
                .firstName(BAKER_FIRSTNAME)
                .lastName(BAKER_LASTNAME)
                .city(BAKER_CITY)
                .phoneNumber(BAKER_PHONE_NUMBER)
                .build();
        baker.setId(BAKER_ID);
        baker.setRole(RoleTestData.getUserRole());
        return baker;
    }

    static User getLory() {
        User lory = User.builder()
                .email(LORY_EMAIL)
                .password(LORY_PASSWORD)
                .firstName(LORY_FIRSTNAME)
                .lastName(LORY_LASTNAME)
                .city(LORY_CITY)
                .phoneNumber(LORY_PHONE_NUMBER)
                .build();
        lory.setId(LORY_ID);
        lory.setRole(RoleTestData.getUserRole());
        return lory;
    }

    static User getMessy() {
        User messy = User.builder()
                .email(MESSY_EMAIL)
                .password(MESSY_PASSWORD)
                .firstName(MESSY_FIRSTNAME)
                .lastName(MESSY_LASTNAME)
                .city(MESSY_CITY)
                .phoneNumber(MESSY_PHONE_NUMBER)
                .build();
        messy.setId(MESSY_ID);
        messy.setRole(RoleTestData.getUserRole());
        return messy;
    }

    static User getBrandon() {
        User brandon = User.builder()
                .email(BRANDON_EMAIL)
                .password(BRANDON_PASSWORD)
                .firstName(BRANDON_FIRSTNAME)
                .lastName(BRANDON_LASTNAME)
                .city(BRANDON_CITY)
                .phoneNumber(BRANDON_PHONE_NUMBER)
                .build();
        brandon.setId(BRANDON_ID);
        brandon.setRole(RoleTestData.getUserRole());
        return brandon;
    }

}
