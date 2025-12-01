package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.entity.image.Image;
import com.challengeteam.shop.entity.image.MIMEType;
import com.challengeteam.shop.persistence.repository.ImageRepository;
import com.challengeteam.shop.persistence.repository.MIMETypeRepository;
import com.challengeteam.shop.service.ImageService;
import com.challengeteam.shop.service.MIMETypeService;
import com.challengeteam.shop.testContainer.ContainerExtension;
import com.challengeteam.shop.testContainer.TestContextConfigurator;
import com.challengeteam.shop.web.TestAuthHelper;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static com.challengeteam.shop.web.controller.ImageControllerTest.TestResources.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(ContainerExtension.class)
class ImageControllerTest {
    @Autowired private TestAuthHelper testAuthHelper;
    @Autowired private ImageService imageService;
    @Autowired private ImageRepository imageRepository;
    @Autowired private MIMETypeService mimeTypeService;
    @Autowired private MockMvc mockMvc;
    private Long imageId;
    private Long unsupportedImageId;
    private String accessToken;

    @DynamicPropertySource
    static void loadPropertiesForTest(DynamicPropertyRegistry propertyRegistry) {
        TestContextConfigurator.initRequiredProperties(propertyRegistry);
    }

    @BeforeEach
    public void setup() {
        // clean all
        // note: when we are deleting all images from the repository,
        //       we are not deleting from image storage. So, we can ignore
        //       the fact that image remains there
        imageRepository.deleteAll();

        // add test data
        imageId = imageService.uploadImage(buildMultipartFile(TestImage.IMAGE_1)).getId();
        // IMAGE_3 is unsupported
        unsupportedImageId = loadUnsupportedImage(TestImage.IMAGE_1, TestImage.IMAGE_3);

        // authorize
        accessToken = testAuthHelper.authorizeLikeTestUser();
    }

    private Long loadUnsupportedImage(TestImage supported, TestImage unsupported) {
        // first load support image
        Image image = imageService.uploadImage(buildMultipartFile(supported));

        // add unsupported MIMEType
        MIMEType unsupportedType = mimeTypeService.createIfDoesntExist(buildMultipartFile(unsupported));

        // change existing image MIMEType
        image.setMimeType(unsupportedType);
        imageRepository.save(image);

        return image.getId();
    }

    @Nested
    @DisplayName("GET /api/v1/images/{id}")
    class GetImageTest {
        private final static String URL = "/api/v1/images/{id}";

        @Test
        void whenExists_thenStatus200AndReturnImageLikeBytes() throws Exception {
            mockMvc.perform(get(URL, imageId)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken)))
                    .andExpect(status().isOk())
                    .andExpect(header().exists(HttpHeaders.CONTENT_LENGTH))
                    .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION))
                    .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                    .andExpect(content().bytes(TestImage.IMAGE_1.content));
        }

        @Test
        void whenDoesntExist_thenStatus404() throws Exception {
            mockMvc.perform(get(URL, NON_EXISTING_ID)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenRequestMissingToken_thenStatus403() throws Exception {
            mockMvc.perform(get(URL, imageId))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            mockMvc.perform(get(URL, imageId)
                            .header(HttpHeaders.AUTHORIZATION, auth("some_invalid_text")))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestToGetUnsupportedImage_thenStatus400() throws Exception {
            mockMvc.perform(get(URL, unsupportedImageId)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken)))
                    .andExpect(status().isBadRequest());
        }

    }

    @Nested
    @DisplayName("GET /api/v1/images/{id}/metadata")
    class GetImageMetadataTest {
        private static final String URL = "/api/v1/images/{id}/metadata";

        @Test
        void whenImageExists_thenStatus200AndReturnMetadata() throws Exception {
            mockMvc.perform(get(URL, imageId)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.url").exists())
                    .andExpect(jsonPath("$.size").exists())
                    .andExpect(jsonPath("$.mimeType").exists());
        }

        @Test
        void whenImageDoesntExists_thenStatus404() throws Exception {
            mockMvc.perform(get(URL, NON_EXISTING_ID)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenRequestMissingToken_thenStatus403() throws Exception {
            mockMvc.perform(get(URL, imageId))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            mockMvc.perform(get(URL, imageId)
                            .header(HttpHeaders.AUTHORIZATION, auth("some_invalid_text")))
                    .andExpect(status().isForbidden());
        }
    }

    static class TestResources {
        static final long NON_EXISTING_ID = 99_999L;

        static String auth(String token) {
            return "Bearer " + token;
        }

        static MultipartFile buildMultipartFile(TestImage image) {
            return new MockMultipartFile(
                    image.filename,
                    image.originalFilename,
                    image.contentType,
                    image.content
            );
        }

    }

    enum TestImage {
        IMAGE_1(
                "web/controller/imageController/image_1.jpg",
                "image",
                "image_1.jpg",
                "image/jpg"
        ),
        IMAGE_2(
                "web/controller/imageController/image_2.jpeg",
                "image",
                "image_2.jpeg",
                "image/jpg"
        ),
        IMAGE_3(
                "web/controller/imageController/image_3.gif",
                "image",
                "image_3.gif",
                "image/gif"
        );

        private final String url;
        private final String filename;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        TestImage(String url, String filename, String originalFilename, String contentType) {
            this.url = url;
            this.filename = filename;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = readImageBytes(url);
        }

        private static byte[] readImageBytes(String imagePath) {
            try (InputStream is = TestResources.class.getClassLoader().getResourceAsStream(imagePath)) {
                if (is != null) {
                    byte[] bytes = is.readAllBytes();
                    if (bytes != null) {

                        return bytes;
                    }
                }
                throw new RuntimeException("Failed to read test image from test resources using path: " + imagePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}