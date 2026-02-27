package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.pagination.PhoneFilterDto;
import com.challengeteam.shop.dto.phone.PhoneCreateRequestDto;
import com.challengeteam.shop.dto.phone.PhoneUpdateRequestDto;
import com.challengeteam.shop.entity.image.Image;
import com.challengeteam.shop.entity.image.MIMEType;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.PhoneCharacteristics;
import com.challengeteam.shop.exceptionHandling.exception.CriticalSystemException;
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
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
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
import static org.mockito.ArgumentMatchers.anyList;

@ExtendWith(MockitoExtension.class)
class PhoneServiceImplTest {
    @Mock private PhoneRepository phoneRepository;
    @Mock private PhoneMerger phoneMerger;
    @Mock private ImageService imageService;
    @Mock private ImageRepository imageRepository;
    @InjectMocks private PhoneServiceImpl phoneService;

    @Nested
    class GetAllPhonesTest {

        @Test
        void whenPhonesExist_thenReturnPageWithPhones() {
            // given
            int page = 0;
            int size = 10;
            PhoneFilterDto filterDto = buildDefaultPhoneFilterDto();
            Sort expectedSort = Sort.by("name").ascending();
            Pageable pageable = PageRequest.of(page, size, expectedSort);
            List<Phone> phones = buildPhonesFromTo(1, 11);
            Page<Phone> expected = new PageImpl<>(phones, pageable, 20);

            // mockito
            Mockito.when(phoneRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(expected);

            // when
            Page<Phone> result = phoneService.getPhones(page, size, filterDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expected);
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getContent()).hasSize(10);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.getTotalElements()).isEqualTo(20);

            // capture
            ArgumentCaptor<Specification<Phone>> specCaptor = ArgumentCaptor.forClass(Specification.class);
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            Mockito.verify(phoneRepository).findAll(specCaptor.capture(), pageableCaptor.capture());

            assertThat(specCaptor.getValue()).isNotNull();
            assertThat(pageableCaptor.getValue()).isNotNull();
        }

