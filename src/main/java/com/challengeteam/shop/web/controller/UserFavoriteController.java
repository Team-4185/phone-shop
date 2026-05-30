package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.phone.response.PhoneResponseDto;
import com.challengeteam.shop.security.SimpleUserDetailsService.SimpleUserDetails;
import com.challengeteam.shop.service.UserFavoriteService;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/v1/me/favorites")
@RequiredArgsConstructor
@Validated
public class UserFavoriteController {

    private final UserFavoriteService userFavoriteService;

    @GetMapping
    public ResponseEntity<List<PhoneResponseDto>> getUserFavorites(
            @AuthenticationPrincipal SimpleUserDetails simpleUserDetails
    ) {
        Long userId = simpleUserDetails.getUserId();

        return ResponseEntity.ok(userFavoriteService.getUserFavorites(userId));
    }

    @PostMapping("/{phoneId}")
    public ResponseEntity<List<PhoneResponseDto>> addProductToFavorites(
            @AuthenticationPrincipal SimpleUserDetails simpleUserDetails,
            @PathVariable @Positive Long phoneId
    ) {
        Long userId = simpleUserDetails.getUserId();

        return ResponseEntity.ok(userFavoriteService.addProductToFavorites(userId, phoneId));
    }

    @DeleteMapping("/{phoneId}")
    public ResponseEntity<List<PhoneResponseDto>> removeProductFromFavorites(
            @AuthenticationPrincipal SimpleUserDetails simpleUserDetails,
            @PathVariable @Positive Long phoneId
    ) {
        Long userId = simpleUserDetails.getUserId();

        return ResponseEntity.ok(userFavoriteService.removeProductFromFavorites(userId, phoneId));
    }
}
