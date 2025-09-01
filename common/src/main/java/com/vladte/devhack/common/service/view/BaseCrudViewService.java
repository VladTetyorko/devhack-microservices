package com.vladte.devhack.common.service.view;

import com.vladte.devhack.domain.entities.BasicEntity;
import org.springframework.ui.Model;

import java.util.List;

/**
 * Service interface for CRUD view-related operations.
 * This interface follows the Single Responsibility Principle by focusing only on CRUD view-related operations.
 * It extends BaseViewService to inherit common view-related methods.
 */
public interface BaseCrudViewService extends BaseViewService {

    /**
     * Prepare the model for the list view.
     *
     * @param <T>        the entity type
     * @param entities   the list of entities to display
     * @param entityName the name of the entity
     * @param pageTitle  the page title
     * @param model      the model to add attributes to
     */
    <T extends BasicEntity> void prepareListModel(List<T> entities, String entityName, String pageTitle, Model model);

    /**
     * Prepare the model for the detail view.
     *
     * @param <T>        the entity type
     * @param entity     the entity to display
     * @param entityName the name of the entity
     * @param pageTitle  the page title
     * @param model      the model to add attributes to
     */
    <T extends BasicEntity> void prepareDetailModel(T entity, String entityName, String pageTitle, Model model);
}