package com.vladte.devhack.common.service.view.impl;

import com.vladte.devhack.common.service.view.BaseCrudViewService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import com.vladte.devhack.domain.entities.BasicEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;

/**
 * Implementation of the BaseCrudViewService interface.
 * This class handles CRUD view-related operations.
 */
@Service
public class BaseCrudViewServiceImpl extends BaseViewServiceImpl implements BaseCrudViewService {

    @Override
    public <T extends BasicEntity> void prepareListModel(List<T> entities, String entityName, String pageTitle, Model model) {
        ModelBuilder.of(model)
                .addAttribute(getModelAttributeName(entityName), entities)
                .setPageTitle(pageTitle)
                .build();
    }

    @Override
    public <T extends BasicEntity> void prepareDetailModel(T entity, String entityName, String pageTitle, Model model) {
        ModelBuilder.of(model)
                .addAttribute(getModelAttributeName(entityName, false), entity)
                .setPageTitle(pageTitle)
                .build();
    }
}
