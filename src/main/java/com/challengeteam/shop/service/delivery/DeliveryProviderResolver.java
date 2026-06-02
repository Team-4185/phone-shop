package com.challengeteam.shop.service.delivery;

import com.challengeteam.shop.exceptionHandling.exception.InvalidAPIRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DeliveryProviderResolver {
    private final String defaultProvider;
    private final Map<String, DeliveryProvider> providers;

    public DeliveryProviderResolver(
            @Value("${delivery.provider:mock}") String defaultProvider,
            List<DeliveryProvider> providers) {
        this.defaultProvider = defaultProvider;
        this.providers = providers.stream()
                .collect(Collectors.toMap(DeliveryProvider::providerCode, Function.identity()));
    }

    public List<DeliveryProvider> getProviders() {
        return List.copyOf(providers.values());
    }

    public DeliveryProvider getDefaultProvider() {
        return getProvider(defaultProvider);
    }

    public DeliveryProvider getProvider(String providerCode) {
        DeliveryProvider provider = providers.get(providerCode);
        if (provider == null) {
            throw new InvalidAPIRequestException("Unsupported delivery provider: " + providerCode);
        }
        return provider;
    }
}
