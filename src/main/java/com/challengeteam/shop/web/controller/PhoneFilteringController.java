package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.pagination.paginationRequest.PhoneMultipleFilterRequest;
import com.challengeteam.shop.dto.pagination.paginationResponse.PageResponseDto;
import com.challengeteam.shop.dto.phone.PhoneResponseDto;
import com.challengeteam.shop.service.pagination.PhoneFilteringAndSortingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/filter")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Phone Filtering", description = "API endpoints for filtering and sorting phones")
public class PhoneFilteringController {

    private final PhoneFilteringAndSortingService phoneFilteringAndSortingService;

    @Operation(
            summary = "Filter and sort phones",
            description = "Retrieve a paginated list of phones based on multiple filter criteria and sorting options"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved filtered phones",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid filter parameters or pagination values"
            )
    })
    @GetMapping("/by")
    public ResponseEntity<PageResponseDto<PhoneResponseDto>>
    filterPhones(@Parameter(description = "Filter criteria for phones", required = true)
                 @Valid @ModelAttribute PhoneMultipleFilterRequest filterRequest,
                 @Parameter(description = "Page number (starts from 1)", example = "1")
                 @RequestParam(defaultValue = "1") @Min(1) int page,
                 @Parameter(description = "Number of items per page", example = "12")
                 @RequestParam(defaultValue = "12") @Min(1) int size) {
        log.debug("filterPhones: filterRequest = {}, page = {}, size = {}", filterRequest, page, size);
        Pageable pageable = PageRequest.of(page - 1, size);
        PageResponseDto<PhoneResponseDto> response = phoneFilteringAndSortingService.filterAndSort(filterRequest, pageable);
        return ResponseEntity.ok(response);
    }
}