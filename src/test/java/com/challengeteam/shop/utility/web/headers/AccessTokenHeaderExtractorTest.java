package com.challengeteam.shop.utility.web.headers;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class AccessTokenHeaderExtractorTest {

    @Test
    void shouldReturnToken_whenValidBearerHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer my.jwt.token");

        String result = AccessTokenHeaderExtractor.extractAccessToken(request);

        assertThat(result).isEqualTo("my.jwt.token");
    }

    @Test
    void shouldReturnNull_whenHeaderIsMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = AccessTokenHeaderExtractor.extractAccessToken(request);

        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNull_whenHeaderHasNoBearerPrefix() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic my.jwt.token");

        String result = AccessTokenHeaderExtractor.extractAccessToken(request);

        assertThat(result).isNull();
    }
}