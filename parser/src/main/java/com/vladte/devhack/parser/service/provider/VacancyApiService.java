package com.vladte.devhack.parser.service.provider;

import com.vladte.devhack.entities.global.Vacancy;
import com.vladte.devhack.parser.repository.VacancyRepository;
import com.vladte.devhack.parser.service.VacancyProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for handling vacancy retrieval from external APIs.
 * This service works with VacancyProviderService implementations to
 * authorize, query, and parse vacancy data from various job sites' APIs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VacancyApiService {

    private final VacancyRepository vacancyRepository;
    private final VacancyProviderServiceFactory providerServiceFactory;

    /**
     * Authorize with a specific vacancy provider.
     *
     * @param providerName The name of the vacancy provider
     * @param credentials  Map containing authentication credentials
     * @return true if authorization was successful, false otherwise
     */
    public boolean authorizeProvider(String providerName, Map<String, String> credentials) {
        log.info("Authorizing with provider: {}", providerName);

        Optional<VacancyProviderService> serviceOpt = providerServiceFactory.getProviderService(providerName);
        if (serviceOpt.isEmpty()) {
            log.error("No provider service found for: {}", providerName);
            return false;
        }

        VacancyProviderService service = serviceOpt.get();
        boolean authorized = service.authorize(credentials);

        if (authorized) {
            log.info("Successfully authorized with provider: {}", providerName);
        } else {
            log.error("Failed to authorize with provider: {}", providerName);
        }

        return authorized;
    }

    /**
     * Query and save vacancies from a specific provider's API.
     *
     * @param providerName The name of the vacancy provider
     * @param queryParams  Map containing query parameters
     * @return The number of new vacancies saved
     */
    @Transactional
    public int queryAndSaveVacancies(String providerName, Map<String, String> queryParams) {
        log.info("Querying and saving vacancies from provider: {} with params: {}", providerName, queryParams);

        Optional<VacancyProviderService> serviceOpt = providerServiceFactory.getProviderService(providerName);
        if (serviceOpt.isEmpty()) {
            log.error("No provider service found for: {}", providerName);
            return 0;
        }

        VacancyProviderService service = serviceOpt.get();

        if (!service.isAuthorized()) {
            log.error("Provider {} is not authorized. Please authorize first.", providerName);
            return 0;
        }

        try {
            // Query the API
            String apiResponse = service.query(queryParams);
            if (apiResponse == null || apiResponse.trim().isEmpty()) {
                log.warn("Empty response from provider: {}", providerName);
                return 0;
            }

            // Parse the response
            List<Vacancy> vacancies = service.parse(apiResponse);
            if (vacancies.isEmpty()) {
                log.warn("No vacancies found in API response from provider: {}", providerName);
                return 0;
            }

            log.info("Found {} vacancies from provider: {}", vacancies.size(), providerName);

            // Filter out vacancies that already exist (by URL)
            List<Vacancy> newVacancies = vacancies.stream()
                    .filter(vacancy -> {
                        if (vacancy.getUrl() == null) {
                            return true; // Keep vacancies without URL
                        }
                        return vacancyRepository.findByUrl(vacancy.getUrl()).isEmpty();
                    })
                    .toList();

            if (newVacancies.isEmpty()) {
                log.info("All vacancies from provider {} already exist in the database", providerName);
                return 0;
            }

            log.info("Saving {} new vacancies from provider: {}", newVacancies.size(), providerName);
            vacancyRepository.saveAll(newVacancies);

            return newVacancies.size();
        } catch (Exception e) {
            log.error("Error querying and saving vacancies from provider {}: {}", providerName, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Query vacancies from a provider without saving them.
     *
     * @param providerName The name of the vacancy provider
     * @param queryParams  Map containing query parameters
     * @return List of vacancies from the API
     */
    public List<Vacancy> queryVacancies(String providerName, Map<String, String> queryParams) {
        log.info("Querying vacancies from provider: {} with params: {}", providerName, queryParams);

        Optional<VacancyProviderService> serviceOpt = providerServiceFactory.getProviderService(providerName);
        if (serviceOpt.isEmpty()) {
            log.error("No provider service found for: {}", providerName);
            return List.of();
        }

        VacancyProviderService service = serviceOpt.get();

        if (!service.isAuthorized()) {
            log.error("Provider {} is not authorized. Please authorize first.", providerName);
            return List.of();
        }

        try {
            // Query the API
            String apiResponse = service.query(queryParams);
            if (apiResponse == null || apiResponse.trim().isEmpty()) {
                log.warn("Empty response from provider: {}", providerName);
                return List.of();
            }

            // Parse the response
            List<Vacancy> vacancies = service.parse(apiResponse);
            log.info("Found {} vacancies from provider: {}", vacancies.size(), providerName);

            return vacancies;
        } catch (Exception e) {
            log.error("Error querying vacancies from provider {}: {}", providerName, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Check if a provider is authorized.
     *
     * @param providerName The name of the vacancy provider
     * @return true if the provider is authorized, false otherwise
     */
    public boolean isProviderAuthorized(String providerName) {
        Optional<VacancyProviderService> serviceOpt = providerServiceFactory.getProviderService(providerName);
        if (serviceOpt.isEmpty()) {
            log.warn("No provider service found for: {}", providerName);
            return false;
        }

        return serviceOpt.get().isAuthorized();
    }

    /**
     * Get all available provider names.
     *
     * @return List of available provider names
     */
    public List<String> getAvailableProviders() {
        return providerServiceFactory.getAvailableProviders();
    }

    /**
     * Check if a provider is available.
     *
     * @param providerName The name of the vacancy provider
     * @return true if the provider is available, false otherwise
     */
    public boolean isProviderAvailable(String providerName) {
        return providerServiceFactory.isProviderAvailable(providerName);
    }

    /**
     * Get the API base URL for a specific provider.
     *
     * @param providerName The name of the vacancy provider
     * @return The API base URL, or null if provider not found
     */
    public String getProviderApiUrl(String providerName) {
        Optional<VacancyProviderService> serviceOpt = providerServiceFactory.getProviderService(providerName);
        if (serviceOpt.isEmpty()) {
            log.warn("No provider service found for: {}", providerName);
            return null;
        }

        return serviceOpt.get().getApiBaseUrl();
    }

    /**
     * Query and save vacancies from all available and authorized providers.
     *
     * @param queryParams Map containing query parameters
     * @return Total number of new vacancies saved across all providers
     */
    @Transactional
    public int queryAndSaveFromAllProviders(Map<String, String> queryParams) {
        log.info("Querying and saving vacancies from all authorized providers with params: {}", queryParams);

        List<VacancyProviderService> allServices = providerServiceFactory.getAllProviderServices();
        int totalSaved = 0;

        for (VacancyProviderService service : allServices) {
            if (service.isAuthorized()) {
                try {
                    int saved = queryAndSaveVacancies(service.getProviderName(), queryParams);
                    totalSaved += saved;
                    log.info("Saved {} vacancies from provider: {}", saved, service.getProviderName());
                } catch (Exception e) {
                    log.error("Error processing provider {}: {}", service.getProviderName(), e.getMessage());
                }
            } else {
                log.warn("Provider {} is not authorized, skipping", service.getProviderName());
            }
        }

        log.info("Total vacancies saved from all providers: {}", totalSaved);
        return totalSaved;
    }
}