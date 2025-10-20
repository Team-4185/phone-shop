package com.challengeteam.shop.dto.image;

public record ImageDataDto(
        String filename,
        byte[] imageBytes,
        String mimeType,
        long size
) {
}
