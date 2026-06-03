package com.challengeteam.shop.service.pagination;

import com.challengeteam.shop.constraints.filter.FilterRequestConstraints;
import com.challengeteam.shop.dto.pagination.paginationRequest.PhoneMultipleFilterRequest;
import com.challengeteam.shop.dto.pagination.paginationResponse.PageResponseDto;
import com.challengeteam.shop.dto.phone.response.PhoneResponseDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.Phone_;
import com.challengeteam.shop.mapper.phone.PhoneMapper;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.utility.pagination.filter.sort.SortResolver;
import com.challengeteam.shop.utility.pagination.filter.specefication.PhoneFilterSpecificationBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link PhoneFilteringAndSortingService} that provides filtering and sorting
 * capabilities for phone entities.
 * <p>
 * This service uses Spring Data JPA Specifications to dynamically build complex queries based on
 * multiple filter criteria such as brandName, price range, and stock availability. The filtered results
 * are returned as paginated responses with customizable sorting options.
 * </p>
 *
 * @see PhoneFilteringAndSortingService
 * @see PhoneRepository
 * @see PhoneMapper
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PhoneFilteringAndSortingServiceImpl implements PhoneFilteringAndSortingService {
    private static final String NO_BRAND_FILTER_VALUE = "__no_brand_filter__";

    private final PhoneRepository phoneRepository;
    private final PhoneMapper phoneMapper;


    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    @Override
    public PageResponseDto<PhoneResponseDto> filterAndSort(PhoneMultipleFilterRequest filterDto, Pageable pageable) {
        log.debug("Filter and sort phones with filter: {}", filterDto);
        Specification<Phone> specification = PhoneFilterSpecificationBuilder.build()
                .add(filterDto.brands(), brands -> (root, query, builder)
                        -> root.get(Phone_.BRAND).in(brands))
                .add(filterDto.minPrice(), minPrice -> (root, query, builder)
                        -> builder.greaterThanOrEqualTo(root.get(Phone_.PRICE), minPrice))
                .add(filterDto.maxPrice(), maxPrice -> (root, query, builder)
                        -> builder.lessThanOrEqualTo(root.get(Phone_.PRICE), maxPrice))
                .add(filterDto.inStock(), () -> (root, query, builder)
                        -> builder.greaterThan(root.get(Phone_.STOCK), 0))
                .add(filterDto.preOrder(), () -> (root, query, builder)
                        -> builder.equal(root.get(Phone_.STOCK), 0))
                .buildAnd();
        log.debug("Specification: {}", specification);
        boolean popularitySort = FilterRequestConstraints.isPopularitySort(filterDto.sort());
        Pageable pageableWithSort = popularitySort
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), resolveSort(filterDto.sort()));
        Page<Phone> phonePage = popularitySort
                ? phoneRepository.findAllByCatalogPopularity(
                        filterBrands(filterDto.brands()),
                        filterDto.brands() == null || filterDto.brands().isEmpty(),
                        filterDto.minPrice(),
                        filterDto.maxPrice(),
                        Boolean.TRUE.equals(filterDto.inStock()),
                        Boolean.TRUE.equals(filterDto.preOrder()),
                        pageableWithSort)
                : phoneRepository.findAll(specification, pageableWithSort);
        log.info("Phones found: {}", phonePage.getTotalElements());
        List<PhoneResponseDto> filteredPhoneDtos = phoneMapper.toResponseList(phonePage.getContent());
        return new PageResponseDto<PhoneResponseDto>(
                filteredPhoneDtos,
                phonePage.getNumber() + 1,
                phonePage.getSize(),
                phonePage.getTotalElements(),
                phonePage.getTotalPages(),
                phonePage.isFirst(),
                phonePage.isLast());
    }

    private Sort resolveSort(String sortParam) {
        Sort sort = SortResolver.resolve(sortParam);
        log.debug("Sort: {}", sort);
        return sort;
    }

    private List<String> filterBrands(List<String> brands) {
        return brands == null || brands.isEmpty() ? List.of(NO_BRAND_FILTER_VALUE) : brands;
    }
}
