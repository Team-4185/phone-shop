package com.challengeteam.shop.service.pagination;

import com.challengeteam.shop.dto.pagination.paginationRequest.PhoneMultipleFilterRequest;
import com.challengeteam.shop.dto.pagination.paginationResponse.PageResponseDto;
import com.challengeteam.shop.dto.phone.response.PhoneColorResponseDto;
import com.challengeteam.shop.dto.phone.response.PhoneResponseDto;
import com.challengeteam.shop.dto.phone.response.StorageCapacityResponseDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.PhoneColor;
import com.challengeteam.shop.entity.phone.Phone_;
import com.challengeteam.shop.entity.phone.StorageCapacity;
import com.challengeteam.shop.mapper.phone.PhoneMapper;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.utility.pagination.filter.specefication.PhoneFilterSpecificationBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class PhoneFilteringAndSortingServiceTest {

    @Mock
    private PhoneRepository phoneRepository;
    @Mock
    private PhoneMapper phoneMapper;
    @InjectMocks
    private PhoneFilteringAndSortingServiceImpl phoneFilteringAndSortingServiceImpl;

    private static List<PhoneResponseDto> listOfPhonesWithPriceLowerThan300;
    private static List<PhoneResponseDto> listOfPhonesWithPriceGreater300;
    private static List<PhoneResponseDto> listOfPhonesWithPriceLower1000AndPriceHigherThan500;
    private static List<PhoneResponseDto> listOfApplePhonesBrand;
    private static List<PhoneResponseDto> listOfPhonesWithMultipleBrands;
    private static List<PhoneResponseDto> listOfPhonesWithStockIn;
    private static List<PhoneResponseDto> listOfPhonesWithPreOrder;
    private static Set<PhoneColorResponseDto> availablePhoneColors;
    private static Set<StorageCapacityResponseDto> availableStorageCapacities;


    private static List<PhoneResponseDto> emptyList;

    private static final String appleBrand = "Apple";
    private static final String samsungBrand = "Samsung";
    private static final String googleBrand = "Google";
    private static final String xiaomiBrand = "Xiaomi";
    private static final String onePlusBrand = "OnePlus";
    private static final String motorolaBrand = "Motorola";
    private static final String nokiaBrand = "Nokia";

    private static final Pageable page = PageRequest.of(0, 10);

    @BeforeAll

    static void init() {
        availablePhoneColors = Set.of(new PhoneColorResponseDto(PhoneColor.BLACK.name(), PhoneColor.BLACK.getDisplayName(), PhoneColor.BLACK.getHexCode()),
                new PhoneColorResponseDto(PhoneColor.BLUE.name(), PhoneColor.BLUE.getDisplayName(), PhoneColor.BLUE.getHexCode()),
                new PhoneColorResponseDto(PhoneColor.RED.name(), PhoneColor.RED.getDisplayName(), PhoneColor.RED.getHexCode()),
                new PhoneColorResponseDto(PhoneColor.GOLD.name(), PhoneColor.GOLD.getDisplayName(), PhoneColor.GOLD.getHexCode()),
                new PhoneColorResponseDto(PhoneColor.GRAY.name(), PhoneColor.GRAY.getDisplayName(), PhoneColor.GRAY.getHexCode()));
        availableStorageCapacities = Set.of(new StorageCapacityResponseDto(StorageCapacity.CAPACITY_128GB.name(), StorageCapacity.CAPACITY_128GB.getValue(), StorageCapacity.CAPACITY_128GB.getUnit()),
                new StorageCapacityResponseDto(StorageCapacity.CAPACITY_64GB.name(), StorageCapacity.CAPACITY_64GB.getValue(), StorageCapacity.CAPACITY_64GB.getUnit()),
                new StorageCapacityResponseDto(StorageCapacity.CAPACITY_512GB.name(), StorageCapacity.CAPACITY_512GB.getValue(), StorageCapacity.CAPACITY_512GB.getUnit()),
                new StorageCapacityResponseDto(StorageCapacity.CAPACITY_2TB.name(), StorageCapacity.CAPACITY_2TB.getValue(), StorageCapacity.CAPACITY_2TB.getUnit()));
        listOfPhonesWithPriceLowerThan300 = List.of(
                new PhoneResponseDto(1L, "Samsung Galaxy A14", "Budget smartphone", new BigDecimal("299.99"), samsungBrand, 2023, "Exynos 850", 8, "6.6\"", "13MP", "50MP", "5000mAh",
                        availablePhoneColors, availableStorageCapacities, null),
                new PhoneResponseDto(2L, "Nokia G22", "Affordable phone", new BigDecimal("199.99"), nokiaBrand, 2023, "Unisoc T606", 8, "6.5\"", "8MP", "50MP", "5050mAh",
                        availablePhoneColors, availableStorageCapacities, null),
                new PhoneResponseDto(3L, "Motorola Moto G13", "Entry-level device", new BigDecimal("179.99"), motorolaBrand, 2023, "MediaTek Helio G85", 8, "6.5\"", "8MP", "50MP", "5000mAh",
                        availablePhoneColors, availableStorageCapacities, null)
        );
        listOfPhonesWithPriceGreater300 = List.of(
                new PhoneResponseDto(4L, "Google Pixel 7a", "Mid-range phone", new BigDecimal("500.99"), googleBrand, 2023, "Google Tensor G2", 8, "6.1\"", "13MP", "64MP", "4385mAh",
                        availablePhoneColors, availableStorageCapacities, null),
                new PhoneResponseDto(5L, "Samsung Galaxy A54", "Mid-tier device", new BigDecimal("449.99"), samsungBrand, 2023, "Exynos 1380", 8, "6.4\"", "32MP", "50MP", "5000mAh", null, null, null),
                new PhoneResponseDto(6L, "OnePlus Nord N30", "Good value phone", new BigDecimal("399.99"), onePlusBrand, 2023, "Snapdragon 695", 8, "6.72\"", "16MP", "108MP", "5000mAh",
                        availablePhoneColors, availableStorageCapacities, null),
                new PhoneResponseDto(7L, "Xiaomi Redmi Note 12", "Budget-friendly", new BigDecimal("349.99"), xiaomiBrand, 2023, "Snapdragon 685", 8, "6.67\"", "13MP", "50MP", "5000mAh",
                        availablePhoneColors, availableStorageCapacities, null)
        );
        listOfPhonesWithPriceLower1000AndPriceHigherThan500 = List.of(
                new PhoneResponseDto(8L, "iPhone 14", "Apple flagship", new BigDecimal("799.99"), appleBrand, 2022, "A15 Bionic", 6, "6.1\"", "12MP", "12MP", "3279mAh",
                        availablePhoneColors, availableStorageCapacities, null),
                new PhoneResponseDto(9L, "Samsung Galaxy S23", "Premium Android", new BigDecimal("899.99"), samsungBrand, 2023, "Snapdragon 8 Gen 2", 8, "6.1\"", "12MP", "50MP", "3900mAh",
                        availablePhoneColors, availableStorageCapacities, null),
                new PhoneResponseDto(10L, "Google Pixel 8", "Latest Pixel", new BigDecimal("699.99"), googleBrand, 2023, "Google Tensor G3", 8, "6.2\"", "10.5MP", "50MP", "4575mAh",
                        availablePhoneColors, availableStorageCapacities, null),
                new PhoneResponseDto(11L, "OnePlus 11", "Flagship killer", new BigDecimal("699.99"), onePlusBrand, 2023, "Snapdragon 8 Gen 2", 8, "6.7\"", "16MP", "50MP", "5000mAh",
                        availablePhoneColors, availableStorageCapacities, null),
                new PhoneResponseDto(12L, "Xiaomi 13", "Premium phone", new BigDecimal("749.99"), xiaomiBrand, 2023, "Snapdragon 8 Gen 2", 8, "6.36\"", "32MP", "50MP", "4500mAh",
                        availablePhoneColors, availableStorageCapacities, null)
        );
        listOfApplePhonesBrand = List.of(
                new PhoneResponseDto(13L, "Apple iPhone 13", "Previous gen iPhone", new BigDecimal("599.99"), appleBrand, 2021, "A15 Bionic", 6, "6.1\"", "12MP", "12MP", "3240mAh",
                        availablePhoneColors, availableStorageCapacities, null),
                new PhoneResponseDto(14L, "Apple iPhone 14 Pro", "Pro model", new BigDecimal("999.99"), appleBrand, 2022, "A16 Bionic", 6, "6.1\"", "12MP", "48MP", "3200mAh",
                        availablePhoneColors, availableStorageCapacities, null),
                new PhoneResponseDto(15L, "Apple iPhone SE", "Compact iPhone", new BigDecimal("429.99"), appleBrand, 2022, "A15 Bionic", 6, "4.7\"", "7MP", "12MP", "2018mAh",
                        availablePhoneColors, availableStorageCapacities, null)
        );
        listOfPhonesWithMultipleBrands = List.of(
                new PhoneResponseDto(16L, "Samsung Galaxy Z Fold 5", "Foldable phone", new BigDecimal("1799.99"), samsungBrand, 2023, "Snapdragon 8 Gen 2", 8, "7.6\"", "10MP", "50MP", "4400mAh",
                        availablePhoneColors, availableStorageCapacities, null),
                new PhoneResponseDto(17L, "Google Pixel Fold", "Google's foldable", new BigDecimal("1799.99"), googleBrand, 2023, "Google Tensor G2", 8, "7.6\"", "9.5MP", "48MP", "4821mAh",
                        availablePhoneColors, availableStorageCapacities, null),
                new PhoneResponseDto(18L, "OnePlus Open", "OnePlus foldable", new BigDecimal("1699.99"), onePlusBrand, 2023, "Snapdragon 8 Gen 2", 8, "7.82\"", "32MP", "48MP", "4805mAh",
                        availablePhoneColors, availableStorageCapacities, null),
                new PhoneResponseDto(19L, "Motorola Razr 40", "Flip phone", new BigDecimal("699.99"), motorolaBrand, 2023, "Snapdragon 7 Gen 1", 8, "6.9\"", "32MP", "64MP", "4200mAh",
                        availablePhoneColors, availableStorageCapacities, null)
        );
        listOfPhonesWithStockIn = List.of(
                new PhoneResponseDto(20L, "Samsung Galaxy S23 Ultra", "In stock flagship", new BigDecimal("1199.99"), samsungBrand, 2023, "Snapdragon 8 Gen 2", 8, "6.8\"", "12MP", "200MP", "5000mAh",
                        availablePhoneColors, availableStorageCapacities, null),
                new PhoneResponseDto(21L, "iPhone 15", "Available now", new BigDecimal("799.99"), appleBrand, 2023, "A16 Bionic", 6, "6.1\"", "12MP", "48MP", "3349mAh",
                        availablePhoneColors, availableStorageCapacities, null),
                new PhoneResponseDto(22L, "Google Pixel 8 Pro", "Ready to ship", new BigDecimal("999.99"), googleBrand, 2023, "Google Tensor G3", 8, "6.7\"", "10.5MP", "50MP", "5050mAh",
                        availablePhoneColors, availableStorageCapacities, null),
                new PhoneResponseDto(23L, "Xiaomi 13 Pro", "In stock", new BigDecimal("999.99"), xiaomiBrand, 2023, "Snapdragon 8 Gen 2", 8, "6.73\"", "32MP", "50MP", "4820mAh",
                        availablePhoneColors, availableStorageCapacities, null)
        );
        listOfPhonesWithPreOrder = List.of(
                new PhoneResponseDto(24L, "Samsung Galaxy S24", "Pre-order available", new BigDecimal("899.99"), samsungBrand, 2024, "Snapdragon 8 Gen 3", 8, "6.2\"", "12MP", "50MP", "4000mAh",
                        availablePhoneColors, availableStorageCapacities, null),
                new PhoneResponseDto(25L, "iPhone 15 Pro Max", "Coming soon", new BigDecimal("1199.99"), appleBrand, 2023, "A17 Pro", 6, "6.7\"", "12MP", "48MP", "4422mAh",
                        availablePhoneColors, availableStorageCapacities, null),
                new PhoneResponseDto(26L, "OnePlus 12", "Pre-order now", new BigDecimal("799.99"), onePlusBrand, 2024, "Snapdragon 8 Gen 3", 8, "6.82\"", "32MP", "50MP", "5400mAh",
                        availablePhoneColors, availableStorageCapacities, null)
        );
        emptyList = Collections.emptyList();
    }


    /**
     * cases:
     * brandName was not found -> empty list;
     * request contains only one brandName -> return a list of phones with this brandName;
     * request contains multiple brands -> return a list of phones with all brands;
     */
    @Nested
    class FilterByBrandTest {

        @Test
        void whenBrandWasNotFound_thenReturnEmptyList() {
            PhoneMultipleFilterRequest filterDto = new PhoneMultipleFilterRequest(List.of("NoNameBrand"),
                    null, null, null, null, null);
            Specification<Phone> phoneSpecification = PhoneFilterSpecificationBuilder.build()
                    .add(filterDto.brands(), brands -> (root, query, builder)
                            -> root.get(Phone_.BRAND).in(brands))
                    .buildAnd();
            doReturn(Page.empty()).when(phoneRepository).findAll(any(Specification.class), any(Pageable.class));
            doReturn(emptyList).when(phoneMapper).toResponseList(anyList());

            PageResponseDto<PhoneResponseDto> responseDto = phoneFilteringAndSortingServiceImpl.filterAndSort(filterDto, page);

            assertNotNull(responseDto);
            assertTrue(responseDto.content().isEmpty());
            verify(phoneRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(phoneRepository);
        }


        @Test
        void whenFilterRequestContainsOnlyOneBrand_thanReturnListOfThisBrand() {
            PhoneMultipleFilterRequest filterDto = new PhoneMultipleFilterRequest(List.of(appleBrand),
                    null, null, null, null, null);
            Specification<Phone> phoneSpecification = PhoneFilterSpecificationBuilder.build()
                    .add(filterDto.brands(), brands -> (root, query, builder)
                            -> root.get(Phone_.BRAND).in(brands)).buildAnd();
            doReturn(Page.empty()).when(phoneRepository).findAll(any(Specification.class), any(Pageable.class));
            doReturn(listOfApplePhonesBrand).when(phoneMapper).toResponseList(anyList());


            PageResponseDto<PhoneResponseDto> responseDto = phoneFilteringAndSortingServiceImpl.filterAndSort(filterDto, page);

            assertNotNull(responseDto);
            assertEquals(listOfApplePhonesBrand.size(), responseDto.content().size());
            assertEquals(listOfApplePhonesBrand, responseDto.content());
            assertTrue(responseDto.content().containsAll(listOfApplePhonesBrand));
            verify(phoneRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(phoneRepository);
        }

        @Test
        void whenFilterRequestContainsMultipleBrands_thanReturnListOfPhonesWithAllBrands() {
            PhoneMultipleFilterRequest filterDto = new PhoneMultipleFilterRequest(List.of(appleBrand, samsungBrand, onePlusBrand, googleBrand),
                    null, null, null, null, null);
            Specification<Phone> phoneSpecification = PhoneFilterSpecificationBuilder.build()
                    .add(filterDto.brands(), brands -> (root, query, builder)
                            -> root.get(Phone_.BRAND).in(brands)).buildAnd();
            doReturn(Page.empty()).when(phoneRepository).findAll(any(Specification.class), any(Pageable.class));
            doReturn(listOfPhonesWithMultipleBrands).when(phoneMapper).toResponseList(anyList());

            PageResponseDto<PhoneResponseDto> responseDto = phoneFilteringAndSortingServiceImpl.filterAndSort(filterDto, page);

            assertNotNull(responseDto);
            assertEquals(listOfPhonesWithMultipleBrands.size(), responseDto.content().size());
            assertEquals(listOfPhonesWithMultipleBrands, responseDto.content());
            assertTrue(responseDto.content().containsAll(listOfPhonesWithMultipleBrands));
            verify(phoneRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(phoneRepository);
        }
    }

    /**
     * cases:
     * phones with min price were not found -> empty list;
     * request contains min price -> return a list of phones with this price;
     */
    @Nested
    class FilterByMinPriceTest {

        @Test
        void whenPhonesWithMinPriceWasNotFound_thenReturnEmptyList() {
            PhoneMultipleFilterRequest filterRequest = new PhoneMultipleFilterRequest(
                    null, BigDecimal.TEN, null, null, null, null);
            Specification<Phone> phoneSpecification = PhoneFilterSpecificationBuilder.build()
                    .add(filterRequest.minPrice(), minPrice -> (root, query, builder)
                            -> builder.greaterThanOrEqualTo(root.get(Phone_.price), minPrice))
                    .buildAnd();
            doReturn(Page.empty()).when(phoneRepository).findAll(any(Specification.class), any(Pageable.class));
            doReturn(emptyList).when(phoneMapper).toResponseList(anyList());

            PageResponseDto<PhoneResponseDto> responseDto = phoneFilteringAndSortingServiceImpl.filterAndSort(filterRequest, page);
            assertNotNull(responseDto);
            assertTrue(responseDto.content().isEmpty());
            verify(phoneRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(phoneRepository);
        }

        @Test
        void whenFilterRequestContainsMinPrice_thanReturnListOfPhonesWithMinPrice() {
            PhoneMultipleFilterRequest filterRequest = new PhoneMultipleFilterRequest(
                    null, BigDecimal.valueOf(300), null, null, null, null);
            Specification<Phone> phoneSpecification = PhoneFilterSpecificationBuilder.build()
                    .add(filterRequest.minPrice(), minPrice -> (root, query, builder)
                            -> builder.greaterThanOrEqualTo(root.get(Phone_.price), minPrice))
                    .buildAnd();
            doReturn(Page.empty()).when(phoneRepository).findAll(any(Specification.class), any(Pageable.class));
            doReturn(listOfPhonesWithPriceGreater300).when(phoneMapper).toResponseList(anyList());

            PageResponseDto<PhoneResponseDto> responseDto = phoneFilteringAndSortingServiceImpl.filterAndSort(filterRequest, page);
            assertNotNull(responseDto);
            assertEquals(listOfPhonesWithPriceGreater300.size(), responseDto.content().size());
            assertEquals(listOfPhonesWithPriceGreater300, responseDto.content());
            assertTrue(responseDto.content().containsAll(listOfPhonesWithPriceGreater300));
            verify(phoneRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(phoneRepository);
        }

    }

    /**
     * cases:
     * phones with max price were not found -> empty list;
     * request contains max price -> return a list of phones with this price;
     */
    @Nested
    class FilterByMaxPriceTest {
        @Test
        void whenPhonesWithMaxPriceWasNotFound_thenReturnEmptyList() {
            PhoneMultipleFilterRequest filterRequest = new PhoneMultipleFilterRequest(
                    null, null, BigDecimal.TEN, null, null, null);
            Specification<Phone> phoneSpecification = PhoneFilterSpecificationBuilder.build()
                    .add(filterRequest.maxPrice(), maxPrice -> (root, query, builder)
                            -> builder.lessThanOrEqualTo(root.get(Phone_.price), maxPrice))
                    .buildAnd();
            doReturn(Page.empty()).when(phoneRepository).findAll(any(Specification.class), any(Pageable.class));
            doReturn(emptyList).when(phoneMapper).toResponseList(anyList());

            PageResponseDto<PhoneResponseDto> responseDto = phoneFilteringAndSortingServiceImpl.filterAndSort(filterRequest, page);
            assertNotNull(responseDto);
            assertTrue(responseDto.content().isEmpty());
            verify(phoneRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(phoneRepository);
        }

        @Test
        void whenFilterRequestContainsMaxPrice_thanReturnListOfPhonesWithMaxPrice() {
            PhoneMultipleFilterRequest filterRequest = new PhoneMultipleFilterRequest(
                    null, null, BigDecimal.valueOf(300), null, null, null);
            Specification<Phone> phoneSpecification = PhoneFilterSpecificationBuilder.build()
                    .add(filterRequest.maxPrice(), maxPrice -> (root, query, builder)
                            -> builder.lessThanOrEqualTo(root.get(Phone_.price), maxPrice))
                    .buildAnd();
            doReturn(Page.empty()).when(phoneRepository).findAll(any(Specification.class), any(Pageable.class));
            doReturn(listOfPhonesWithPriceLowerThan300).when(phoneMapper).toResponseList(anyList());

            PageResponseDto<PhoneResponseDto> responseDto = phoneFilteringAndSortingServiceImpl.filterAndSort(filterRequest, page);
            assertNotNull(responseDto);
            assertEquals(listOfPhonesWithPriceLowerThan300.size(), responseDto.content().size());
            assertEquals(listOfPhonesWithPriceLowerThan300, responseDto.content());
            assertTrue(responseDto.content().containsAll(listOfPhonesWithPriceLowerThan300));
            verify(phoneRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(phoneRepository);
        }
    }

    /**
     * cases:
     * phones between lower and higher price were not found -> empty list;
     * request contains lower and higher price -> return a list of phones with this price;
     */
    @Nested
    class FilterBetweenLowerAndHigherPriceTest {

        @Test
        void whenPhonesWereNotFoundBetweenLowerAndHigherPrice_thanReturnEmptyList() {
            PhoneMultipleFilterRequest filterRequest = new PhoneMultipleFilterRequest(null, BigDecimal.TWO, BigDecimal.TEN,
                    null, null, null);
            Specification<Phone> phoneSpecification = PhoneFilterSpecificationBuilder.build()
                    .add(filterRequest.minPrice(), minPrice -> ((root, query, builder) ->
                            builder.greaterThanOrEqualTo(root.get(Phone_.price), minPrice)))
                    .add(filterRequest.maxPrice(), maxPrice -> (root, query, builder)
                            -> builder.lessThanOrEqualTo(root.get(Phone_.price), maxPrice))
                    .buildAnd();
            doReturn(Page.empty()).when(phoneRepository).findAll(any(Specification.class), any(Pageable.class));
            doReturn(emptyList).when(phoneMapper).toResponseList(anyList());

            PageResponseDto<PhoneResponseDto> responseDto = phoneFilteringAndSortingServiceImpl.filterAndSort(filterRequest, page);
            assertNotNull(responseDto);
            assertTrue(responseDto.content().isEmpty());
            verify(phoneRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(phoneRepository);
        }

        @Test
        void whenPhonesWereFoundBetweenLowerAndHigherPrice_thanReturnListOfPhones() {
            PhoneMultipleFilterRequest filterRequest = new PhoneMultipleFilterRequest(null, BigDecimal.valueOf(500),
                    BigDecimal.valueOf(1000), null, null, null);
            Specification<Phone> phoneSpecification = PhoneFilterSpecificationBuilder.build()
                    .add(filterRequest.minPrice(), minPrice -> ((root, query, builder) ->
                            builder.greaterThanOrEqualTo(root.get(Phone_.price), minPrice)))
                    .add(filterRequest.maxPrice(), maxPrice -> (root, query, builder)
                            -> builder.lessThanOrEqualTo(root.get(Phone_.price), maxPrice))
                    .buildAnd();
            doReturn(Page.empty()).when(phoneRepository).findAll(any(Specification.class), any(Pageable.class));
            doReturn(listOfPhonesWithPriceLower1000AndPriceHigherThan500).when(phoneMapper).toResponseList(anyList());

            PageResponseDto<PhoneResponseDto> responseDto = phoneFilteringAndSortingServiceImpl.filterAndSort(filterRequest, page);

            assertNotNull(responseDto);
            assertEquals(listOfPhonesWithPriceLower1000AndPriceHigherThan500.size(), responseDto.content().size());
            assertEquals(listOfPhonesWithPriceLower1000AndPriceHigherThan500, responseDto.content());
            assertTrue(responseDto.content().containsAll(listOfPhonesWithPriceLower1000AndPriceHigherThan500));
            verify(phoneRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(phoneRepository);
        }
    }

    /**
     * cases:
     * phones with stock were not found -> empty list;
     * phones with stock in -> return a list of phones with stock in;
     * phones with pre-order were not found -> empty list;
     * phones with pre-order -> return a list of phones with pre-order;
     */
    @Nested
    class FilterByStockTest {

        @Test
        void whenPhonesInStockWereNotFound_thanReturnEmptyList() {
            PhoneMultipleFilterRequest filterRequest = new PhoneMultipleFilterRequest(null, null,
                    null, true, null, null);
            Specification<Phone> phoneSpecification = PhoneFilterSpecificationBuilder.build()
                    .add(filterRequest.inStock(), inStock -> ((root, query, builder)
                            -> builder.greaterThanOrEqualTo(root.get(Phone_.stock), 1)))
                    .buildAnd();
            doReturn(Page.empty()).when(phoneRepository).findAll(any(Specification.class), any(Pageable.class));
            doReturn(emptyList).when(phoneMapper).toResponseList(anyList());

            PageResponseDto<PhoneResponseDto> responseDto = phoneFilteringAndSortingServiceImpl.filterAndSort(filterRequest, page);
            assertNotNull(responseDto);
            assertTrue(responseDto.content().isEmpty());
            verify(phoneRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(phoneRepository);
        }

        @Test
        void whenPhonesInStockWereFound_thanReturnListOfPhonesWithStockIn() {
            PhoneMultipleFilterRequest filterRequest = new PhoneMultipleFilterRequest(null, null,
                    null, true, null, null);
            Specification<Phone> phoneSpecification = PhoneFilterSpecificationBuilder.build()
                    .add(filterRequest.inStock(), inStock -> ((root, query, builder)
                            -> builder.greaterThanOrEqualTo(root.get(Phone_.stock), 1)))
                    .buildAnd();
            doReturn(Page.empty()).when(phoneRepository).findAll(any(Specification.class), any(Pageable.class));
            doReturn(listOfPhonesWithStockIn).when(phoneMapper).toResponseList(anyList());

            PageResponseDto<PhoneResponseDto> responseDto = phoneFilteringAndSortingServiceImpl.filterAndSort(filterRequest, page);

            assertNotNull(responseDto);
            assertEquals(listOfPhonesWithStockIn.size(), responseDto.content().size());
            assertEquals(listOfPhonesWithStockIn, responseDto.content());
            assertTrue(responseDto.content().containsAll(listOfPhonesWithStockIn));
            verify(phoneRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(phoneRepository);
        }

        @Test
        void whenPhonesPreOrderWereNotFound_thanReturnEmptyList() {
            PhoneMultipleFilterRequest filterRequest = new PhoneMultipleFilterRequest(null, null,
                    null, null, true, null);
            Specification<Phone> phoneSpecification = PhoneFilterSpecificationBuilder.build()
                    .add(filterRequest.preOrder(), inStock -> ((root, query, builder)
                            -> builder.lessThanOrEqualTo(root.get(Phone_.stock), 0)))
                    .buildAnd();
            doReturn(Page.empty()).when(phoneRepository).findAll(any(Specification.class), any(Pageable.class));
            doReturn(emptyList).when(phoneMapper).toResponseList(anyList());

            PageResponseDto<PhoneResponseDto> responseDto = phoneFilteringAndSortingServiceImpl.filterAndSort(filterRequest, page);
            assertNotNull(responseDto);
            assertTrue(responseDto.content().isEmpty());
            verify(phoneRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(phoneRepository);
        }

        @Test
        void whenPhonesPreOrderWereFound_thanReturnListOfPhonesWithPreOrder() {
            PhoneMultipleFilterRequest filterRequest = new PhoneMultipleFilterRequest(null, null,
                    null, null, true, null);
            Specification<Phone> phoneSpecification = PhoneFilterSpecificationBuilder.build()
                    .add(filterRequest.preOrder(), inStock -> ((root, query, builder)
                            -> builder.lessThanOrEqualTo(root.get(Phone_.stock), 0)))
                    .buildAnd();
            doReturn(Page.empty()).when(phoneRepository).findAll(any(Specification.class), any(Pageable.class));
            doReturn(listOfPhonesWithPreOrder).when(phoneMapper).toResponseList(anyList());

            PageResponseDto<PhoneResponseDto> responseDto = phoneFilteringAndSortingServiceImpl.filterAndSort(filterRequest, page);
            assertNotNull(responseDto);
            assertFalse(responseDto.content().isEmpty());
            assertEquals(listOfPhonesWithPreOrder.size(), responseDto.content().size());
            assertEquals(listOfPhonesWithPreOrder, responseDto.content());
            assertTrue(responseDto.content().containsAll(listOfPhonesWithPreOrder));
            verify(phoneRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
            verifyNoMoreInteractions(phoneRepository);
        }
    }
}