package com.challengeteam.shop.service.payment;

import com.challengeteam.shop.exceptionHandling.exception.InvalidAPIRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentProviderResolver {
    private final String defaultProvider;
    private final Map<String, PaymentProvider> providers;

    public PaymentProviderResolver(
            @Value("${payment.provider:mock}") String defaultProvider,
            List<PaymentProvider> providers) {
        this.defaultProvider = defaultProvider;
        this.providers = providers.stream()
                .collect(Collectors.toMap(PaymentProvider::providerCode, Function.identity()));
    }

    public PaymentProvider getDefaultProvider() {
        return getProvider(defaultProvider);
    }

    public PaymentProvider getProvider(String providerCode) {
        PaymentProvider provider = providers.get(providerCode);
        if (provider == null) {
            throw new InvalidAPIRequestException("Unsupported payment provider: " + providerCode);
        }
        return provider;
    }
}
