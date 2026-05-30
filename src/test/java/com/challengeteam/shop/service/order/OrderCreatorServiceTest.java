package com.challengeteam.shop.service.order;

import com.challengeteam.shop.constants.notification.type.Notification_type;
import com.challengeteam.shop.dto.email.Notification;
import com.challengeteam.shop.dto.order.OrderResponseDto;
import com.challengeteam.shop.dto.order.request.item.OrderItemRequestDto;
import com.challengeteam.shop.dto.order.request.order.OrderRequestDto;
import com.challengeteam.shop.dto.order.request.payment.PaymentDetailsRequestDto;
import com.challengeteam.shop.dto.order.request.shippingAddress.ShippingAddressRequestDto;
import com.challengeteam.shop.dto.payment.TransactionResult;
import com.challengeteam.shop.entity.order.DeliveryMethod;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.entity.order.payment.PaymentDetails;
import com.challengeteam.shop.entity.order.payment.PaymentMethod;
import com.challengeteam.shop.entity.order.payment.PaymentStatus;
import com.challengeteam.shop.entity.order.shipping.ShippingAddress;
import com.challengeteam.shop.entity.phone.*;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.order.OrderCreationException;
import com.challengeteam.shop.exceptionHandling.exception.order.PaymentFailedException;
import com.challengeteam.shop.exceptionHandling.exception.phone.PhoneNotFoundException;
import com.challengeteam.shop.mapper.order.OrderMapper;
import com.challengeteam.shop.mapper.order.ShippingAddressOrderMapper;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.service.mock.PaymentMockService;
import com.challengeteam.shop.service.notification.NotificationSenderService;
import com.challengeteam.shop.service.order.create.OrderCreatorService;
import com.challengeteam.shop.service.order.create.OrderCreatorServiceImpl;
import com.challengeteam.shop.utility.AuthenticationUserExtractorHelper;
import com.challengeteam.shop.utility.notification.email.OrderConfirmationEmailBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link OrderCreatorService}
 */
@ExtendWith(MockitoExtension.class)
class OrderCreatorServiceTest {

    @Mock
    private PhoneRepository phoneRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentMockService paymentMockService;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private ShippingAddressOrderMapper shippingAddressOrderMapper;
    @Mock
    private AuthenticationUserExtractorHelper authenticationUserExtractorHelper;
    @Mock
    private NotificationSenderService notificationSenderService;
    @Mock
    private OrderConfirmationEmailBuilder orderConfirmationEmailBuilder;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderCreatorServiceImpl orderCreatorService;

    // ── Common stubs (non-subject mocks shared across tests) ─────────────────

