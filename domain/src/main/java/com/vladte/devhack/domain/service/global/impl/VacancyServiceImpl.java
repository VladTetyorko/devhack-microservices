package com.vladte.devhack.domain.service.global.impl;

import com.vladte.devhack.domain.entities.global.Vacancy;
import com.vladte.devhack.domain.repository.global.VacancyRepository;
import com.vladte.devhack.domain.service.AuditableCrudService;
import com.vladte.devhack.domain.service.audit.AuditService;
import com.vladte.devhack.domain.service.global.VacancyService;
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
public class VacancyServiceImpl extends AuditableCrudService<Vacancy, UUID, VacancyRepository> implements VacancyService {

    /**
     * Constructor with repository injection.
     *
     * @param repository the vacancy repository
     */
    public VacancyServiceImpl(VacancyRepository repository, AuditService auditService) {
        super(repository, auditService);
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