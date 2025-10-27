package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.phone.PhoneCreateRequestDto;
import com.challengeteam.shop.dto.phone.PhoneUpdateRequestDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.service.impl.merger.PhoneMerger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class PhoneServiceImplTest {

    @Mock
    private PhoneRepository phoneRepository;

    @Mock
    private PhoneMerger phoneMerger;

    private PhoneServiceImpl phoneService;

    @BeforeEach
    public void initBeforeEach() {
        phoneService = new PhoneServiceImpl(phoneRepository, phoneMerger);
    }

    @Nested
    class GetAllTest {

        @Test
        void whenPhonesExist_thenReturnAll() {
            // given
            List<Phone> phones = buildPhonesFromTo(1, 11);
            List<Phone> expected = buildPhonesFromTo(1, 11);

            // mockito
            Mockito.when(phoneRepository.findAll()).thenReturn(phones);

            // when
            List<Phone> result = phoneService.getAll();

            // then
            assertNotNull(result);
            assertEquals(expected, result);
        }

        @Test
        void whenNoPhonesExist_thenReturnEmptyList() {
            // given
            List<Phone> phones = List.of();
            List<Phone> expected = List.of();

            // mockito
            Mockito.when(phoneRepository.findAll()).thenReturn(phones);

            // when
            List<Phone> result = phoneService.getAll();

            // then
            assertNotNull(result);
            assertEquals(expected, result);
        }
    }

    @Nested
    class GetByIdTest {

        @Test
        void whenPhoneExists_thenReturnOptionalWithPhone() {
            // given
            Phone phone = buildPhone(10L);
            Phone expected = buildPhone(10L);

            // mockito
            Mockito.when(phoneRepository.findById(10L))
                    .thenReturn(Optional.of(phone));

            // when
            Optional<Phone> result = phoneService.getById(10L);

            // then
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(expected, result.get());
        }

        @Test
        void whenPhoneDoesNotExist_thenReturnEmptyOptional() {
            // given
            // ...

            // mockito
            Mockito.when(phoneRepository.findById(10L)).thenReturn(Optional.empty());

            // when
            Optional<Phone> result = phoneService.getById(10L);

            // then
            assertNotNull(result);
            assertFalse(result.isPresent());
        }

        @Test
        void whenParameterIdIsNull_thenThrowException() {
            // given
            Long id = null;

            // mockito
            // ...

            // when + then
            assertThrows(NullPointerException.class, () -> phoneService.getById(id));
        }
    }

    @Nested
    class CreateTest {

        @Test
        void whenDataIsValid_thenCreatePhoneAndReturnId() {
            // given
            String name = "name";
            String description = "description";
            BigDecimal price = BigDecimal.valueOf(100);
            String brand = "brand";
            Integer releaseYear = 2025;
            PhoneCreateRequestDto dto = new PhoneCreateRequestDto(
                    name,
                    description,
                    price,
                    brand,
                    releaseYear
            );

            // mockito
            Mockito.when(phoneRepository.save(any())).thenReturn(buildPhone(100L));

            // when
            Long result = phoneService.create(dto);

            // then
            assertNotNull(result);
            assertEquals(100L, result);

            // capture
            ArgumentCaptor<Phone> captor = ArgumentCaptor.forClass(Phone.class);
            Mockito.verify(phoneRepository).save(captor.capture());
            Phone beforeSave = captor.getValue();

            assertEquals(dto.name(), beforeSave.getName());
            assertEquals(dto.description(), beforeSave.getDescription());
            assertEquals(dto.price(), beforeSave.getPrice());
            assertEquals(dto.brand(), beforeSave.getBrand());
            assertEquals(dto.releaseYear(), beforeSave.getReleaseYear());
        }

        @Test
        void whenDtoIsNull_thenThrowException() {
            // given
            PhoneCreateRequestDto dto = null;

            // mockito
            // ...

            // when + then
            assertThrows(NullPointerException.class, () -> phoneService.create(dto));
        }
    }

    @Nested
    class UpdateTest {

        @Test
        void whenPhoneExists_thenUpdatePhone() {
            // given
            Long id = 10L;
            Phone phone = buildPhone(id);
            PhoneUpdateRequestDto dto = new PhoneUpdateRequestDto(
                    "newName",
                    "newDescription",
                    new BigDecimal("19999"),
                    "newBrand",
                    2025
            );

            // mockito
            Mockito.when(phoneRepository.findById(id))
                    .thenReturn(Optional.of(phone));

            // when
            phoneService.update(id, dto);

            // then
            Mockito.verify(phoneRepository).findById(id);
            Mockito.verify(phoneMerger).mergePhone(phone, dto);
            Mockito.verify(phoneRepository).save(phone);
            Mockito.verifyNoMoreInteractions(phoneRepository, phoneMerger);
        }

        @Test
        void whenPhoneDoesNotExist_thenThrowException() {
            // given
            Long id = 10L;
            PhoneUpdateRequestDto dto = new PhoneUpdateRequestDto(
                    "newName",
                    "newDescription",
                    new BigDecimal("19999"),
                    "newBrand",
                    2025
            );

            // mockito
            Mockito.when(phoneRepository.findById(id)).thenReturn(Optional.empty());

            // when + then
            assertThrows(ResourceNotFoundException.class, () -> phoneService.update(id, dto));
        }

        @Test
        void whenIdIsNull_thenThrowException() {
            // given
            Long id = null;
            PhoneUpdateRequestDto dto = new PhoneUpdateRequestDto(
                    "newName",
                    "newDescription",
                    new BigDecimal("19999"),
                    "newBrand",
                    2025
            );

            // mockito
            // ...

            // when + then
            assertThrows(NullPointerException.class, () -> phoneService.update(id, dto));
        }

        @Test
        void whenDtoIsNull_thenThrowException() {
            // given
            Long id = 10L;
            PhoneUpdateRequestDto dto = null;

            // mockito
            // ...

            // when + then
            assertThrows(NullPointerException.class, () -> phoneService.update(id, dto));
        }
    }

    @Nested
    class DeleteTest {

        @Test
        void whenCalled_thenReturnNothing() {
            // given
            Long id = 10L;

            // mockito
            // ...

            // when
            phoneService.delete(id);

            // then
            Mockito.verify(phoneRepository).deleteById(id);
            Mockito.verifyNoMoreInteractions(phoneRepository);
        }

        @Test
        void whenIdIsNull_thenThrowException() {
            // given
            Long id = null;

            // mockito
            // ...

            // when + then
            assertThrows(NullPointerException.class, () -> phoneService.delete(id));
        }
    }

    private List<Phone> buildPhonesFromTo(long from, long to) {
        return LongStream.range(from, to)
                .mapToObj(this::buildPhone)
                .toList();
    }

    private Phone buildPhone(Long id) {
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
}
