package com.vladte.devhack.common.service.domain.impl;

import com.vladte.devhack.common.repository.VacancyRepository;
import com.vladte.devhack.common.service.domain.VacancyService;
import com.vladte.devhack.entities.Vacancy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the VacancyService interface.
 */
@Service
@Slf4j
public class VacancyServiceImpl extends BaseServiceImpl<Vacancy, UUID, VacancyRepository> implements VacancyService {

    /**
     * Constructor with repository injection.
     *
     * @param repository the vacancy repository
     */
    public VacancyServiceImpl(VacancyRepository repository) {
        super(repository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Vacancy> findByCompanyName(String companyName) {
        log.debug("Finding vacancies by company name: {}", companyName);
        return repository.findByCompanyName(companyName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Vacancy> findByPosition(String position) {
        log.debug("Finding vacancies by position: {}", position);
        return repository.findByPosition(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Vacancy> findBySource(String source) {
        log.debug("Finding vacancies by source: {}", source);
        return repository.findBySource(source);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Vacancy> findByRemoteAllowed(Boolean remoteAllowed) {
        log.debug("Finding vacancies by remote allowed status: {}", remoteAllowed);
        return repository.findByRemoteAllowed(remoteAllowed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Vacancy> searchByKeyword(String keyword) {
        log.debug("Searching vacancies by keyword: {}", keyword);
        return repository.searchByKeyword(keyword);
    }
}