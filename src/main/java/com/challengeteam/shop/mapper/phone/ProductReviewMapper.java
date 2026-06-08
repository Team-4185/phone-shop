package com.challengeteam.shop.mapper.phone;

import com.challengeteam.shop.dto.phone.response.ProductReviewResponseDto;
import com.challengeteam.shop.entity.review.ProductReview;
import com.challengeteam.shop.entity.user.User;
import org.springframework.stereotype.Component;

@Component
public class ProductReviewMapper {

    public ProductReviewResponseDto toResponse(ProductReview review) {
        User user = review.getUser();

        return new ProductReviewResponseDto(
                review.getId(),
                review.getPhone().getId(),
                user.getId(),
                authorName(user),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt());
    }

    private String authorName(User user) {
        String firstName = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String lastName = user.getLastName() == null ? "" : user.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();

        return fullName.isBlank() ? user.getEmail() : fullName;
    }
}
