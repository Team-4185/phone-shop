package com.challengeteam.shop.testContainer;

import com.challengeteam.shop.testContainer.container.MinioContainer;
import com.challengeteam.shop.testContainer.container.PostgresContainer;
import org.springframework.test.context.DynamicPropertyRegistry;

import java.util.TimeZone;

public class TestContextConfigurator {
    public final static String DEFAULT_PORT = "8080";
    public final static String DEFAULT_DB_URL = "no-url";
    public final static String DEFAULT_DB_USERNAME = "test-user";
    public final static String DEFAULT_DB_PASSWORD = "test-pass";
    public final static String DEFAULT_JWT_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDIzUhSsmAnJpv202FRJ9VnscBcoeUwAOHGKp2mfJRlD25zgy9u/7omTMPdMSLFVcizIPsuxjPR1NeALS+eR7mbhcElIxm6tjim1LsXZwevVo/W4xD9ae4G/D9EKjjlQNeFSRV3enSMA8hQSZmskFE14cafWqSdNwQIL4qzCsc6FJiz2wlJ/JMZYAvjXNBRerN+2mw5d5mb/obe7q4MGA2uf/tZ4skxcX6tVa/pwJWQLdbMNxUIU/EvRJrxYkO0R9h94BbwyxDkPH8eKWvLJZ1HaLAt4UQmBo+1dG7e488tQ0M8L6iacBsGlIGpEnvImW+YsTF7Rf1pGqvADdCn8HddAgMBAAECggEAJz43TDIgKIabJHnbIwkt474RYgkhyWfit9/MP6VJOxbw9xJESuUfdCy8epYHvZkuSBPCAzopFnEKTLqH+974nzRcsu1RMfQ6zh/1EHXKQrIgGlb2ExCIvES/+Ipn5CXv+NR3pYoBDPwQQeCgb+EkfetJ7grA8Ri3aQIhkSwiE29p/XYCiIgflnY2lj87n4BzrG08/NjON9+ThBvkKVt3j+KyhJmXLHKow90Skn2CbuUKUyPdZRXEvbOiEaUAqkHFAi+5rza6yFKa06DnPeTv9qWBidkxqhFJyr2lxbSSNU/75K50HeAbJPdWGVBTGbqJV03BeaTP4DVdIqIOShY0AQKBgQD5+DkLrNt3wZZEQyIGYm/Lx0v1XK/KDcoLzvTY3dXrrk60w+5mhlQ7jsQX9VLRxKlUgKaRTBveXK6MkRHERkSa5vfCP0EoA7AVpU6wJC8xc/yVyCwmEmutX4LDygoK6hUGIXqi8635sDMGhcphTpb2H97nzgBfguURD0nrVzrmfQKBgQDNpWgbYmSVDh5EGVFMHiG1F1GDnN1WBJM71AZ1nY5jA1hnOCvLmiDV7+vO+BrWkz5J2MreD03Ij0LAuB4+NI9T5XvXWmKIy9BOe/pV+tObFavmSGQjScCLHBJHE688JlrTow4SCX3+Dtsai5FoyV4JL3IDLEROpfwX5G3Z4StKYQKBgQDgecWYs3k++QydboAOpbVplSakR8DhPTLVGdwNKGGjzRuG/3CGh1j7RwDX1wmxsN6zMPjkACoCrM5fEyOWU9fmF0YlHTLA4VbuiU25pjTvPw5z35et9NrXja7bbgNIu993avc7gIMad93KnLkaWz379rDxD7CMFPrqMAWcnvAY7QKBgQCD70DRgNjDAGHOkuejjBlYE3PKmpMuIpVLZwYV41V8lKLc5h7C60lxuBFzoZ2mWKU3v4y31t2ydKcA+Z79jb7+tlYzndtlpE0qbUP4cYndD5RPk9YbBbAwD3xyeWCNmJXg/dWDIO/iVSmg6DYMIlTgU74z5uyUAM2xjm4jwOCDQQKBgBTSxuACZk9mde16NAw6cfnLfCFD3xGjjs45yVNwoYINMRdXpx/xBVbjVE/gCQfov8Qr0ONmqI6+kfM3ZK611MSfBMI2UdBFqPlSnrg6r/vdrkUBNrDVuKp6ZfytJI7gpnOPgzDbz2/CxNsUHxvpaVQhtJmmrKSblODzNE5EPHRF";
    public final static String DEFAULT_JWT_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyM1IUrJgJyab9tNhUSfVZ7HAXKHlMADhxiqdpnyUZQ9uc4Mvbv+6JkzD3TEixVXIsyD7LsYz0dTXgC0vnke5m4XBJSMZurY4ptS7F2cHr1aP1uMQ/WnuBvw/RCo45UDXhUkVd3p0jAPIUEmZrJBRNeHGn1qknTcECC+KswrHOhSYs9sJSfyTGWAL41zQUXqzftpsOXeZm/6G3u6uDBgNrn/7WeLJMXF+rVWv6cCVkC3WzDcVCFPxL0Sa8WJDtEfYfeAW8MsQ5Dx/HilryyWdR2iwLeFEJgaPtXRu3uPPLUNDPC+omnAbBpSBqRJ7yJlvmLExe0X9aRqrwA3Qp/B3XQIDAQAB";
    public final static String DEFAULT_JWT_ACCESS_TOKEN_EXPIRATION = "10";
    public final static String DEFAULT_JWT_REFRESH_TOKEN_EXPIRATION = "1";
    public final static String DEFAULT_MINIO_URL = "no-url";
    public final static String DEFAULT_MINIO_USERNAME = "username";
    public final static String DEFAULT_MINIO_PASSWORD = "password";
    public final static String DEFAULT_CORS_ALLOWED_ORIGINS = "['*']";
    public final static String DEFAULT_CORS_ALLOWED_METHODS = "['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS']";
    public final static String DEFAULT_CORS_ALLOWED_HEADERS = "['*']";
    public final static String DEFAULT_CORS_MAX_CACHE_AGE = "3600";
    public final static String DEFAULT_CORS_ALLOWED_CREDENTIALS = "true";

