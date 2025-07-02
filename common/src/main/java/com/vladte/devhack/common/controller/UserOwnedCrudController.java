package com.vladte.devhack.common.controller;

import com.vladte.devhack.common.dto.BaseDTO;
import com.vladte.devhack.common.mapper.EntityDTOMapper;
import com.vladte.devhack.common.service.domain.BaseService;
import com.vladte.devhack.common.service.domain.UserService;
import com.vladte.devhack.common.service.view.BaseCrudViewService;
import com.vladte.devhack.common.service.view.BaseViewService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import com.vladte.devhack.entities.BasicEntity;
import com.vladte.devhack.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Base controller for CRUD operations on user-owned entities with DTO mapping.
 * This class extends BaseCrudController and adds access control based on user roles.
 *
 * @param <E>  the entity type
 * @param <D>  the DTO type
 * @param <ID> the entity ID type
 * @param <S>  the service type
 * @param <M>  the mapper type
 */
public abstract class UserOwnedCrudController<E extends BasicEntity, D extends BaseDTO, ID, S extends BaseService<E, ID>, M extends EntityDTOMapper<E, D>>
        extends BaseCrudController<E, D, ID, S, M> {

    private static final Logger logger = LoggerFactory.getLogger(UserOwnedCrudController.class);
    private static final String ROLE_MANAGER = "ROLE_MANAGER";

    protected final UserService userService;

    /**
     * Constructor with service, mapper, and view service injection.
     *
     * @param service             the service
     * @param mapper              the entity-DTO mapper
     * @param baseViewService     the base view service
     * @param baseCrudViewService the base CRUD view service
     * @param userService         the user service
     */
    protected UserOwnedCrudController(S service,
                                      M mapper,
                                      BaseViewService baseViewService,
                                      BaseCrudViewService baseCrudViewService,
                                      UserService userService) {
        super(service, mapper, baseViewService, baseCrudViewService);
        this.userService = userService;
    }

    /**
     * Constructor with service and mapper injection for backward compatibility.
     *
     * @param service     the service
     * @param mapper      the entity-DTO mapper
     * @param userService the user service
     */
    protected UserOwnedCrudController(S service, M mapper, UserService userService) {
        super(service, mapper);
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
     * List all entities that the current user has access to as DTOs with pagination.
     *
     * @param model the model
     * @param page  the page number (0-based)
     * @param size  the page size
     * @return the view name
     */
    @Override
    @RequestMapping(method = RequestMethod.GET)
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size) {
        logger.debug("Listing entities with access control and pagination");

        Page<E> entityPage;
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        // If the current user is a manager, show all entities with pagination
        if (isCurrentUserManager()) {
            logger.debug("Current user is a manager, showing all entities with pagination");
            entityPage = service.findAll(pageable);
        } else {
            // Otherwise, filter entities based on user access
            // Note: This is not optimal as it loads all entities and then filters them
            // A better approach would be to add a method to the service that filters by user
            logger.debug("Current user is not a manager, filtering entities");
            List<E> accessibleEntities = service.findAll().stream()
                    .filter(this::hasAccessToEntity)
                    .collect(Collectors.toList());

            // Create a Page from the filtered list
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), accessibleEntities.size());

            List<E> pageContent = start < end ?
                    accessibleEntities.subList(start, end) :
                    List.of();

            entityPage = new PageImpl<>(pageContent, pageable, accessibleEntities.size());
            logger.debug("Filtered to {} accessible entities, showing page {} with {} items",
                    accessibleEntities.size(), page, pageContent.size());
        }

        // Convert entity page to DTO page
        Page<D> dtoPage = toDTOPage(entityPage);

        if (baseCrudViewService != null) {
            // Add DTO page to model using ModelBuilder
            ModelBuilder.of(model)
                    .addPagination(dtoPage, page, size, getModelAttributeName())
                    .build();
            baseCrudViewService.prepareListModel(entityPage.getContent(), getEntityName(), getListPageTitle(), model);
        } else {
            // Fallback for backward compatibility using ModelBuilder
            ModelBuilder.of(model)
                    .addPagination(dtoPage, page, size, getModelAttributeName())
                    .setPageTitle(getListPageTitle())
                    .build();
        }

        return getListViewName();
    }

    /**
     * View an entity as DTO if the current user has access to it.
     *
     * @param id    the entity ID
     * @param model the model
     * @return the view name
     * @throws IllegalArgumentException if the entity is not found or the current user doesn't have access to it
     */
    @Override
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String view(@PathVariable ID id, Model model) {
        logger.debug("Viewing entity with ID: {} with access control", id);

        E entity = getEntityOrThrow(service.findById(id), getEntityName() + " not found");

        // Check if the current user has access to the entity
        if (!hasAccessToEntity(entity)) {
            logger.warn("Current user does not have access to entity with ID: {}", id);
            throw new IllegalArgumentException("Access denied");
        }

        // Convert entity to DTO
        D dto = mapper.toDTO(entity);

        if (baseCrudViewService != null) {
            // Add DTO to model using ModelBuilder
            ModelBuilder.of(model)
                    .addAttribute(getModelAttributeName(false), dto)
                    .build();
            baseCrudViewService.prepareDetailModel(entity, getEntityName(), getDetailPageTitle(), model);
        } else {
            // Fallback for backward compatibility using ModelBuilder
            ModelBuilder.of(model)
                    .addAttribute(getModelAttributeName(false), dto)
                    .setPageTitle(getDetailPageTitle())
                    .build();
        }

        return getDetailViewName();
    }
}
