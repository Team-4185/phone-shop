package com.challengeteam.shop.exceptionHandling.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestDataGeneratorOutOfLimitException extends RuntimeException {
    private String target;
    private int limit;

    public TestDataGeneratorOutOfLimitException(String target, int limit) {
        this.target = target;
        this.limit = limit;
    }

    public TestDataGeneratorOutOfLimitException(String message) {
        super(message);
    }

    public TestDataGeneratorOutOfLimitException(String message, Throwable cause) {
        super(message, cause);
    }

    public TestDataGeneratorOutOfLimitException(Throwable cause) {
        super(cause);
    }

    public TestDataGeneratorOutOfLimitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String getMessage() {
        return "Impossible generate " + target + " more then limit " + limit;
    }
}