    /**
     * Configuring an application context for successfully up the context.
     * Created for integration tests.
     *
     * @param propertyRegistry a parameter that allows to load properties dynamically
     */
    private static void configureDefaultContext(DynamicPropertyRegistry propertyRegistry) {
        propertyRegistry.add("server.port", () -> DEFAULT_PORT);
        propertyRegistry.add("spring.datasource.url", () -> DEFAULT_DB_URL);
        propertyRegistry.add("spring.datasource.username", () -> DEFAULT_DB_USERNAME);
        propertyRegistry.add("spring.datasource.password", () -> DEFAULT_DB_PASSWORD);
        propertyRegistry.add("security.jwt.private-key", () -> DEFAULT_JWT_PRIVATE_KEY);
        propertyRegistry.add("security.jwt.public-key", () -> DEFAULT_JWT_PUBLIC_KEY);
        propertyRegistry.add("security.jwt.access-token-expiration", () -> DEFAULT_JWT_ACCESS_TOKEN_EXPIRATION);
        propertyRegistry.add("security.jwt.refresh-token-expiration", () -> DEFAULT_JWT_REFRESH_TOKEN_EXPIRATION);
        propertyRegistry.add("minio.url", () -> DEFAULT_MINIO_URL);
        propertyRegistry.add("minio.username", () -> DEFAULT_MINIO_USERNAME);
        propertyRegistry.add("minio.password", () -> DEFAULT_MINIO_PASSWORD);
        propertyRegistry.add("security.cors.allowed-origins", () -> DEFAULT_CORS_ALLOWED_ORIGINS);
        propertyRegistry.add("security.cors.allowed-methods", () -> DEFAULT_CORS_ALLOWED_METHODS);
        propertyRegistry.add("security.cors.allowed-headers", () -> DEFAULT_CORS_ALLOWED_HEADERS);
        propertyRegistry.add("security.cors.max-cache-age", () -> DEFAULT_CORS_MAX_CACHE_AGE);
        propertyRegistry.add("security.cors.allow-credentials", () -> DEFAULT_CORS_ALLOWED_CREDENTIALS);
    }

    private static void configureSystem() {
        // set a time-zone for independence of execution location
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public synchronized static void initRequiredProperties(DynamicPropertyRegistry propertyRegistry) {
        configureDefaultContext(propertyRegistry);
        configureSystem();
        PostgresContainer.setPostgresProperties(propertyRegistry);
        MinioContainer.setMinioProperties(propertyRegistry);
    }

}
