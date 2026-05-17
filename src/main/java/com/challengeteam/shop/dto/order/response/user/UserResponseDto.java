package com.challengeteam.shop.dto.order.response.user;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link com.challengeteam.shop.entity.user.User}
 */
public record UserResponseDto(Long id,
                              Instant createdAt,
                              Instant updatedAt,
                              String email,
                              String firstName,
                              String lastName,
                              String city,
                              String phoneNumber) implements Serializable {
}