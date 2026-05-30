package com.challengeteam.shop.testContainer.container;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;

public class RedisContainer {

    private static GenericContainer<?> redis;

    public static void init() {
        if (redis == null || !redis.isRunning()) {
            redis = new GenericContainer<>("redis:latest")
                    .withExposedPorts(6379);
            redis.start();
        }
    }

    public static void setRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }
}