package com.challengeteam.shop.service.admin.impl;

import com.challengeteam.shop.dto.payment.TransactionResult;
import com.challengeteam.shop.entity.order.DeliveryMethod;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.entity.order.payment.PaymentDetails;
import com.challengeteam.shop.entity.order.payment.PaymentMethod;
import com.challengeteam.shop.entity.order.payment.PaymentStatus;
import com.challengeteam.shop.exceptionHandling.exception.order.PaymentFailedException;
import com.challengeteam.shop.mapper.admin.AdminOrderMapper;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.service.admin.AdminOrderWorkflowService;
import com.challengeteam.shop.service.notification.NotificationSenderService;
import com.challengeteam.shop.service.payment.PaymentProvider;
import com.challengeteam.shop.service.payment.PaymentProviderResolver;
import com.challengeteam.shop.utility.notification.email.OrderStatusEmailBuilder;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceImplTest {
  @Mock private OrderRepository orderRepository;
  @Mock private AdminOrderMapper adminOrderMapper;
  @Mock private AdminOrderWorkflowService adminOrderWorkflowService;
  @Mock private NotificationSenderService notificationSenderService;
  @Mock private OrderStatusEmailBuilder orderStatusEmailBuilder;
  @Mock private PaymentProviderResolver paymentProviderResolver;
  @Mock private PaymentProvider paymentProvider;

  @Test
  void shouldNotSaveCancelledOrderWhenRefundFails() {
    AdminOrderServiceImpl adminOrderService = adminOrderService();
    Order order = paidCardOrder();

    when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));
    when(adminOrderWorkflowService.resolveTargetStatus(OrderStatus.NEW, "cancel"))
        .thenReturn(OrderStatus.CANCELLED);
    when(paymentProviderResolver.getProvider("stripe")).thenReturn(paymentProvider);
    when(paymentProvider.refund("pi_test", new BigDecimal("100.00")))
        .thenReturn(new TransactionResult(PaymentStatus.FAILED, "pi_test", "Refund declined"));

    assertThatThrownBy(() -> adminOrderService.applyAction(1L, "cancel"))
        .isInstanceOf(PaymentFailedException.class)
        .hasMessage("Refund declined");

    verify(orderRepository, never()).save(order);
    verify(adminOrderMapper, never()).toDetails(order);
    verify(notificationSenderService, never()).sendNotification(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
  }

  @Test
  void shouldNotRefundPaidOrderWithoutTransactionId() {
    AdminOrderServiceImpl adminOrderService = adminOrderService();
    Order order = paidCardOrder();
    order.getPaymentDetails().setTransactionId(null);

    when(orderRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(order));
    when(adminOrderWorkflowService.resolveTargetStatus(OrderStatus.NEW, "cancel"))
        .thenReturn(OrderStatus.CANCELLED);

    assertThatThrownBy(() -> adminOrderService.applyAction(1L, "cancel"))
        .isInstanceOf(PaymentFailedException.class)
        .hasMessage("Cannot refund paid order without payment transaction id");

    verify(paymentProviderResolver, never()).getProvider(org.mockito.ArgumentMatchers.any());
    verify(orderRepository, never()).save(order);
  }

  private AdminOrderServiceImpl adminOrderService() {
    return new AdminOrderServiceImpl(
        orderRepository,
        adminOrderMapper,
        adminOrderWorkflowService,
        notificationSenderService,
        orderStatusEmailBuilder,
        paymentProviderResolver);
  }

  private Order paidCardOrder() {
    return Order.builder()
        .id(1L)
        .customerEmail("customer@example.com")
        .status(OrderStatus.NEW)
        .paymentMethod(PaymentMethod.CARD)
        .paymentProvider("stripe")
        .paymentDetails(new PaymentDetails(PaymentStatus.PAID, "pi_test"))
        .deliveryMethod(DeliveryMethod.PICKUP)
        .deliveryProvider("mock")
        .total(new BigDecimal("100.00"))
        .build();
  }
}
