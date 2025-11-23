package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.phone.PhoneCreateRequestDto;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.service.PhoneService;
import com.challengeteam.shop.testContainer.ContainerExtension;
import com.challengeteam.shop.testContainer.TestContextConfigurator;
import com.challengeteam.shop.web.TestAuthHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.challengeteam.shop.web.controller.PhoneControllerTest.TestResources.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(ContainerExtension.class)
public class PhoneControllerTest {
    @Autowired
    private TestAuthHelper testAuthHelper;
    @Autowired
    private PhoneRepository phoneRepository;
    @Autowired
    private PhoneService phoneService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private String token;
    private Long phone1;
    private Long phone2;
    private Long phone3;

    @DynamicPropertySource
    public static void loadPropertiesForTest(DynamicPropertyRegistry propertyRegistry) {
        TestContextConfigurator.initRequiredProperties(propertyRegistry);
    }

    @BeforeEach
    public void setup() {
        // clear all
        phoneRepository.deleteAll();

        // add 3 phones
        phone1 = phoneService.create(buildPhoneCreateRequestDto(TestPhone.PHONE_1), new ArrayList<>());
        phone2 = phoneService.create(buildPhoneCreateRequestDto(TestPhone.PHONE_2), List.of(buildMultipartFile()));
        phone3 = phoneService.create(buildPhoneCreateRequestDto(TestPhone.PHONE_3), List.of(buildMultipartFile()));

        // authorize
        token = testAuthHelper.authorizeLikeTestUser();
    }

    @Nested
    @DisplayName("GET /api/v1/phones")
    class GetPhonesTest {
        public static final String URL = "/api/v1/phones";

        @Test
        void whenDefaultRequest_thenReturnFirstPage() throws Exception {
            var request = get(URL)
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.numberOfElements").value(3))
                    .andExpect(jsonPath("$.content.size()").value(3))
                    .andExpect(jsonPath("$.content.[0].id").value(phone1));
        }

        @Test
        void whenRequestSecondPage_thenReturnEmptyPage() throws Exception {
            var request = get(URL)
                    .param("page", "1")
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.numberOfElements").value(0))
                    .andExpect(jsonPath("$.content.size()").value(0));
        }

        @Test
        void whenRequestMissingToken_thenReturn403() throws Exception {
            var request = get(URL);

            mockMvc.perform(request)
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenReturn403() throws Exception {
            var request = get(URL)
                    .header(HttpHeaders.AUTHORIZATION, auth("invalid_token"));

            mockMvc.perform(request)
                    .andExpect(status().isForbidden());
        }

    }

    @Nested
    @DisplayName("GET /api/v1/phones/{id}")
    class GetPhoneByIdTest {
        public static final String URL = "/api/v1/phones/{id}";

        @Test
        void whenPhoneExists_thenReturnPhoneAndStatus200() throws Exception {
            var request = get(URL, phone1)
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(phone1))
                    .andExpect(jsonPath("$.name").value(TestPhone.PHONE_1.name))
                    .andExpect(jsonPath("$.description").value(TestPhone.PHONE_1.description))
                    .andExpect(jsonPath("$.price").value(TestPhone.PHONE_1.price))
                    .andExpect(jsonPath("$.brand").value(TestPhone.PHONE_1.brand))
                    .andExpect(jsonPath("$.releaseYear").value(TestPhone.PHONE_1.releaseYear));
        }

        @Test
        void whenPhoneDoesntExists_thenReturnStatus404() throws Exception {
            var request = get(URL, UNEXISTING_PHONE_ID)
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Not Found Resource"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.detail").value("Not found phone with id: " + UNEXISTING_PHONE_ID));
        }

        @Test
        void whenRequestMissingToken_thenReturn403() throws Exception {
            var request = get(URL, phone1);

            mockMvc.perform(request)
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenReturn403() throws Exception {
            var request = get(URL, phone1)
                    .header(HttpHeaders.AUTHORIZATION, auth("invalid_token"));

            mockMvc.perform(request)
                    .andExpect(status().isForbidden());
        }

    }

    @Nested
    @DisplayName("POST /api/v1/phones")
    class CreatePhoneTest {
        public static final String URL = "/api/v1/phones";

        @Test
        void whenDataValid_thenCreateAndReturn201() throws Exception {
            // when
            var request = multipart(URL)
                    .file((MockMultipartFile) buildPhoneMultipartFile(TestPhone.PHONE_2, objectMapper))
                    .file((MockMultipartFile) buildMultipartFile())
                    .file((MockMultipartFile) buildMultipartFile())
                    .header(HttpHeaders.AUTHORIZATION, auth(token))
                    .accept(MediaType.MULTIPART_FORM_DATA_VALUE);

            mockMvc.perform(request)
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("location"));
        }

        // todo: continue

    }

    // todo: continue

    static class TestResources {
        public static final String IMAGE_NAME = "images";
        public static final String IMAGE_ORIGINAL_NAME = "image.jpeg";
        public static final String IMAGE_CONTENT_TYPE = "image/jpeg";
        public static final byte[] IMAGE_CONTENT = IMAGE_ORIGINAL_NAME.getBytes();

        public static final Long UNEXISTING_PHONE_ID = 999_999L;

        public static MultipartFile buildMultipartFile() {
            return new MockMultipartFile(
                    IMAGE_NAME,
                    IMAGE_ORIGINAL_NAME,
                    IMAGE_CONTENT_TYPE,
                    IMAGE_CONTENT
            );
        }

        public static MultipartFile buildPhoneMultipartFile(TestPhone phone, ObjectMapper mapper) throws Exception {
            PhoneCreateRequestDto phoneCreateRequestDto = buildPhoneCreateRequestDto(phone);

            return new MockMultipartFile(
                    "phone",
                    "",
                    MediaType.APPLICATION_JSON.toString(),
                    mapper.writeValueAsBytes(phoneCreateRequestDto)
            );
        }

        public static PhoneCreateRequestDto buildPhoneCreateRequestDto(TestPhone phone) {
            return new PhoneCreateRequestDto(
                    phone.name,
                    phone.description,
                    phone.price,
                    phone.brand,
                    phone.releaseYear
            );
        }

        static String auth(String token) {
            return "Bearer " + token;
        }

    }

    enum TestPhone {
        PHONE_1(
                "Phone_1",
                "Description_1",
                new BigDecimal("1000.0"),
                "Brand_1",
                2001
        ),
        PHONE_2(
                "Phone_2",
                "Description_2",
                new BigDecimal("2000.0"),
                "Brand_2",
                2002
        ),
        PHONE_3(
                "Phone_3",
                null,
                new BigDecimal("3000.0"),
                "Brand_3",
                2003
        );

        private final String name;
        private final String description;
        private final BigDecimal price;
        private final String brand;
        private final Integer releaseYear;


        TestPhone(String name, String description, BigDecimal price, String brand, Integer releaseYear) {
            this.name = name;
            this.description = description;
            this.price = price;
            this.brand = brand;
            this.releaseYear = releaseYear;
        }

    }

}
