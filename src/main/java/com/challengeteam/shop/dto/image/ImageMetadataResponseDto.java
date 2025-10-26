    package com.challengeteam.shop.dto.image;

    public record ImageMetadataResponseDto(
            String name,
            String url,
            long size,
            String mimeType
    ) {
    }
