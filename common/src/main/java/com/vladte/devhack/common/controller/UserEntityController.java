package com.vladte.devhack.common.controller;

import com.vladte.devhack.common.service.domain.BaseService;
import com.vladte.devhack.common.service.domain.UserService;
import com.vladte.devhack.common.service.view.BaseViewService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import com.vladte.devhack.entities.BasicEntity;
import com.vladte.devhack.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Base controller for operations on user-owned entities.
 * This class extends BaseController and adds access control based on user roles.
 *
 * @param <E>  the entity type, must extend BasicEntity
 * @param <ID> the entity ID type
 * @param <S>  the service type
 */
public abstract class UserEntityController<E extends BasicEntity, ID, S extends BaseService<E, ID>> extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(UserEntityController.class);
    private static final String ROLE_MANAGER = "ROLE_MANAGER";

    protected final S service;
    protected final UserService userService;

    /**
     * Constructor with service and userService injection.
     *
     * @param service         the service
     * @param userService     the user service
     * @param baseViewService the base view service
     */
    protected UserEntityController(S service, UserService userService, BaseViewService baseViewService) {
        super(baseViewService);
        this.service = service;
        this.userService = userService;
    }

    /**
     * Constructor with service and userService injection for backward compatibility.
     *
     * @param service     the service
     * @param userService the user service
     */
    protected UserEntityController(S service, UserService userService) {
        super();
        this.service = service;
        this.userService = userService;
    }

    /**
     * Get the user associated with the entity.
     *
     * @param entity the entity
     * @return the user associated with the entity
     */
    protected abstract User getEntityUser(E entity);

    /**
     * Get the current authenticated user.
     *
     * @return the current user
     * @throws IllegalStateException if the current user is not found
     */
    protected User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        return userService.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }

    /**
     * Check if the current user is a manager.
     *
     * @return true if the current user is a manager, false otherwise
     */
    protected boolean isCurrentUserManager() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority(ROLE_MANAGER));
    }

    /**
     * Check if the current user has access to the entity.
     *
     * @param entity the entity to check
     * @return true if the current user has access to the entity, false otherwise
     */
    protected boolean hasAccessToEntity(E entity) {
        // Managers have access to all entities
        if (isCurrentUserManager()) {
            return true;
        }

        // Users have access only to their own entities
        User entityUser = getEntityUser(entity);
        User currentUser = getCurrentUser();

        return entityUser != null && entityUser.getId().equals(currentUser.getId());
    }

    /**
     * Get the view name for the list page.
     *
     * @return the view name
     */
    protected abstract String getListViewName();

    /**
     * Get the view name for the detail page.
     *
     * @return the view name
     */
    protected abstract String getDetailViewName();

    /**
     * Get the page title for the list page.
     *
     * @return the page title
     */
    protected abstract String getListPageTitle();

    /**
     * Get the page title for the detail page.
     *
     * @return the page title
     */
    protected abstract String getDetailPageTitle();

    /**
     * Get the entity name for error messages.
     *
     * @return the entity name
     */
    protected abstract String getEntityName();

    /**
     * List all entities that the current user has access to with pagination.
     *
     * @param page  the page number (zero-based)
     * @param size  the page size
     * @param model the model
     * @return the view name
     */
    @RequestMapping(method = RequestMethod.GET)
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        logger.debug("Listing entities with access control and pagination");

        // Get the current authenticated user
        User currentUser = getCurrentUser();

        // Create pageable object
        Pageable pageable = PageRequest.of(page, size);

        // Get entities with pagination
        // The service layer should filter entities based on user access
        Page<E> entityPage = service.findAll(pageable);

        // Using ModelBuilder to build the model with pagination data
        ModelBuilder.of(model)
                .addPagination(entityPage, page, size, "items")
                .setPageTitle(getListPageTitle())
                .addAttribute("currentUser", currentUser)
                .build();

        return getListViewName();
    }

    /**
     * View an entity if the current user has access to it.
     *
     * @param id    the entity ID
     * @param model the model
     * @return the view name
     */
    @GetMapping("/{id}")
    public String view(@PathVariable ID id, Model model) {
        logger.debug("Viewing entity with ID: {} with access control", id);

        // Get the entity from the service
        E entity = service.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(getEntityName() + " not found with ID: " + id));

        // Check if the current user has access to the entity
        if (!hasAccessToEntity(entity)) {
            logger.warn("Access denied to {} with ID: {}", getEntityName(), id);
            throw new SecurityException("Access denied to " + getEntityName() + " with ID: " + id);
        }

        // Get the current authenticated user
        User currentUser = getCurrentUser();

        // Add the entity and current user to the model
        ModelBuilder.of(model)
                .addAttribute("item", entity)
                .addAttribute("currentUser", currentUser)
                .setPageTitle(getDetailPageTitle())
                .build();

        return getDetailViewName();
    }
}
