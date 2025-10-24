package com.challengeteam.shop.utility;

import com.challengeteam.shop.exceptionHandling.exception.FileUtilityException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static com.challengeteam.shop.utility.FileUtilityTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileUtilityTest {

    @Nested
    class GetFileExtensionTest {

        @Test
        void whenGivenMultipartFile_thenReturnFileExtension() throws Exception {
            // when
            String result = FileUtility.getFileExtension(buildMultipartFile());

            // then
            assertThat(result).isEqualTo(EXTENSION);
        }

        @Test
        void whenGivenMultipartFileWithoutFilename_thenThrowException() throws Exception {
            // when + then
            assertThatThrownBy(() -> FileUtility.getFilename(buildMultipartFileWithoutOriginalFilename()))
                    .isInstanceOf(FileUtilityException.class);
        }

        @Test
        void whenParameterMultipartFileIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> FileUtility.getFilename(null)).isInstanceOf(NullPointerException.class);
        }

    }

    @Nested
    class GetFilenameTest {

        @Test
        void whenGivenMultipartFile_thenReturnFilename() throws Exception {
            // when
            String result = FileUtility.getFilename(buildMultipartFile());

            // then
            assertThat(result).isEqualTo(NAME);
        }

        @Test
        void whenGivenMultipartFileDoesntHaveFilename_thenThrowException() throws Exception {
            // when + then
            assertThatThrownBy(() -> FileUtility.getFilename(buildMultipartFileWithoutOriginalFilename()))
                    .isInstanceOf(FileUtilityException.class);
        }

        @Test
        void whenParameterMultipartFileIsNull_thenThrowException() throws Exception {
            // when + then
            assertThatThrownBy(() -> FileUtility.getFilename(null)).isInstanceOf(NullPointerException.class);
        }

    }

    @Nested
    class GetContentTypeTest {

        @Test
        void whenGivenMultipartFile_thenReturnContentType() throws Exception {
            // when
            String result = FileUtility.getContentType(buildMultipartFile());

            // then
            assertThat(result).isEqualTo(CONTENT_TYPE);
        }

        @Test
        void whenGivenMultipartFileDoesntHaveContentType_thenThrowException() throws Exception {
            // when + then
            assertThatThrownBy(() -> FileUtility.getContentType(buildMultipartFileWithoutContentType()))
                    .isInstanceOf(FileUtilityException.class);
        }

        @Test
        void whenParameterContentTypeIsNull_thenThrowException() throws Exception {
            // when + then
            assertThatThrownBy(() -> FileUtility.getContentType(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class TransformFilenameToUniqueTest {

        @Test
        void whenGivenFilename_thenReturnUniqueFilename() {
            // when
            String first = FileUtility.transformFilenameToUnique(NAME);
            String second = FileUtility.transformFilenameToUnique(NAME);

            // then
            String suffix = FileUtility.FILENAME_SEPARATOR + NAME;
            assertThat(first).isNotEqualTo(second);
            assertThat(first).endsWith(suffix);
            assertThat(second).endsWith(suffix);
        }

        @Test
        void whenFilenameIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> FileUtility.transformFilenameToUnique(null))
                    .isInstanceOf(NullPointerException.class);
        }

    }

    static class TestResources {
        static final String NAME = "filename.jpeg";
        static final String EXTENSION = "jpeg";
        static final String CONTENT_TYPE = "image/jpeg";
        static final byte[] CONTENT = NAME.getBytes();

        static MultipartFile buildMultipartFile() {
            return new MockMultipartFile(
                    "file",
                    NAME,
                    CONTENT_TYPE,
                    CONTENT
            );
        }

        static MultipartFile buildMultipartFileWithoutOriginalFilename() {
            return new MockMultipartFile(
                    "file",
                    null,
                    CONTENT_TYPE,
                    CONTENT
            );
        }

        static MultipartFile buildMultipartFileWithoutContentType() {
            return new MockMultipartFile(
                    "file",
                    NAME,
                    null,
                    CONTENT
            );
        }
    }

}