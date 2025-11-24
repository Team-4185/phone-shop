package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.phone.PhoneCreateRequestDto;
import com.challengeteam.shop.dto.phone.PhoneUpdateRequestDto;
import com.challengeteam.shop.entity.image.Image;
import com.challengeteam.shop.entity.image.MIMEType;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.persistence.repository.ImageRepository;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.service.ImageService;
import com.challengeteam.shop.service.impl.merger.PhoneMerger;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static com.challengeteam.shop.service.impl.PhoneServiceImplTest.TestResources.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class PhoneServiceImplTest {
    @Mock private PhoneRepository phoneRepository;
    @Mock private PhoneMerger phoneMerger;
    @Mock private ImageService imageService;
    @Mock private ImageRepository imageRepository;
    @InjectMocks private PhoneServiceImpl phoneService;

    @Nested
    class TetPhonesTest {

        @Test
        void whenPhonesExist_thenReturnPageWithPhones() {
            // given
            int page = 0;
            int size = 10;
            Pageable pageable = PageRequest.of(page, size);
            List<Phone> phones = buildPhonesFromTo(1, 11);
            Page<Phone> expected = new PageImpl<>(phones, pageable, 20);

            // mockito
            Mockito.when(phoneRepository.findAll(pageable)).thenReturn(expected);

            // when
            Page<Phone> result = phoneService.getPhones(page, size);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expected);
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getContent()).hasSize(10);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getTotalElements()).isEqualTo(20);
            Mockito.verify(phoneRepository).findAll(pageable);
        }

        @Test
        void whenNoPhonesExist_thenReturnEmptyPage() {
            // given
            int page = 0;
            int size = 10;
            Pageable pageable = PageRequest.of(page, size);
            Page<Phone> expected = new PageImpl<>(List.of(), pageable, 0);

            // mockito
            Mockito.when(phoneRepository.findAll(pageable)).thenReturn(expected);

            // when
            Page<Phone> result = phoneService.getPhones(page, size);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expected);
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            Mockito.verify(phoneRepository).findAll(pageable);
        }

        @Test
        void whenLastPage_thenReturnRemainingPhones() {
            // given
            int page = 1;
            int size = 10;
            Pageable pageable = PageRequest.of(page, size);
            List<Phone> phones = buildPhonesFromTo(11, 15);
            Page<Phone> expected = new PageImpl<>(phones, pageable, 14);

            // mockito
            Mockito.when(phoneRepository.findAll(pageable)).thenReturn(expected);

            // when
            Page<Phone> result = phoneService.getPhones(page, size);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(4);
            assertThat(result.getTotalElements()).isEqualTo(14);
            assertThat(result.isLast()).isTrue();
            Mockito.verify(phoneRepository).findAll(pageable);
        }

        @Test
        void whenFirstPage_thenReturnFirstPageInfo() {
            // given
            int page = 0;
            int size = 5;
            Pageable pageable = PageRequest.of(page, size);
            List<Phone> phones = buildPhonesFromTo(1, 6);
            Page<Phone> expected = new PageImpl<>(phones, pageable, 25);

            // mockito
            Mockito.when(phoneRepository.findAll(pageable)).thenReturn(expected);

            // when
            Page<Phone> result = phoneService.getPhones(page, size);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isFalse();
            assertThat(result.hasNext()).isTrue();
            Mockito.verify(phoneRepository).findAll(pageable);
        }

        @Test
        void whenPageAndSizeAreValid_thenVerifyPageableArgument() {
            // given
            int page = 2;
            int size = 15;
            Pageable pageable = PageRequest.of(page, size);
            List<Phone> phones = buildPhonesFromTo(1, 5);
            Page<Phone> expected = new PageImpl<>(phones, pageable, 50);

            // mockito
            Mockito.when(phoneRepository.findAll(any(Pageable.class))).thenReturn(expected);

            // when
            phoneService.getPhones(page, size);

            // then
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            Mockito.verify(phoneRepository).findAll(captor.capture());
            Pageable capturedPageable = captor.getValue();

            assertThat(capturedPageable.getPageNumber()).isEqualTo(page);
            assertThat(capturedPageable.getPageSize()).isEqualTo(size);
        }

    }

    @Nested
    class GetByIdTest {

        @Test
        void whenPhoneExists_thenReturnOptionalWithPhone() {
            // given
            Phone phone = buildPhone(PHONE_ID);

            // mockito
            Mockito.when(phoneRepository.findById(PHONE_ID))
                    .thenReturn(Optional.of(phone));

            // when
            Optional<Phone> result = phoneService.getById(PHONE_ID);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(phone);
            Mockito.verify(phoneRepository).findById(PHONE_ID);
        }

        @Test
        void whenPhoneDoesNotExist_thenReturnEmptyOptional() {
            // mockito
            Mockito.when(phoneRepository.findById(PHONE_ID)).thenReturn(Optional.empty());

            // when
            Optional<Phone> result = phoneService.getById(PHONE_ID);

            // then
            assertThat(result).isNotPresent();
            Mockito.verify(phoneRepository).findById(PHONE_ID);
        }

        @Test
        void whenParameterIdIsNull_thenThrowNullPointerException() {
            // when + then
            assertThatThrownBy(() -> phoneService.getById(null))
                    .isInstanceOf(NullPointerException.class);
        }

    }

    @Nested
    class CreateTest {

        @Test
        void whenDataIsValid_thenCreatePhoneAndReturnId() {
            // given
            PhoneCreateRequestDto dto = buildPhoneCreateRequestDto();
            List<MultipartFile> files = List.of(buildMultipartFile());
            Phone phone = buildPhone(PHONE_ID);

            // mockito
            Mockito.when(phoneRepository.save(any()))
                    .thenReturn(phone);
            Mockito.when(imageService.uploadImage(any(MultipartFile.class)))
                    .thenReturn(IMAGE_ID);
            Mockito.when(imageService.getImageById(IMAGE_ID))
                    .thenReturn(Optional.of(buildImage()));

            // when
            Long result = phoneService.create(dto, files);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(PHONE_ID);

            // capture Phone
            ArgumentCaptor<Phone> phoneCaptor = ArgumentCaptor.forClass(Phone.class);
            Mockito.verify(phoneRepository).save(phoneCaptor.capture());
            Phone phoneBeforeSave = phoneCaptor.getValue();

            assertThat(phoneBeforeSave.getName()).isEqualTo(dto.name());
            assertThat(phoneBeforeSave.getDescription()).isEqualTo(dto.description());
            assertThat(phoneBeforeSave.getPrice()).isEqualTo(dto.price());
            assertThat(phoneBeforeSave.getBrand()).isEqualTo(dto.brand());
            assertThat(phoneBeforeSave.getReleaseYear()).isEqualTo(dto.releaseYear());

            // capture Image
            ArgumentCaptor<Image> imageCaptor = ArgumentCaptor.forClass(Image.class);
            Mockito.verify(imageRepository).save(imageCaptor.capture());
            List<Image> imagesBeforeSave = imageCaptor.getAllValues();

            for (Image imageBeforeSave : imagesBeforeSave) {
                assertThat(imageBeforeSave.getPhone()).isEqualTo(phone);
            }
        }

        @Test
        void whenDtoIsNull_thenThrowNullPointerException() {
            // when + then
            assertThatThrownBy(() -> phoneService.create(null, new ArrayList<>()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void whenImagesIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> phoneService.create(buildPhoneCreateRequestDto(), null))
                    .isInstanceOf(NullPointerException.class);
        }

    }

    @Nested
    class UpdateTest {

        @Test
        void whenPhoneExists_thenUpdatePhone() {
            // given
            Phone phone = buildPhone(PHONE_ID);
            PhoneUpdateRequestDto dto = buildPhoneUpdateRequestDto();

            // mockito
            Mockito.when(phoneRepository.findById(PHONE_ID))
                    .thenReturn(Optional.of(phone));

            // when
            phoneService.update(PHONE_ID, dto);

            // then
            Mockito.verify(phoneRepository).findById(PHONE_ID);
            Mockito.verify(phoneMerger).mergePhone(phone, dto);
            Mockito.verify(phoneRepository).save(phone);
        }

        @Test
        void whenPhoneDoesNotExist_thenThrowResourceNotFoundException() {
            // mockito
            Mockito.when(phoneRepository.findById(PHONE_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> phoneService.update(PHONE_ID, buildPhoneUpdateRequestDto()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void whenIdIsNull_thenThrowNullPointerException() {
            // when + then
            assertThatThrownBy(() -> phoneService.update(null,  buildPhoneUpdateRequestDto()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void whenDtoIsNull_thenThrowNullPointerException() {
            // when + then
            assertThatThrownBy(() -> phoneService.update(PHONE_ID, null))
                    .isInstanceOf(NullPointerException.class);
        }

    }

    @Nested
    class DeleteTest {

        @Test
        void whenCalled_thenDeleteSuccessfully() {
            //mockito
            Mockito.when(phoneRepository.existsById(PHONE_ID)).thenReturn(true);

            // when
            phoneService.delete(PHONE_ID);

            // then
            Mockito.verify(phoneRepository).deleteById(PHONE_ID);
        }

        @Test
        void whenIdIsNull_thenThrowNullPointerException() {
            // when + then
            assertThatThrownBy(() -> phoneService.delete(null))
                    .isInstanceOf(NullPointerException.class);
        }

    }

    @Nested
    class AddImageToPhoneTest {

        @Test
        void whenPhoneExists_thenAddImage() {
            // mockito
            Mockito.when(phoneRepository.findById(PHONE_ID))
                    .thenReturn(Optional.of(buildPhone(PHONE_ID)));
            Mockito.when(imageService.uploadImage(any(MultipartFile.class)))
                    .thenReturn(IMAGE_ID);
            Mockito.when(imageService.getImageById(IMAGE_ID))
                    .thenReturn(Optional.of(buildImage()));

            // when
            phoneService.addImageToPhone(PHONE_ID, buildMultipartFile());

            // captor
            ArgumentCaptor<Image> captor = ArgumentCaptor.forClass(Image.class);
            Mockito.verify(imageRepository).save(captor.capture());
            Image beforeSave = captor.getValue();

            // then
            assertThat(beforeSave.getPhone()).isEqualTo(buildPhone(PHONE_ID));
            assertThat(beforeSave.getName()).isEqualTo(FILENAME);
            assertThat(beforeSave.getStorageKey()).isEqualTo(ORIGINAL_FILENAME);
            assertThat(beforeSave.getMimeType()).isEqualTo(buildMimeType());
            assertThat(beforeSave.getSize()).isEqualTo(CONTENT.length);
        }

        @Test
        void whenPhoneDoesntExists_thenThrowException() {
            // mockito
            Mockito.when(phoneRepository.findById(PHONE_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> phoneService.addImageToPhone(PHONE_ID, buildMultipartFile()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void whenParameterPhoneIdIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> phoneService.addImageToPhone(null, buildMultipartFile()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void whenParameterNewImageIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> phoneService.addImageToPhone(PHONE_ID,null))
                    .isInstanceOf(NullPointerException.class);
        }

    }

    @Nested
    class GetPhoneImagesTest {

        @Test
        void whenPhoneExists_thenReturnListOfImages() {
            // mockito
            Mockito.when(phoneRepository.findById(PHONE_ID))
                    .thenReturn(Optional.of(buildPhone(PHONE_ID)));
            Mockito.when(imageRepository.getImagesByPhone_Id(PHONE_ID))
                    .thenReturn(List.of(buildImage()));

            // when
            List<Image> result = phoneService.getPhoneImages(PHONE_ID);

            // then
            assertThat(result.size()).isEqualTo(1);
            assertThat(result.get(0)).isEqualTo(buildImage());
        }

        @Test
        void whenPhoneDoesntExist_thenThrowException() {
            // mockito
            Mockito.when(phoneRepository.findById(PHONE_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> phoneService.getPhoneImages(PHONE_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void whenParameterPhoneIdIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> phoneService.getPhoneImages(null))
                    .isInstanceOf(NullPointerException.class);
        }

    }

    static class TestResources {
        public static final Long PHONE_ID = 100L;

        public static final String FILENAME = "file";
        public static final String ORIGINAL_FILENAME = "file.jpeg";
        public static final String CONTENT_TYPE = "image/jpeg";
        public static final byte[] CONTENT = ORIGINAL_FILENAME.getBytes();

        public static final Long IMAGE_ID = 10L;

        static List<Phone> buildPhonesFromTo(long from, long to) {
            return LongStream.range(from, to)
                    .mapToObj(TestResources::buildPhone)
                    .toList();
        }

        static Phone buildPhone(Long id) {
            Phone phone = Phone.builder()
                    .name("name" + id)
                    .description("description" + id)
                    .price(BigDecimal.valueOf(1000 + id))
                    .brand("brand" + id)
                    .releaseYear(2025)
                    .build();
            phone.setId(id);
            phone.setCreatedAt(Instant.now());
            return phone;
        }

        static PhoneCreateRequestDto buildPhoneCreateRequestDto() {
            return new PhoneCreateRequestDto(
                    "name",
                    "description",
                    BigDecimal.valueOf(100),
                    "brand",
                    2025
            );
        }

        static PhoneUpdateRequestDto buildPhoneUpdateRequestDto() {
            return new PhoneUpdateRequestDto(
                    "newName",
                    "newDescription",
                    new BigDecimal("19999"),
                    "newBrand",
                    2025
            );
        }

        static MultipartFile buildMultipartFile() {
            return new MockMultipartFile(
                    FILENAME,
                    ORIGINAL_FILENAME,
                    CONTENT_TYPE,
                    CONTENT
            );
        }

        static Image buildImage() {
            return Image.builder()
                    .name(FILENAME)
                    .storageKey(ORIGINAL_FILENAME)
                    .size((long) CONTENT.length)
                    .mimeType(buildMimeType())
                    .build();
        }

        public static MIMEType buildMimeType() {
            return MIMEType.builder()
                    .id(10L)
                    .extension(".jpg")
                    .type("image/jpeg")
                    .build();
        }
    }
}