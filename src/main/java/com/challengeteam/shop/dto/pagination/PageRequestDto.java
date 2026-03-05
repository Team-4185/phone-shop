package com.challengeteam.shop.dto.pagination;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PageRequestDto(
        @Min(value = 1, message = "Minimum value for page parameter is {value}")
        @Max(value = 999_999_999, message = "Maximum value for page parameter is {value}")
        Integer page,
        @Min(value = 1, message = "Minimum value for size parameter is {value}")
        @Max(value = 100, message = "Maximum value for size parameter is {value}")
        Integer size
) {

    // default constructor allows to define default values
    public PageRequestDto {
        page = (page == null) ? 1 : page;
        size = (size == null) ? 10 : size;
    }

}
