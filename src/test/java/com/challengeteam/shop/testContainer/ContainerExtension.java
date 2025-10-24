package com.challengeteam.shop.testContainer;

import com.challengeteam.shop.testContainer.container.MinioContainer;
import com.challengeteam.shop.testContainer.container.PostgresContainer;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

@Slf4j
public class ContainerExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        log.info("Initialization test containers..");
        PostgresContainer.init();
        MinioContainer.init();
    }

}
