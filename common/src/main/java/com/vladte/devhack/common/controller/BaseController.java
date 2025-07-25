package com.vladte.devhack.common.controller;

import com.vladte.devhack.common.service.view.BaseViewService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/**
 * Base controller with common methods for all controllers.
 */
public abstract class BaseController {

    protected BaseViewService baseViewService;

    /**
     * Constructor with BaseViewService injection.
     *
     * @param baseViewService the base view service
     */
    protected BaseController(@Qualifier("baseViewServiceImpl") BaseViewService baseViewService) {
        this.baseViewService = baseViewService;
    }

    /**
     * Default constructor for backward compatibility.
     * This will be autowired with BaseViewService after construction.
     */
    protected BaseController() {
        // Default constructor for backward compatibility
    }

    /**
     * Setter for baseViewService, used for autowiring after construction.
     *
     * @param baseViewService the base view service
     */
    @Qualifier("baseViewServiceImpl")
    public void setBaseViewService(BaseViewService baseViewService) {
        this.baseViewService = baseViewService;
    }

    /**
     * Add a page title to the model.
     *
     * @param model the model
     * @param title the page title
     */
    protected void setPageTitle(Model model, String title) {
        if (baseViewService != null) {
            baseViewService.setPageTitle(model, title);
        } else {
            // Fallback for backward compatibility
            model.addAttribute("pageTitle", title);
        }
    }

    /**
     * Get an entity from an Optional or throw a NOT_FOUND exception.
     *
     * @param optional     the Optional containing the entity
     * @param errorMessage the error message if the entity is not found
     * @param <T>          the entity type
     * @return the entity
     * @throws ResponseStatusException if the entity is not found
     */
    protected <T> T getEntityOrThrow(Optional<T> optional, String errorMessage) {
        return optional.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage));
    }
}
