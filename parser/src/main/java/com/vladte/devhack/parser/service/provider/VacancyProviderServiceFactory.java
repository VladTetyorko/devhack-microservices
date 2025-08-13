package com.vladte.devhack.parser.service.provider;

import com.vladte.devhack.parser.service.VacancyProviderService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Factory for creating VacancyProviderService instances based on the provider name.
 * This factory manages all available vacancy provider services and provides
 * a unified way to access them.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VacancyProviderServiceFactory {

    private final List<VacancyProviderService> providerServices;
    private final Map<String, VacancyProviderService> providerServiceMap = new HashMap<>();

    /**
     * Initialize the provider service map after dependency injection.
     */
    @PostConstruct
    public void init() {
        log.info("Initializing VacancyProviderServiceFactory with {} provider services", providerServices.size());

        for (VacancyProviderService service : providerServices) {
            String providerName = service.getProviderName().toLowerCase();
            providerServiceMap.put(providerName, service);
            log.info("Registered vacancy provider service for: {}", service.getProviderName());
        }

        log.info("VacancyProviderServiceFactory initialization completed");
    }

    /**
     * Get a vacancy provider service for the specified provider name.
     *
     * @param providerName The provider name to get a service for (case-insensitive)
     * @return The service for the specified provider, or empty if no service is found
     */
    public Optional<VacancyProviderService> getProviderService(String providerName) {
        if (providerName == null || providerName.trim().isEmpty()) {
            log.warn("Provider name is null or empty");
            return Optional.empty();
        }

        VacancyProviderService service = providerServiceMap.get(providerName.toLowerCase());
        if (service == null) {
            log.warn("No vacancy provider service found for provider: {}", providerName);
            return Optional.empty();
        }

        return Optional.of(service);
    }

    /**
     * Get all available provider names.
     *
     * @return A list of all available provider names
     */
    public List<String> getAvailableProviders() {
        return providerServices.stream()
                .map(VacancyProviderService::getProviderName)
                .toList();
    }

    /**
     * Check if a provider service is available for the given provider name.
     *
     * @param providerName The provider name to check (case-insensitive)
     * @return true if a service is available, false otherwise
     */
    public boolean isProviderAvailable(String providerName) {
        if (providerName == null || providerName.trim().isEmpty()) {
            return false;
        }
        return providerServiceMap.containsKey(providerName.toLowerCase());
    }

    /**
     * Get the number of available provider services.
     *
     * @return The number of available provider services
     */
    public int getProviderCount() {
        return providerServices.size();
    }

    /**
     * Get all provider services.
     *
     * @return A list of all provider services
     */
    public List<VacancyProviderService> getAllProviderServices() {
        return List.copyOf(providerServices);
    }
}