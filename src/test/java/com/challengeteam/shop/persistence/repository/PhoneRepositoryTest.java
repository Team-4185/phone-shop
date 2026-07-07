package com.challengeteam.shop.persistence.repository;

import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.PhoneCharacteristics;
import com.challengeteam.shop.entity.phone.ProductStatus;
import com.challengeteam.shop.testContainer.ContainerExtension;
import com.challengeteam.shop.testContainer.TestContextConfigurator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(ContainerExtension.class)
class PhoneRepositoryTest {

    @Autowired
    private PhoneRepository phoneRepository;

    @DynamicPropertySource
    static void loadPropertiesForTest(DynamicPropertyRegistry propertyRegistry) {
        TestContextConfigurator.initRequiredProperties(propertyRegistry);
    }

    @BeforeEach
    void setup() {
        phoneRepository.deleteAll();
        IntStream.rangeClosed(1, 5)
                .mapToObj(this::phone)
                .forEach(phoneRepository::saveAndFlush);
    }

    @Test
    void whenFindTop4ByCreatedAtDesc_thenReturnFourNewestPhones() {
        List<Phone> result = phoneRepository.findTop4ByOrderByCreatedAtDescIdDesc();

        assertThat(result)
                .extracting(Phone::getName)
                .containsExactly("Phone 5", "Phone 4", "Phone 3", "Phone 2");
    }

    private Phone phone(int number) {
        return Phone.builder()
                .name("Phone " + number)
                .description("Phone " + number + " description")
                .price(BigDecimal.valueOf(100 + number))
                .brand("Brand")
                .releaseYear(2026)
                .sku("PHONE-" + number)
                .stock(10)
                .status(ProductStatus.IN_STOCK)
                .phoneCharacteristics(PhoneCharacteristics.builder()
                        .cpu("CPU")
                        .coresNumber(8)
                        .screenSize("6.1\"")
                        .frontCamera("12 MP")
                        .mainCamera("50 MP")
                        .batteryCapacity("4000 mAh")
                        .build())
                .build();
    }
}
