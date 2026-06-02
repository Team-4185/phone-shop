package com.challengeteam.shop.service.payment;

import com.challengeteam.shop.dto.payment.PaymentWebhookRequestDto;
import com.challengeteam.shop.dto.payment.PaymentWebhookResponseDto;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.payment.PaymentDetails;
import com.challengeteam.shop.entity.order.payment.PaymentStatus;
import com.challengeteam.shop.entity.payment.ProcessedPaymentEvent;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.persistence.repository.ProcessedPaymentEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProcessedPaymentEventRepository processedPaymentEventRepository;
    @Mock
    private PaymentProviderResolver paymentProviderResolver;
    @Mock
    private PaymentProvider paymentProvider;
    @InjectMocks
    private PaymentWebhookServiceImpl paymentWebhookService;

    @Test
    void shouldUpdatePaymentStatusAndStoreProcessedEvent() {
        Order order = Order.builder()
                .id(10L)
                .paymentDetails(new PaymentDetails(PaymentStatus.PENDING, "tx-1"))
                .build();
        PaymentWebhookRequestDto request =
                new PaymentWebhookRequestDto("evt-1", "tx-1", PaymentStatus.PAID, null);

        when(paymentProviderResolver.getProvider("mock")).thenReturn(paymentProvider);
        when(orderRepository.findByPaymentDetailsTransactionId("tx-1")).thenReturn(Optional.of(order));
        when(processedPaymentEventRepository.existsByExternalEventId("evt-1")).thenReturn(false);

        PaymentWebhookResponseDto response = paymentWebhookService.processWebhook("mock", request);

        assertThat(response.processed()).isTrue();
        assertThat(response.orderId()).isEqualTo(10L);
        assertThat(order.getPaymentDetails().getPaymentStatus()).isEqualTo(PaymentStatus.PAID);

        ArgumentCaptor<ProcessedPaymentEvent> eventCaptor =
                ArgumentCaptor.forClass(ProcessedPaymentEvent.class);
        verify(processedPaymentEventRepository).save(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getExternalEventId()).isEqualTo("evt-1");
        assertThat(eventCaptor.getValue().getExternalTransactionId()).isEqualTo("tx-1");
    }

    @Test
    void shouldIgnoreDuplicateWebhookEvent() {
        Order order = Order.builder()
                .id(10L)
                .paymentDetails(new PaymentDetails(PaymentStatus.PAID, "tx-1"))
                .build();
        PaymentWebhookRequestDto request =
                new PaymentWebhookRequestDto("evt-1", "tx-1", PaymentStatus.FAILED, null);

        when(paymentProviderResolver.getProvider("mock")).thenReturn(paymentProvider);
        when(orderRepository.findByPaymentDetailsTransactionId("tx-1")).thenReturn(Optional.of(order));
        when(processedPaymentEventRepository.existsByExternalEventId("evt-1")).thenReturn(true);

        PaymentWebhookResponseDto response = paymentWebhookService.processWebhook("mock", request);

        assertThat(response.processed()).isFalse();
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.PAID);
        verify(processedPaymentEventRepository, never()).save(any());
    }
}
