package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.persistence.repository.OrderItemRepository;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.service.PriceService;
import com.challengeteam.shop.service.impl.validator.OrderValidator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    @Mock private OrderRepository orderRepository;
    @Mock private OrderValidator orderValidator;
    @Mock private PriceService priceService;
    @Mock private OrderItemRepository orderItemRepository;
    @InjectMocks private OrderServiceImpl orderService;

    @Nested
    class GetOrdersTest {

        @Test
        void whenOrdersExists_thenReturnPageOfOrders() throws Exception {

        }

        @Test
        void whenNoOneOrder_thenReturnEmptyPage() throws Exception {

        }

        @Test
        void whenRequestedOutOfBoundPage_thenReturnEmptyPage() throws Exception {

        }

        @Test
        void whenParameterPageLessZero_thenThrowException() throws Exception {

        }

        @Test
        void whenParameterSizeLessZero_thenThrowException() throws Exception {

        }

    }

    @Nested
    class GetByIdTest {

        @Test
        void whenOrderExists_thenReturnOptionalOrder() throws Exception {

        }

        @Test
        void whenOrderDoesntExist_thenReturnEmptyOptional() throws Exception {

        }

        @Test
        void whenParameterIdLessZero_thenThrowException() throws Exception {

        }

    }

    @Nested
    class GetOrdersByUserIdTest {

        @Test
        void whenUserHasOrders_thenReturnHisOrders() throws Exception {

        }

        @Test
        void whenUserDoesntHaveOrders_thenReturnEmptyList() throws Exception {

        }

        @Test
        void whenParameterUserIdLessZero_thenThrowException() throws Exception {

        }

    }

    @Nested
    class CreateTest {

        @Test
        void whenAllValid_thenCreateNewOrderForUser() throws Exception {

        }

        @Test
        void whenParameterUserIsNull_thenThrowException() throws Exception {

        }

        @Test
        void whenParameterProductsIsNull_thenThrowException() throws Exception {

        }

        @Test
        void whenParameterCreateUserDtoIsNull_thenThrowException() throws Exception {

        }

        @Test
        void whenMultipleProducts_thenCreateCorrectAmountOfOrderItems() throws Exception {

        }

        @Test
        void whenValidatorThrowsException_thenPropagateException() throws Exception {

        }

    }

    @Nested
    class SetOrderStatusTest {
    }

    @Nested
    class MakeOrderPaidTest {
    }

    @Nested
    class MakeOrderFailedTest {
    }

    @Nested
    class SetProcessedByWebhookTest {
    }

    @Nested
    class SetCheckoutUrlTest {
    }

    static class TestResources {

    }

}