    @BeforeEach
    void setUpCommonStubs() {
        // default: guest order
        lenient().when(authenticationUserExtractorHelper
                        .extractUserFromSecurityContextHolder(any()))
                .thenReturn(Optional.empty());

        // default: shipping mapper returns empty address
        lenient().when(shippingAddressOrderMapper
                        .toShippingAddress(any(ShippingAddressRequestDto.class)))
                .thenReturn(new ShippingAddress());

        // default: save returns the same order passed in
        lenient().when(orderRepository.save(any(Order.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // default: mapper returns a stub response
        lenient().when(orderMapper.toDto(any(Order.class)))
                .thenReturn(buildStubOrderResponse());

        // default: email builder returns a stub notification
        lenient().when(orderConfirmationEmailBuilder
                        .buildOrderConfirmationNotification(any(Order.class)))
                .thenReturn(new Notification(
                        "customer@example.com",
                        "noreply@gadgetroom.ua",
                        "Order confirmed",
                        "<html>...</html>"
                ));
    }

    // =========================================================================
    // Test data builders
    // =========================================================================

    private PhoneCharacteristics buildCharacteristics() {
        return PhoneCharacteristics.builder()
                .phoneColors(Set.of(PhoneColor.GOLD))
                .storageCapacities(Set.of(StorageCapacity.CAPACITY_128GB))
                .coresNumber(123)
                .batteryCapacity("800")
                .mainCamera("25")
                .build();
    }

    private Phone buildPhoneInStock(Long id, BigDecimal price, int stock) {
        Phone phone = new Phone();
        phone.setId(id);
        phone.setName("iPhone 15 Pro");
        phone.setSku("SKU-" + id);
        phone.setPrice(price);
        phone.setStock(stock);
        phone.setStatus(ProductStatus.IN_STOCK);
        phone.setPhoneCharacteristics(buildCharacteristics());
        return phone;
    }

    private Phone buildPhoneOutOfStock(Long id) {
        Phone phone = new Phone();
        phone.setId(id);
        phone.setName("Samsung S24");
        phone.setSku("SKU-OOS-" + id);
        phone.setPrice(BigDecimal.valueOf(999));
        phone.setStock(0);
        phone.setStatus(ProductStatus.OUT_OF_STOCK);
        phone.setPhoneCharacteristics(buildCharacteristics());
        return phone;
    }

    private OrderItemRequestDto item(Long phoneId, int quantity) {
        return new OrderItemRequestDto(phoneId, quantity, PhoneColor.GOLD, StorageCapacity.CAPACITY_128GB);
    }

    private PaymentDetailsRequestDto validCardDetails() {
        return new PaymentDetailsRequestDto("JOHN DOE", "4111111111111111", 12, 2026, "123");
    }

    private ShippingAddressRequestDto courierAddress() {
        return new ShippingAddressRequestDto(
                "5", "10A", null, null,
                "Main Street", "Kyiv", "Kyiv Region", "Ukraine", "01001"
        );
    }

    private OrderRequestDto cardCourierRequest(List<OrderItemRequestDto> items) {
        return new OrderRequestDto(
                "customer@example.com", "John", "Doe", "+380991234567",
                PaymentMethod.CARD, validCardDetails(),
                DeliveryMethod.COURIER, courierAddress(),
                items
        );
    }

    private OrderRequestDto cashPickupRequest(List<OrderItemRequestDto> items) {
        return new OrderRequestDto(
                "customer@example.com", "John", "Doe", "+380991234567",
                PaymentMethod.CASH_ON_DELIVERY, null,
                DeliveryMethod.PICKUP, null,
                items
        );
    }

    private TransactionResult successfulPayment() {
        return new TransactionResult(PaymentStatus.PAID, "tx-abc-123", null);
    }

    private TransactionResult failedPayment() {
        return new TransactionResult(PaymentStatus.FAILED, null, "Insufficient funds");
    }

    private OrderResponseDto buildStubOrderResponse() {
        return new OrderResponseDto(
                1L, null, null, null,
                "customer@example.com", "John", "Doe", "+380991234567",
                OrderStatus.NEW, PaymentMethod.CARD, DeliveryMethod.COURIER,
                null, null, BigDecimal.valueOf(999), List.of()
        );
    }

    // =========================================================================

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Phone validation — stock and existence checks")
            // Cases:
            //   - phone ID does not exist in DB → PhoneNotFoundException
            //   - one of multiple phone IDs is missing → PhoneNotFoundException with missing IDs
            //   - phone exists but stock is 0 → OrderCreationException
            //   - requested quantity exceeds available stock → OrderCreationException
            // -------------------------------------------------------------------------
    class PhoneValidationTests {

        @Test
        @DisplayName("Should throw PhoneNotFoundException when phone ID does not exist in DB")
        void shouldThrowPhoneNotFoundException_whenPhoneDoesNotExist() {
            when(phoneRepository.findAllById(any())).thenReturn(List.of());

            assertThatThrownBy(() ->
                    orderCreatorService.create(cardCourierRequest(List.of(item(999L, 1))), null))
                    .isInstanceOf(PhoneNotFoundException.class)
                    .hasMessageContaining("999");

            verifyNoInteractions(paymentMockService, orderRepository);
        }

        @Test
        @DisplayName("Should throw PhoneNotFoundException when some phone IDs are missing in DB")
        void shouldThrowPhoneNotFoundException_whenSomePhoneIdsAreMissing() {
            Phone existingPhone = buildPhoneInStock(1L, BigDecimal.valueOf(999), 10);
            when(phoneRepository.findAllById(any())).thenReturn(List.of(existingPhone));

            List<OrderItemRequestDto> items = List.of(item(1L, 1), item(999L, 1));

            assertThatThrownBy(() ->
                    orderCreatorService.create(cardCourierRequest(items), null))
                    .isInstanceOf(PhoneNotFoundException.class);

            verifyNoInteractions(paymentMockService, orderRepository);
        }

        @Test
        @DisplayName("Should throw OrderCreationException when phone stock is 0")
        void shouldThrowOrderCreationException_whenPhoneIsOutOfStock() {
            Phone outOfStock = buildPhoneOutOfStock(1L);
            when(phoneRepository.findAllById(any())).thenReturn(List.of(outOfStock));

            assertThatThrownBy(() ->
                    orderCreatorService.create(cardCourierRequest(List.of(item(1L, 1))), null))
                    .isInstanceOf(OrderCreationException.class);

            verifyNoInteractions(paymentMockService, orderRepository);
        }

        @Test
        @DisplayName("Should throw OrderCreationException when requested quantity exceeds available stock")
        void shouldThrowOrderCreationException_whenQuantityExceedsStock() {
            Phone phone = buildPhoneInStock(1L, BigDecimal.valueOf(999), 2);
            when(phoneRepository.findAllById(any())).thenReturn(List.of(phone));

            assertThatThrownBy(() ->
                    orderCreatorService.create(cardCourierRequest(List.of(item(1L, 5))), null))
                    .isInstanceOf(OrderCreationException.class);

            verifyNoInteractions(paymentMockService, orderRepository);
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("User resolution — guest vs registered user")
            // Cases:
            //   - null authentication → order created without linked user (guest)
            //   - valid authentication → registered user is linked to the order
            // -------------------------------------------------------------------------
    class UserResolutionTests {

        @Test
        @DisplayName("Should create order without linked user when authentication is null")
        void shouldCreateGuestOrder_whenAuthenticationIsNull() {
            Phone phone = buildPhoneInStock(1L, BigDecimal.valueOf(999), 10);
            when(phoneRepository.findAllById(any())).thenReturn(List.of(phone));
            when(paymentMockService.pay(any(), any())).thenReturn(successfulPayment());

            orderCreatorService.create(cardCourierRequest(List.of(item(1L, 1))), null);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            assertThat(captor.getValue().getUser()).isNull();
        }

        @Test
        @DisplayName("Should link registered user to order when authentication is present")
        void shouldLinkRegisteredUser_whenAuthenticationIsPresent() {
            User registeredUser = new User();
            registeredUser.setId(42L);

            when(authenticationUserExtractorHelper
                    .extractUserFromSecurityContextHolder(authentication))
                    .thenReturn(Optional.of(registeredUser));

            Phone phone = buildPhoneInStock(1L, BigDecimal.valueOf(999), 10);
            when(phoneRepository.findAllById(any())).thenReturn(List.of(phone));
            when(paymentMockService.pay(any(), any())).thenReturn(successfulPayment());

            orderCreatorService.create(cardCourierRequest(List.of(item(1L, 1))), authentication);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            assertThat(captor.getValue().getUser()).isEqualTo(registeredUser);
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Card payment processing")
            // Cases:
            //   - successful payment → order saved with PAID status and transactionId stored
            //   - failed payment → PaymentFailedException thrown, order is NOT saved
            //   - payment service is called with the correct calculated amount
            // -------------------------------------------------------------------------
    class CardPaymentTests {

        @Test
        @DisplayName("Should save order with PAID status and transactionId when payment succeeds")
        void shouldSaveOrder_withPaidStatusAndTransactionId_whenPaymentSucceeds() {
            Phone phone = buildPhoneInStock(1L, BigDecimal.valueOf(999), 10);
            when(phoneRepository.findAllById(any())).thenReturn(List.of(phone));
            when(paymentMockService.pay(any(), any())).thenReturn(successfulPayment());

            orderCreatorService.create(cardCourierRequest(List.of(item(1L, 1))), null);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());

            PaymentDetails paymentDetails = captor.getValue().getPaymentDetails();
            assertThat(paymentDetails.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
            assertThat(paymentDetails.getTransactionId()).isEqualTo("tx-abc-123");
        }

        @Test
        @DisplayName("Should throw PaymentFailedException and not save order when payment fails")
        void shouldThrowPaymentFailedException_andNotSaveOrder_whenPaymentFails() {
            Phone phone = buildPhoneInStock(1L, BigDecimal.valueOf(999), 10);
            when(phoneRepository.findAllById(any())).thenReturn(List.of(phone));
            when(paymentMockService.pay(any(), any())).thenReturn(failedPayment());

            assertThatThrownBy(() ->
                    orderCreatorService.create(cardCourierRequest(List.of(item(1L, 1))), null))
                    .isInstanceOf(PaymentFailedException.class)
                    .hasMessageContaining("Insufficient funds");

            verifyNoInteractions(orderRepository);
        }

        @Test
        @DisplayName("Should call payment service with correctly calculated total amount")
        void shouldCallPaymentService_withCorrectTotalAmount() {
            Phone phone = buildPhoneInStock(1L, BigDecimal.valueOf(500), 10);
            when(phoneRepository.findAllById(any())).thenReturn(List.of(phone));
            when(paymentMockService.pay(any(), any())).thenReturn(successfulPayment());

            orderCreatorService.create(cardCourierRequest(List.of(item(1L, 3))), null);

            verify(paymentMockService).pay(any(), eq(new BigDecimal("1500")));
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Cash on delivery payment processing")
            // Cases:
            //   - payment service is NOT called for CASH_ON_DELIVERY orders
            //   - order is saved with PENDING payment status
            // -------------------------------------------------------------------------
    class CashOnDeliveryPaymentTests {

        @Test
        @DisplayName("Should not call payment service when payment method is CASH_ON_DELIVERY")
        void shouldNotCallPaymentService_whenCashOnDelivery() {
            Phone phone = buildPhoneInStock(1L, BigDecimal.valueOf(999), 10);
            when(phoneRepository.findAllById(any())).thenReturn(List.of(phone));

            orderCreatorService.create(cashPickupRequest(List.of(item(1L, 1))), null);

            verifyNoInteractions(paymentMockService);
        }

        @Test
        @DisplayName("Should save order with PENDING payment status when payment method is CASH_ON_DELIVERY")
        void shouldSaveOrder_withPendingPaymentStatus_whenCashOnDelivery() {
            Phone phone = buildPhoneInStock(1L, BigDecimal.valueOf(999), 10);
            when(phoneRepository.findAllById(any())).thenReturn(List.of(phone));

            orderCreatorService.create(cashPickupRequest(List.of(item(1L, 1))), null);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            assertThat(captor.getValue().getPaymentDetails().getPaymentStatus())
                    .isEqualTo(PaymentStatus.PENDING);
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Order total calculation")
            // Cases:
            //   - single item: total = price * quantity
            //   - multiple items: total = sum of (price * quantity) for each item
            // -------------------------------------------------------------------------
    class OrderTotalCalculationTests {

        @Test
        @DisplayName("Should calculate correct total for a single item")
        void shouldCalculateCorrectTotal_forSingleItem() {
            Phone phone = buildPhoneInStock(1L, BigDecimal.valueOf(999), 10);
            when(phoneRepository.findAllById(any())).thenReturn(List.of(phone));
            when(paymentMockService.pay(any(), any())).thenReturn(successfulPayment());

            orderCreatorService.create(cardCourierRequest(List.of(item(1L, 2))), null);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            assertThat(captor.getValue().getTotal())
                    .isEqualByComparingTo(BigDecimal.valueOf(1998));
        }

        @Test
        @DisplayName("Should calculate correct total for multiple items")
        void shouldCalculateCorrectTotal_forMultipleItems() {
            Phone phone1 = buildPhoneInStock(1L, BigDecimal.valueOf(999), 10);
            Phone phone2 = buildPhoneInStock(2L, BigDecimal.valueOf(500), 10);
            when(phoneRepository.findAllById(any())).thenReturn(List.of(phone1, phone2));
            when(paymentMockService.pay(any(), any())).thenReturn(successfulPayment());

            orderCreatorService.create(
                    cardCourierRequest(List.of(item(1L, 1), item(2L, 3))), null);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            assertThat(captor.getValue().getTotal())
                    .isEqualByComparingTo(BigDecimal.valueOf(2499));
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Saved order structure")
            // Cases:
            //   - order contains correct customer details from request
            //   - order status is NEW after creation
            //   - order contains correct number of items
            //   - each item unit price is taken from DB, not from client
            //   - order total is set on the order entity
            // -------------------------------------------------------------------------
    class OrderStructureTests {

        @Test
        @DisplayName("Should save order with correct customer details from request")
        void shouldSaveOrder_withCorrectCustomerDetails() {
            Phone phone = buildPhoneInStock(1L, BigDecimal.valueOf(999), 10);
            when(phoneRepository.findAllById(any())).thenReturn(List.of(phone));
            when(paymentMockService.pay(any(), any())).thenReturn(successfulPayment());

            orderCreatorService.create(cardCourierRequest(List.of(item(1L, 1))), null);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());

            Order saved = captor.getValue();
            assertThat(saved.getCustomerEmail()).isEqualTo("customer@example.com");
            assertThat(saved.getCustomerFirstName()).isEqualTo("John");
            assertThat(saved.getCustomerLastName()).isEqualTo("Doe");
            assertThat(saved.getCustomerPhoneNumber()).isEqualTo("+380991234567");
        }

        @Test
        @DisplayName("Should save order with NEW status")
        void shouldSaveOrder_withNewStatus() {
            Phone phone = buildPhoneInStock(1L, BigDecimal.valueOf(999), 10);
            when(phoneRepository.findAllById(any())).thenReturn(List.of(phone));
            when(paymentMockService.pay(any(), any())).thenReturn(successfulPayment());

            orderCreatorService.create(cardCourierRequest(List.of(item(1L, 1))), null);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.NEW);
        }

        @Test
        @DisplayName("Should save order with correct number of order items")
        void shouldSaveOrder_withCorrectNumberOfItems() {
            Phone phone1 = buildPhoneInStock(1L, BigDecimal.valueOf(999), 10);
            Phone phone2 = buildPhoneInStock(2L, BigDecimal.valueOf(500), 10);
            when(phoneRepository.findAllById(any())).thenReturn(List.of(phone1, phone2));
            when(paymentMockService.pay(any(), any())).thenReturn(successfulPayment());

            orderCreatorService.create(
                    cardCourierRequest(List.of(item(1L, 1), item(2L, 2))), null);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            assertThat(captor.getValue().getItems()).hasSize(2);
        }

        @Test
        @DisplayName("Should set unit price from DB value, not from client request")
        void shouldSetUnitPrice_fromDatabase() {
            Phone phone = buildPhoneInStock(1L, BigDecimal.valueOf(999), 10);
            when(phoneRepository.findAllById(any())).thenReturn(List.of(phone));
            when(paymentMockService.pay(any(), any())).thenReturn(successfulPayment());

            orderCreatorService.create(cardCourierRequest(List.of(item(1L, 1))), null);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            assertThat(captor.getValue().getItems().get(0).getUnitPrice())
                    .isEqualByComparingTo(BigDecimal.valueOf(999));
        }

        @Test
        @DisplayName("Should set calculated total on the saved order entity")
        void shouldSetTotal_onSavedOrderEntity() {
            Phone phone = buildPhoneInStock(1L, BigDecimal.valueOf(500), 10);
            when(phoneRepository.findAllById(any())).thenReturn(List.of(phone));
            when(paymentMockService.pay(any(), any())).thenReturn(successfulPayment());

            orderCreatorService.create(cardCourierRequest(List.of(item(1L, 2))), null);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            assertThat(captor.getValue().getTotal())
                    .isEqualByComparingTo(BigDecimal.valueOf(1000));
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Post-order actions — notifications and response")
            // Cases:
            //   - notification is sent after successful order creation
            //   - mapper is called exactly once after order is saved
            //   - service returns the mapped response DTO
            //   - notification is NOT sent when payment fails
            // -------------------------------------------------------------------------
    class PostOrderActionTests {

        @Test
        @DisplayName("Should send email notification after successful order creation")
        void shouldSendEmailNotification_afterSuccessfulOrderCreation() {
            Phone phone = buildPhoneInStock(1L, BigDecimal.valueOf(999), 10);
            when(phoneRepository.findAllById(any())).thenReturn(List.of(phone));
            when(paymentMockService.pay(any(), any())).thenReturn(successfulPayment());

            orderCreatorService.create(cardCourierRequest(List.of(item(1L, 1))), null);

            verify(notificationSenderService).sendNotification(any(), eq(Notification_type.EMAIL));
        }

        @Test
        @DisplayName("Should not send notification when payment fails")
        void shouldNotSendNotification_whenPaymentFails() {
            Phone phone = buildPhoneInStock(1L, BigDecimal.valueOf(999), 10);
            when(phoneRepository.findAllById(any())).thenReturn(List.of(phone));
            when(paymentMockService.pay(any(), any())).thenReturn(failedPayment());

            assertThatThrownBy(() ->
                    orderCreatorService.create(cardCourierRequest(List.of(item(1L, 1))), null))
                    .isInstanceOf(PaymentFailedException.class);

            verifyNoInteractions(notificationSenderService);
        }

        @Test
        @DisplayName("Should call mapper exactly once after order is saved")
        void shouldCallMapper_exactlyOnce() {
            Phone phone = buildPhoneInStock(1L, BigDecimal.valueOf(999), 10);
            when(phoneRepository.findAllById(any())).thenReturn(List.of(phone));
            when(paymentMockService.pay(any(), any())).thenReturn(successfulPayment());

            orderCreatorService.create(cardCourierRequest(List.of(item(1L, 1))), null);

            verify(orderMapper, times(1)).toDto(any(Order.class));
        }

        @Test
        @DisplayName("Should return mapped response DTO from the mapper")
        void shouldReturnMappedResponseDto() {
            Phone phone = buildPhoneInStock(1L, BigDecimal.valueOf(999), 10);
            OrderResponseDto expectedResponse = buildStubOrderResponse();

            when(phoneRepository.findAllById(any())).thenReturn(List.of(phone));
            when(paymentMockService.pay(any(), any())).thenReturn(successfulPayment());
            when(orderMapper.toDto(any(Order.class))).thenReturn(expectedResponse);

            OrderResponseDto result = orderCreatorService.create(
                    cardCourierRequest(List.of(item(1L, 1))), null);

            assertThat(result).isEqualTo(expectedResponse);
        }
    }
}