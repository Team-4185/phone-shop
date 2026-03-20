package com.challengeteam.shop.web.controller.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Validated
public class AdminProductController {

    //getAllProducts()
    //getProductById()
}
