package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.entity.image.MIMEType;
import com.challengeteam.shop.exceptionHandling.exception.CriticalSystemException;
import com.challengeteam.shop.persistence.repository.MIMETypeRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.MediaType;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.challengeteam.shop.service.impl.MIMETypeServiceImplTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith({MockitoExtension.class})
class MIMETypeServiceImplTest {
    @Mock
    private MIMETypeRepository mimeTypeRepository;
    @InjectMocks
    private MIMETypeServiceImpl mimeTypeService;

    @Nested
    class CreateIfDoesntExists {

        @Test
        void whenTypeExists_thenReturnMimeType() {
            // mockito
            Mockito.when(mimeTypeRepository.findByExtension(EXTENSION))
                    .thenReturn(Optional.of(buildMIMEType()));

            // when
            MIMEType result = mimeTypeService.createIfDoesntExist(buildMultipartFile());

            // then
            assertThat(result).isEqualTo(buildMIMEType());
        }

        @Test
        void whenTypeDoesntExists_thenCreateAndReturnMIMEType() {
            // mockito
            Mockito.when(mimeTypeRepository.findByExtension(EXTENSION))
                    .thenReturn(Optional.empty());
            Mockito.when(mimeTypeRepository.save(buildMIMETypeWithoutId()))
                    .thenReturn(buildMIMEType());

            // when
            MIMEType result = mimeTypeService.createIfDoesntExist(buildMultipartFile());

            // then
            assertThat(result).isEqualTo(buildMIMEType());
        }

        @Test
        void whenRaceConditionOccursToCreateType_thenReturnMIMEType() {
            // mockito
            var raceConditionFlag = new AtomicBoolean();
            Mockito.when(mimeTypeRepository.save(buildMIMETypeWithoutId()))
                    .then(invocation -> {
                        raceConditionFlag.set(true);
                        throw new DataIntegrityViolationException("");
                    });
            Mockito.when(mimeTypeRepository.findByExtension(EXTENSION))
                    .then(invocation -> {
                        if (raceConditionFlag.get()) {
                            return Optional.of(buildMIMEType());
                        } else {
                            return Optional.empty();
                        }
                    });

            // when
            MIMEType result = mimeTypeService.createIfDoesntExist(buildMultipartFile());

            // then
            assertThat(result).isEqualTo(buildMIMEType());
        }

        @Test
        void whenRaceConditionOccursToCreateTypeAndNotFoundMIMETypeAfterException_thenThrowSystemCriticalException() {
            // mockito
            Mockito.when(mimeTypeRepository.save(buildMIMETypeWithoutId()))
                    .thenThrow(DataIntegrityViolationException.class);
            Mockito.when(mimeTypeRepository.findByExtension(EXTENSION))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> mimeTypeService.createIfDoesntExist(buildMultipartFile()))
                    .isInstanceOf(CriticalSystemException.class);
        }

        @Test
        void whenParameterMultipartFileIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> mimeTypeService.createIfDoesntExist(null))
                    .isInstanceOf(NullPointerException.class);
        }

    }

    static class TestResources {
        static final long ID = 10L;
        static final String EXTENSION = "jpg";
        static final String CONTENT_TYPE = MediaType.IMAGE_JPEG.toString();

        static final String FILE_NAME = "image.jpg";
        static final byte[] CONTENT = buildContent();

        private static byte[] buildContent() {
            return FILE_NAME.getBytes();
        }

        static MultipartFile buildMultipartFile() {
            return new MockMultipartFile(
                    "name",
                    FILE_NAME,
                    CONTENT_TYPE,
                    CONTENT
            );
        }

        static MIMEType buildMIMEType() {
            MIMEType mimeType = buildMIMETypeWithoutId();
            mimeType.setId(ID);

            return mimeType;
        }

        static MIMEType buildMIMETypeWithoutId() {
            return MIMEType.builder()
                    .type(CONTENT_TYPE)
                    .extension(EXTENSION)
                    .build();
        }
    }

}