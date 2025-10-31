package com.challengeteam.shop.web.resolver.headerResolver;

import org.springframework.http.HttpHeaders;

public interface HeadersResolver<T> {

    HttpHeaders resolveHeaders(T responseBody);

}
