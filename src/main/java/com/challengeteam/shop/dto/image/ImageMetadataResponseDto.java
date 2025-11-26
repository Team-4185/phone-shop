    package com.challengeteam.shop.dto.image;

    public record ImageMetadataResponseDto(
            long id,
            String name,
            String url,
            long size,
            String mimeType
    ) {
    }
