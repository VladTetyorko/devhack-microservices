package com.vladte.devhack.common.service.view.impl;

import com.vladte.devhack.common.service.view.BaseViewService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

/**
 * Implementation of the BaseViewService interface.
 * This class handles basic view-related operations.
 */
@Service
public class BaseViewServiceImpl implements BaseViewService {

    @Override
    public void setPageTitle(Model model, String title) {
        ModelBuilder.of(model)
                .setPageTitle(title)
                .build();
    }

    @Override
    public String getModelAttributeName(String entityName) {
        return getModelAttributeName(entityName, true);
    }

    @Override
    public String getModelAttributeName(String entityName, boolean plural) {
        String name = entityName.toLowerCase();
        return plural ? name + "s" : name;
    }
}