        @Test
        void whenNoPhonesExist_thenReturnEmptyPage() {
            // given
            int page = 0;
            int size = 10;
            PhoneFilterDto filterDto = buildDefaultPhoneFilterDto();
            Sort expectedSort = Sort.by("name").ascending();
            Pageable pageable = PageRequest.of(page, size, expectedSort);
            Page<Phone> expected = new PageImpl<>(List.of(), pageable, 0);

            // mockito
            Mockito.when(phoneRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(expected);

            // when
            Page<Phone> result = phoneService.getPhones(page, size, filterDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expected);
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();

            // capture
            ArgumentCaptor<Specification<Phone>> specCaptor = ArgumentCaptor.forClass(Specification.class);
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            Mockito.verify(phoneRepository).findAll(specCaptor.capture(), pageableCaptor.capture());

            assertThat(specCaptor.getValue()).isNotNull();
            assertThat(pageableCaptor.getValue()).isNotNull();
        }

        @Test
        void whenLastPage_thenReturnRemainingPhones() {
            // given
            int page = 1;
            int size = 10;
            PhoneFilterDto filterDto = buildDefaultPhoneFilterDto();
            Sort expectedSort = Sort.by("name").ascending();
            Pageable pageable = PageRequest.of(page, size, expectedSort);
            List<Phone> phones = buildPhonesFromTo(11, 15);
            Page<Phone> expected = new PageImpl<>(phones, pageable, 14);

            // mockito
            Mockito.when(phoneRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(expected);

            // when
            Page<Phone> result = phoneService.getPhones(page, size, filterDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(4);
            assertThat(result.getTotalElements()).isEqualTo(14);
            assertThat(result.isLast()).isTrue();

            // capture
            ArgumentCaptor<Specification<Phone>> specCaptor = ArgumentCaptor.forClass(Specification.class);
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            Mockito.verify(phoneRepository).findAll(specCaptor.capture(), pageableCaptor.capture());

            assertThat(specCaptor.getValue()).isNotNull();
            assertThat(pageableCaptor.getValue()).isNotNull();
        }

        @Test
        void whenFirstPage_thenReturnFirstPageInfo() {
            // given
            int page = 0;
            int size = 5;
            PhoneFilterDto filterDto = buildDefaultPhoneFilterDto();
            Sort expectedSort = Sort.by("name").ascending();
            Pageable pageable = PageRequest.of(page, size, expectedSort);
            List<Phone> phones = buildPhonesFromTo(1, 6);
            Page<Phone> expected = new PageImpl<>(phones, pageable, 25);

            // mockito
            Mockito.when(phoneRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(expected);

            // when
            Page<Phone> result = phoneService.getPhones(page, size, filterDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isFalse();
            assertThat(result.hasNext()).isTrue();

            // capture
            ArgumentCaptor<Specification<Phone>> specCaptor = ArgumentCaptor.forClass(Specification.class);
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            Mockito.verify(phoneRepository).findAll(specCaptor.capture(), pageableCaptor.capture());

            assertThat(specCaptor.getValue()).isNotNull();
            assertThat(pageableCaptor.getValue()).isNotNull();
        }

        @Test
        void whenPageAndSizeAreValid_thenVerifyPageableArgument() {
            // given
            int page = 2;
            int size = 15;
            PhoneFilterDto filterDto = buildDefaultPhoneFilterDto();
            Sort expectedSort = Sort.by("name").ascending();
            Pageable pageable = PageRequest.of(page, size, expectedSort);
            List<Phone> phones = buildPhonesFromTo(1, 5);
            Page<Phone> expected = new PageImpl<>(phones, pageable, 50);

            // mockito
            Mockito.when(phoneRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(expected);

            // when
            phoneService.getPhones(page, size, filterDto);

            // then
            ArgumentCaptor<Specification<Phone>> specCaptor = ArgumentCaptor.forClass(Specification.class);
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            Mockito.verify(phoneRepository).findAll(specCaptor.capture(), pageableCaptor.capture());

            Pageable capturedPageable = pageableCaptor.getValue();
            assertThat(capturedPageable.getPageNumber()).isEqualTo(page);
            assertThat(capturedPageable.getPageSize()).isEqualTo(size);
        }

        @Test
        void whenSortByNameAsc_thenReturnSortedPhones() {
            // given
            int page = 0;
            int size = 10;
            PhoneFilterDto filterDto = buildDefaultPhoneFilterDto();
            Sort expectedSort = Sort.by("name").ascending();
            Pageable pageable = PageRequest.of(page, size, expectedSort);
            List<Phone> phones = buildPhonesFromTo(1, 6);
            Page<Phone> expected = new PageImpl<>(phones, pageable, 5);

            // mockito
            Mockito.when(phoneRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(expected);

            // when
            Page<Phone> result = phoneService.getPhones(page, size, filterDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(5);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            Mockito.verify(phoneRepository).findAll(any(Specification.class), pageableCaptor.capture());
            Pageable capturedPageable = pageableCaptor.getValue();

            assertThat(capturedPageable.getSort()).isEqualTo(expectedSort);
        }

        @Test
        void whenSortByNameDesc_thenReturnSortedPhones() {
            // given
            int page = 0;
            int size = 10;
            PhoneFilterDto filterDto = buildPhoneFilterDtoWithSortByNameDesc();
            Sort expectedSort = Sort.by("name").descending();
            Pageable pageable = PageRequest.of(page, size, expectedSort);
            List<Phone> phones = buildPhonesFromTo(1, 6);
            Page<Phone> expected = new PageImpl<>(phones, pageable, 5);

            // mockito
            Mockito.when(phoneRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(expected);

            // when
            Page<Phone> result = phoneService.getPhones(page, size, filterDto);

            // then
            assertThat(result).isNotNull();

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            Mockito.verify(phoneRepository).findAll(any(Specification.class), pageableCaptor.capture());
            Pageable capturedPageable = pageableCaptor.getValue();

            assertThat(capturedPageable.getSort()).isEqualTo(expectedSort);
        }

        @Test
        void whenSortByPriceAsc_thenReturnSortedPhones() {
            // given
            int page = 0;
            int size = 10;
            PhoneFilterDto filterDto = buildPhoneFilterDtoWithSortByPriceAsc();
            Sort expectedSort = Sort.by("price").ascending();
            Pageable pageable = PageRequest.of(page, size, expectedSort);
            List<Phone> phones = buildPhonesFromTo(1, 6);
            Page<Phone> expected = new PageImpl<>(phones, pageable, 5);

            // mockito
            Mockito.when(phoneRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(expected);

            // when
            Page<Phone> result = phoneService.getPhones(page, size, filterDto);

            // then
            assertThat(result).isNotNull();

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            Mockito.verify(phoneRepository).findAll(any(Specification.class), pageableCaptor.capture());
            Pageable capturedPageable = pageableCaptor.getValue();

            assertThat(capturedPageable.getSort()).isEqualTo(expectedSort);
        }

        @Test
        void whenSortByPriceDesc_thenReturnSortedPhones() {
            // given
            int page = 0;
            int size = 10;
            PhoneFilterDto filterDto = buildPhoneFilterDtoWithSortByPriceDesc();
            Sort expectedSort = Sort.by("price").descending();
            Pageable pageable = PageRequest.of(page, size, expectedSort);
            List<Phone> phones = buildPhonesFromTo(1, 6);
            Page<Phone> expected = new PageImpl<>(phones, pageable, 5);

            // mockito
            Mockito.when(phoneRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(expected);

            // when
            Page<Phone> result = phoneService.getPhones(page, size, filterDto);

            // then
            assertThat(result).isNotNull();

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            Mockito.verify(phoneRepository).findAll(any(Specification.class), pageableCaptor.capture());
            Pageable capturedPageable = pageableCaptor.getValue();

            assertThat(capturedPageable.getSort()).isEqualTo(expectedSort);
        }

        @Test
        void whenSortIsInvalid_thenDefaultToNameAsc() {
            // given
            int page = 0;
            int size = 10;
            PhoneFilterDto filterDto = buildPhoneFilterDtoWithInvalidSort();
            Sort expectedSort = Sort.by("name").ascending();
            Pageable pageable = PageRequest.of(page, size, expectedSort);
            List<Phone> phones = buildPhonesFromTo(1, 6);
            Page<Phone> expected = new PageImpl<>(phones, pageable, 5);

            // mockito
            Mockito.when(phoneRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(expected);

            // when
            Page<Phone> result = phoneService.getPhones(page, size, filterDto);

            // then
            assertThat(result).isNotNull();

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            Mockito.verify(phoneRepository).findAll(any(Specification.class), pageableCaptor.capture());
            Pageable capturedPageable = pageableCaptor.getValue();

            assertThat(capturedPageable.getSort()).isEqualTo(expectedSort);
        }

        @Test
        void whenFilterByBrand_thenPassSpecificationToRepository() {
            // given
            int page = 0;
            int size = 10;
            PhoneFilterDto filterDto = buildPhoneFilterDtoWithFilterByBrand();
            Sort expectedSort = Sort.by("name").ascending();
            Pageable pageable = PageRequest.of(page, size, expectedSort);
            List<Phone> phones = buildPhonesFromTo(1, 4);
            Page<Phone> expected = new PageImpl<>(phones, pageable, 3);

            // mockito
            Mockito.when(phoneRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(expected);

            // when
            Page<Phone> result = phoneService.getPhones(page, size, filterDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(3);
            Mockito.verify(phoneRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        void whenFilterByPriceRange_thenPassSpecificationToRepository() {
            // given
            int page = 0;
            int size = 10;
            PhoneFilterDto filterDto = buildPhoneFilterDtoWithFilterByPriceRange();
            Sort expectedSort = Sort.by("name").ascending();
            Pageable pageable = PageRequest.of(page, size, expectedSort);
            List<Phone> phones = buildPhonesFromTo(1, 6);
            Page<Phone> expected = new PageImpl<>(phones, pageable, 5);

            // mockito
            Mockito.when(phoneRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(expected);

            // when
            Page<Phone> result = phoneService.getPhones(page, size, filterDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(5);
            Mockito.verify(phoneRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        void whenFilterByMinPriceOnly_thenPassSpecificationToRepository() {
            // given
            int page = 0;
            int size = 10;
            PhoneFilterDto filterDto = buildPhoneFilterDtoWithFilterByMinPrice();
            Sort expectedSort = Sort.by("name").ascending();
            Pageable pageable = PageRequest.of(page, size, expectedSort);
            List<Phone> phones = buildPhonesFromTo(1, 4);
            Page<Phone> expected = new PageImpl<>(phones, pageable, 3);

            // mockito
            Mockito.when(phoneRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(expected);

            // when
            Page<Phone> result = phoneService.getPhones(page, size, filterDto);

            // then
            assertThat(result).isNotNull();
            Mockito.verify(phoneRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        void whenFilterByMaxPriceOnly_thenPassSpecificationToRepository() {
            // given
            int page = 0;
            int size = 10;
            PhoneFilterDto filterDto = buildPhoneFilterDtoWithFilterByMaxPrice();
            Sort expectedSort = Sort.by("name").ascending();
            Pageable pageable = PageRequest.of(page, size, expectedSort);
            List<Phone> phones = buildPhonesFromTo(1, 8);
            Page<Phone> expected = new PageImpl<>(phones, pageable, 7);

            // mockito
            Mockito.when(phoneRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(expected);

            // when
            Page<Phone> result = phoneService.getPhones(page, size, filterDto);

            // then
            assertThat(result).isNotNull();
            Mockito.verify(phoneRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        void whenFilterByBrandAndPriceRangeWithSort_thenApplyAllFilters() {
            // given
            int page = 0;
            int size = 10;
            PhoneFilterDto filterDto = buildPhoneFilterDtoWithSortAndFilterByPriceRangAndByBrand();
            Sort expectedSort = Sort.by("price").descending();
            Pageable pageable = PageRequest.of(page, size, expectedSort);
            List<Phone> phones = buildPhonesFromTo(1, 4);
            Page<Phone> expected = new PageImpl<>(phones, pageable, 3);

            // mockito
            Mockito.when(phoneRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(expected);

            // when
            Page<Phone> result = phoneService.getPhones(page, size, filterDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(3);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            Mockito.verify(phoneRepository).findAll(any(Specification.class), pageableCaptor.capture());
            Pageable capturedPageable = pageableCaptor.getValue();

            assertThat(capturedPageable.getSort()).isEqualTo(expectedSort);
            assertThat(capturedPageable.getPageNumber()).isEqualTo(page);
            assertThat(capturedPageable.getPageSize()).isEqualTo(size);
        }

    }

    @Nested
    class GetByIdTest {

        @Test
        void whenPhoneExistsWithImages_thenReturnOptionalWithPhone() {
            // given
            Phone phone = buildPhone(PHONE_ID);
            phone.setImages(new ArrayList<>(List.of(buildImage())));
            List<Image> expectedImages = List.copyOf(phone.getImages());

            // mockito
            Mockito.when(phoneRepository.findByIdWithImages(PHONE_ID))
                    .thenReturn(Optional.of(phone));

            // when
            Optional<Phone> result = phoneService.getById(PHONE_ID);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(phone);
            assertThat(result.get().getImages()).isNotNull().hasSize(expectedImages.size())
                    .containsExactlyElementsOf(expectedImages);
            Mockito.verify(phoneRepository).findByIdWithImages(PHONE_ID);
        }

        @Test
        void whenPhoneExistsWithNoImages_thenReturnPhoneWithEmptyImageList() {
            // given
            Phone phone = buildPhone(PHONE_ID);
            phone.setImages(new ArrayList<>());

            // mockito
            Mockito.when(phoneRepository.findByIdWithImages(PHONE_ID))
                    .thenReturn(Optional.of(phone));

            // when
            Optional<Phone> result = phoneService.getById(PHONE_ID);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(phone);
            assertThat(result.get().getImages()).isNotNull().isEmpty();
            Mockito.verify(phoneRepository).findByIdWithImages(PHONE_ID);
        }

        @Test
        void whenPhoneDoesNotExist_thenReturnEmptyOptional() {
            // mockito
            Mockito.when(phoneRepository.findByIdWithImages(PHONE_ID)).thenReturn(Optional.empty());

            // when
            Optional<Phone> result = phoneService.getById(PHONE_ID);

            // then
            assertThat(result).isNotPresent();
            Mockito.verify(phoneRepository).findByIdWithImages(PHONE_ID);
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
                    .thenReturn(buildImage());

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
        void whenFieldsHaveWhitespaces_thenTrimBeforeSave() {
            // given
            PhoneCreateRequestDto dto = buildPhoneCreateRequestDtoWithWhitespaces();
            List<MultipartFile> files = List.of(buildMultipartFile());

            // mockito
            Mockito.when(phoneRepository.save(any(Phone.class)))
                    .thenReturn(buildPhone(PHONE_ID));
            Mockito.when(imageService.uploadImage(any(MultipartFile.class)))
                    .thenReturn(buildImage());

            // when
            phoneService.create(dto, files);

            // captor
            ArgumentCaptor<Phone> captor = ArgumentCaptor.forClass(Phone.class);
            Mockito.verify(phoneRepository).save(captor.capture());
            Phone forSave = captor.getValue();

            // then
            assertThat(forSave.getName()).isEqualTo(PHONE_NAME);
            assertThat(forSave.getDescription()).isEqualTo(dto.description());
            assertThat(forSave.getPrice()).isEqualTo(dto.price());
            assertThat(forSave.getBrand()).isEqualTo(PHONE_BRAND);
            assertThat(forSave.getReleaseYear()).isEqualTo(dto.releaseYear());
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
                    .thenReturn(buildImage());

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

    @Nested
    class DeletePhonesImageByIdTest {

        @Test
        void whenPhoneExistsAndContainsImage_thenDeleteImage() {
            // mockito
            Mockito.when(phoneRepository.existsPhoneByIdWithImage(PHONE_ID, IMAGE_ID))
                    .thenReturn(true);

            // when
            phoneService.deletePhonesImageById(PHONE_ID, IMAGE_ID);

            // then
            Mockito.verify(imageService).deleteImage(IMAGE_ID);
        }

        @Test
        void whenExistsButDoesntContainImage_thenThrowException() {
            // mockito
            Mockito.when(phoneRepository.existsPhoneByIdWithImage(PHONE_ID, IMAGE_ID))
                        .thenReturn(false);

            // when + then
            assertThatThrownBy(() -> phoneService.deletePhonesImageById(PHONE_ID, IMAGE_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void whenDoesntExist_thenThrowException() {
            // mockito
            Mockito.when(phoneRepository.existsPhoneByIdWithImage(PHONE_ID, IMAGE_ID))
                    .thenReturn(false);

            // when + then
            assertThatThrownBy(() -> phoneService.deletePhonesImageById(PHONE_ID, IMAGE_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void whenNotFoundImageAfterVerifying_thenThrowException() {
            // mockito
            Mockito.when(phoneRepository.existsPhoneByIdWithImage(PHONE_ID, IMAGE_ID))
                    .thenReturn(true);
            Mockito.doThrow(ResourceNotFoundException.class)
                    .when(imageService)
                    .deleteImage(IMAGE_ID);

            // when + then
            assertThatThrownBy(() -> phoneService.deletePhonesImageById(PHONE_ID, IMAGE_ID))
                    .isInstanceOf(CriticalSystemException.class);
        }

        @Test
        void whenParameterPhoneIdIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> phoneService.deletePhonesImageById(null, IMAGE_ID))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void whenParameterImageIdIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> phoneService.deletePhonesImageById(PHONE_ID, null))
                    .isInstanceOf(NullPointerException.class);

        }

    }

    static class TestResources {
        public static final Long IMAGE_ID = 10L;
        public static final String FILENAME = "file";
        public static final String ORIGINAL_FILENAME = "file.jpeg";
        public static final String CONTENT_TYPE = "image/jpeg";
        public static final byte[] CONTENT = ORIGINAL_FILENAME.getBytes();

        public static final Long PHONE_ID = 100L;
        public static final String PHONE_NAME = "phone_name";
        public static final String PHONE_DESCRIPTION = "Phone description.";
        public static final BigDecimal PHONE_PRICE = new BigDecimal("1000.0");
        public static final String PHONE_BRAND = "phone_brand";
        public static final int PHONE_RELEASE_YEAR = 2020;
        public static final String PHONE_CPU = "Snapdragon 8 Gen 2";
        public static final Integer PHONE_CORES_NUMBER = 8;
        public static final String PHONE_SCREEN_SIZE = "6.5\"";
        public static final String PHONE_FRONT_CAMERA = "12 MP";
        public static final String PHONE_MAIN_CAMERA = "50-12 MP";
        public static final String PHONE_BATTERY_CAPACITY = "4500 mAh";

        public static final String NEW_PHONE_NAME = "new_phone_name";
        public static final String NEW_PHONE_DESCRIPTION = "New phone description.";
        public static final BigDecimal NEW_PHONE_PRICE = new BigDecimal("2000.0");
        public static final String NEW_PHONE_BRAND = "new_phone_brand";
        public static final int NEW_PHONE_RELEASE_YEAR = 2021;
        public static final String NEW_PHONE_CPU = "Snapdragon 8 Gen 3";
        public static final Integer NEW_PHONE_CORES_NUMBER = 12;
        public static final String NEW_PHONE_SCREEN_SIZE = "6.7\"";
        public static final String NEW_PHONE_FRONT_CAMERA = "16 MP";
        public static final String NEW_PHONE_MAIN_CAMERA = "50-50-12 MP";
        public static final String NEW_PHONE_BATTERY_CAPACITY = "5000 mAh";


        static List<Phone> buildPhonesFromTo(long from, long to) {
            return LongStream.range(from, to)
                    .mapToObj(TestResources::buildPhone)
                    .toList();
        }

        static Phone buildPhone(Long id) {
            Phone phone = Phone.builder()
                    .name(PHONE_NAME + id)
                    .description(PHONE_DESCRIPTION + id)
                    .price(PHONE_PRICE.add(new BigDecimal(id)))
                    .brand(PHONE_BRAND + id)
                    .releaseYear(PHONE_RELEASE_YEAR)
                    .phoneCharacteristics(
                            PhoneCharacteristics.builder()
                                    .cpu(PHONE_CPU)
                                    .coresNumber(PHONE_CORES_NUMBER)
                                    .screenSize(PHONE_SCREEN_SIZE)
                                    .frontCamera(PHONE_FRONT_CAMERA)
                                    .mainCamera(PHONE_MAIN_CAMERA)
                                    .batteryCapacity(PHONE_BATTERY_CAPACITY)
                                    .build()
                    )
                    .build();
            phone.setId(id);
            phone.setCreatedAt(Instant.now());
            return phone;
        }


        static PhoneCreateRequestDto buildPhoneCreateRequestDto() {
            return new PhoneCreateRequestDto(
                    PHONE_NAME,
                    PHONE_DESCRIPTION,
                    PHONE_PRICE,
                    PHONE_BRAND,
                    PHONE_RELEASE_YEAR,
                    PHONE_CPU,
                    PHONE_CORES_NUMBER,
                    PHONE_SCREEN_SIZE,
                    PHONE_FRONT_CAMERA,
                    PHONE_MAIN_CAMERA,
                    PHONE_BATTERY_CAPACITY
            );
        }

        static PhoneCreateRequestDto buildPhoneCreateRequestDtoWithWhitespaces() {
            return new PhoneCreateRequestDto(
                    "  " + PHONE_NAME + "  ",
                    "  " + PHONE_DESCRIPTION + "  ",
                    PHONE_PRICE,
                    "  " + PHONE_BRAND + "  ",
                    PHONE_RELEASE_YEAR,
                    "  " + PHONE_CPU + "  ",
                    PHONE_CORES_NUMBER,
                    "  " + PHONE_SCREEN_SIZE + "  ",
                    "  " + PHONE_FRONT_CAMERA + "  ",
                    "  " + PHONE_MAIN_CAMERA + "  ",
                    "  " + PHONE_BATTERY_CAPACITY + "  "
            );
        }

        static PhoneUpdateRequestDto buildPhoneUpdateRequestDto() {
            return new PhoneUpdateRequestDto(
                    NEW_PHONE_NAME,
                    NEW_PHONE_DESCRIPTION,
                    NEW_PHONE_PRICE,
                    NEW_PHONE_BRAND,
                    NEW_PHONE_RELEASE_YEAR,
                    NEW_PHONE_CPU,
                    NEW_PHONE_CORES_NUMBER,
                    NEW_PHONE_SCREEN_SIZE,
                    NEW_PHONE_FRONT_CAMERA,
                    NEW_PHONE_MAIN_CAMERA,
                    NEW_PHONE_BATTERY_CAPACITY
            );
        }

        static PhoneFilterDto buildDefaultPhoneFilterDto() {
            return new PhoneFilterDto(
                    null,
                    null,
                    null,
                    null
            );
        }

        static PhoneFilterDto buildPhoneFilterDtoWithSortByNameDesc() {
            return new PhoneFilterDto(
                    null,
                    null,
                    null,
                    "name_desc"
            );
        }

        static PhoneFilterDto buildPhoneFilterDtoWithSortByPriceAsc() {
            return new PhoneFilterDto(
                    null,
                    null,
                    null,
                    "price_asc"
            );
        }

        static PhoneFilterDto buildPhoneFilterDtoWithSortByPriceDesc() {
            return new PhoneFilterDto(
                    null,
                    null,
                    null,
                    "price_desc"
            );
        }

        static PhoneFilterDto buildPhoneFilterDtoWithInvalidSort() {
            return new PhoneFilterDto(
                    null,
                    null,
                    null,
                    "invalid_sort"
            );
        }

        static PhoneFilterDto buildPhoneFilterDtoWithFilterByBrand() {
            return new PhoneFilterDto(
                    "Apple",
                    null,
                    null,
                    "name_asc"
            );
        }

        static PhoneFilterDto buildPhoneFilterDtoWithFilterByPriceRange() {
            return new PhoneFilterDto(
                    null,
                    new BigDecimal("10000.0"),
                    new BigDecimal("30000.0"),
                    "name_asc"
            );
        }

        static PhoneFilterDto buildPhoneFilterDtoWithFilterByMinPrice() {
            return new PhoneFilterDto(
                    null,
                    new BigDecimal("10000.0"),
                    null,
                    "name_asc"
            );
        }

        static PhoneFilterDto buildPhoneFilterDtoWithFilterByMaxPrice() {
            return new PhoneFilterDto(
                    null,
                    null,
                    new BigDecimal("30000.0"),
                    "name_asc"
            );
        }

        static PhoneFilterDto buildPhoneFilterDtoWithSortAndFilterByPriceRangAndByBrand() {
            return new PhoneFilterDto(
                    "Apple",
                    new BigDecimal("10000.0"),
                    new BigDecimal("30000.0"),
                    "price_desc"
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
                    .id(IMAGE_ID)
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