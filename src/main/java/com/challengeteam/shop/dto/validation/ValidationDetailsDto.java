package com.challengeteam.shop.dto.validation;

import java.util.List;

public record ValidationDetailsDto(
        String parameter,
        List<String> validationProblems
) {
}
