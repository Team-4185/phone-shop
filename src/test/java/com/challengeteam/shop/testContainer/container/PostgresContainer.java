package com.challengeteam.shop.testContainer.container;

import com.challengeteam.shop.testContainer.TestContextConfigurator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Slf4j
public class PostgresContainer {
    public final static String DOCKER_POSTGRES_IMAGE = "postgres:14";
    public final static String POSTGRES_DATABASE = "test";
    public final static String POSTGRES_USERNAME = "postgres";
    public final static String POSTGRES_PASSWORD = "postgres";
    private final static PostgreSQLContainer postgres;

    static {
        postgres = new PostgreSQLContainer(DOCKER_POSTGRES_IMAGE)
                .withDatabaseName(POSTGRES_DATABASE)
                .withUsername(POSTGRES_USERNAME)
                .withPassword(POSTGRES_PASSWORD);
        postgres.start();
        log.info("Postgres test container was successfully initiated and started");

        Runtime.getRuntime().addShutdownHook(new Thread(postgres::stop));
    }

    public static void init() {
        // NOOP
    }

    public static void setPostgresProperties(DynamicPropertyRegistry propertyRegistry) {
        propertyRegistry.add("spring.datasource.url", postgres::getJdbcUrl);
        propertyRegistry.add("spring.datasource.username", postgres::getUsername);
        propertyRegistry.add("spring.datasource.password", postgres::getPassword);
    }

}
