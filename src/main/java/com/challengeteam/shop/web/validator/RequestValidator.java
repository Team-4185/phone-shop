package com.challengeteam.shop.web.validator;

public interface RequestValidator<T> {

    void validate(T requestObject);

}
