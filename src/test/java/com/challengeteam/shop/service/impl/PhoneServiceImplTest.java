package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.phone.PhoneCreateRequestDto;
import com.challengeteam.shop.dto.phone.PhoneUpdateRequestDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static com.challengeteam.shop.service.impl.PhoneServiceImplTest.TestResources.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class PhoneServiceImplTest {

    @Mock
    private PhoneRepository phoneRepository;

    @Mock
    private PhoneMerger phoneMerger;

    @InjectMocks
    private PhoneServiceImpl phoneService;

    @Nested
    class GetAllPhonesTest {

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
            Page<Phone> result = phoneService.getAllPhones(page, size);

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
            List<Phone> phones = List.of();
            Page<Phone> expected = new PageImpl<>(phones, pageable, 0);

            // mockito
            Mockito.when(phoneRepository.findAll(pageable)).thenReturn(expected);

            // when
            Page<Phone> result = phoneService.getAllPhones(page, size);

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
            Page<Phone> result = phoneService.getAllPhones(page, size);

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
            Page<Phone> result = phoneService.getAllPhones(page, size);

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
            phoneService.getAllPhones(page, size);

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
            Phone phone = buildPhone(10L);

            // mockito
            Mockito.when(phoneRepository.findById(10L))
                    .thenReturn(Optional.of(phone));

            // when
            Optional<Phone> result = phoneService.getById(10L);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(phone);
            Mockito.verify(phoneRepository).findById(10L);
        }

        @Test
        void whenPhoneDoesNotExist_thenReturnEmptyOptional() {
            // mockito
            Mockito.when(phoneRepository.findById(10L)).thenReturn(Optional.empty());

            // when
            Optional<Phone> result = phoneService.getById(10L);

            // then
            assertThat(result).isNotPresent();
            Mockito.verify(phoneRepository).findById(10L);
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

            // mockito
            Mockito.when(phoneRepository.save(any())).thenReturn(buildPhone(100L));

            // when
            Long result = phoneService.create(dto);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(100L);

            // capture
            ArgumentCaptor<Phone> captor = ArgumentCaptor.forClass(Phone.class);
            Mockito.verify(phoneRepository).save(captor.capture());
            Phone beforeSave = captor.getValue();

            assertThat(beforeSave.getName()).isEqualTo(dto.name());
            assertThat(beforeSave.getDescription()).isEqualTo(dto.description());
            assertThat(beforeSave.getPrice()).isEqualTo(dto.price());
            assertThat(beforeSave.getBrand()).isEqualTo(dto.brand());
            assertThat(beforeSave.getReleaseYear()).isEqualTo(dto.releaseYear());
        }

        @Test
        void whenDtoIsNull_thenThrowNullPointerException() {
            // when + then
            assertThatThrownBy(() -> phoneService.create(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class UpdateTest {

        @Test
        void whenPhoneExists_thenUpdatePhone() {
            // given
            Long id = 10L;
            Phone phone = buildPhone(id);
            PhoneUpdateRequestDto dto = buildPhoneUpdateRequestDto();

            // mockito
            Mockito.when(phoneRepository.findById(id))
                    .thenReturn(Optional.of(phone));

            // when
            phoneService.update(id, dto);

            // then
            Mockito.verify(phoneRepository).findById(id);
            Mockito.verify(phoneMerger).mergePhone(phone, dto);
            Mockito.verify(phoneRepository).save(phone);
        }

        @Test
        void whenPhoneDoesNotExist_thenThrowResourceNotFoundException() {
            // given
            Long id = 10L;
            PhoneUpdateRequestDto dto = buildPhoneUpdateRequestDto();

            // mockito
            Mockito.when(phoneRepository.findById(id)).thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> phoneService.update(id, dto))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void whenIdIsNull_thenThrowNullPointerException() {
            // given
            PhoneUpdateRequestDto dto = buildPhoneUpdateRequestDto();

            // when + then
            assertThatThrownBy(() -> phoneService.update(null, dto))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void whenDtoIsNull_thenThrowNullPointerException() {
            // given
            Long id = 10L;

            // when + then
            assertThatThrownBy(() -> phoneService.update(id, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class DeleteTest {

        @Test
        void whenCalled_thenDeleteSuccessfully() {
            // given
            Long id = 10L;

            // when
            phoneService.delete(id);

            // then
            Mockito.verify(phoneRepository).deleteById(id);
        }

        @Test
        void whenIdIsNull_thenThrowNullPointerException() {
            // when + then
            assertThatThrownBy(() -> phoneService.delete(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    static class TestResources {
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
    }
}