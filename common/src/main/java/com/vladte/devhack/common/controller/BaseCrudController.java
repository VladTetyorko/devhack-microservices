package com.vladte.devhack.common.controller;

import com.vladte.devhack.common.model.dto.BaseDTO;
import com.vladte.devhack.common.model.mapper.EntityDTOMapper;
import com.vladte.devhack.common.service.domain.CrudService;
import com.vladte.devhack.common.service.view.BaseCrudViewService;
import com.vladte.devhack.common.service.view.BaseViewService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import com.vladte.devhack.entities.BasicEntity;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Base controller for CRUD operations with DTO mapping.
 * This class follows the SOLID principles:
 * - Single Responsibility: Handles only controller logic, delegates mapping to mappers and business logic to services
 * - Open/Closed: Open for extension (via generics and abstract methods) but closed for modification
 * - Liskov Substitution: Subtypes can be used without affecting the behavior of the system
 * - Interface Segregation: Uses specific interfaces for different responsibilities
 * - Dependency Inversion: Depends on abstractions (interfaces) not concrete implementations
 *
 * @param <Entity>  the entity type, must extend BasicEntity
 * @param <Dto>  the DTO type, must implement BaseDTO
 * @param <ID> the entity ID type
 * @param <Service>  the service type
 * @param <Mapper>  the mapper type
 */
public abstract class BaseCrudController<Entity extends BasicEntity, Dto extends BaseDTO, ID, Service extends CrudService<Entity, ID>, Mapper extends EntityDTOMapper<Entity, Dto>> extends BaseController {

    protected final Service service;
    protected final Mapper mapper;
    /**
     * -- SETTER --
     *  Setter for baseCrudViewService, used for autowiring after construction.
     */
    @Setter
    protected BaseCrudViewService baseCrudViewService;

    /**
     * Constructor with service, mapper, and view service injection.
     *
     * @param service             the service
     * @param mapper              the entity-DTO mapper
     * @param baseViewService     the base view service
     * @param baseCrudViewService the base CRUD view service
     */
    protected BaseCrudController(Service service,
                                 Mapper mapper,
                                 BaseViewService baseViewService,
                                 BaseCrudViewService baseCrudViewService) {
        super(baseViewService);
        this.service = service;
        this.mapper = mapper;
        this.baseCrudViewService = baseCrudViewService;
    }

    /**
     * Constructor with service and mapper injection for backward compatibility.
     *
     * @param service the service
     * @param mapper  the entity-DTO mapper
     */
    protected BaseCrudController(Service service, Mapper mapper) {
        super();
        this.service = service;
        this.mapper = mapper;
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
     * List all entities as DTOs with pagination.
     *
     * @param model the model
     * @param page  the page number (0-based)
     * @param size  the page size
     * @return the view name
     */
    @RequestMapping(method = RequestMethod.GET)
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Entity> entityPage = service.findAll(pageable);
        Page<Dto> dtoPage = toDTOPage(entityPage);

        if (baseCrudViewService != null) {
            ModelBuilder.of(model)
                    .addPagination(dtoPage, page, size, getModelAttributeName())
                    .build();
            baseCrudViewService.prepareListModel(entityPage.getContent(), getEntityName(), getListPageTitle(), model);
        } else {
            ModelBuilder.of(model)
                    .addPagination(dtoPage, page, size, getModelAttributeName())
                    .setPageTitle(getListPageTitle())
                    .build();
        }
        return getListViewName();
    }

    /**
     * View an entity as DTO.
     *
     * @param id    the entity ID
     * @param model the model
     * @return the view name
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String view(@PathVariable ID id, Model model) {
        Entity entity = getEntityOrThrow(service.findById(id), getEntityName() + " not found");
        Dto dto = mapper.toDTO(entity);

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

    /**
     * Get the model attribute name for the entity list.
     *
     * @return the model attribute name
     */
    protected String getModelAttributeName() {
        return getModelAttributeName(true);
    }

    /**
     * Get the model attribute name for the entity or entity list.
     *
     * @param plural whether the name should be plural
     * @return the model attribute name
     */
    protected String getModelAttributeName(boolean plural) {
        String name = getEntityName().toLowerCase();
        return plural ? getPluralName(name) : name;
    }

    private String getPluralName(String name) {
        if (name.endsWith("y")) {
            return name.substring(0, name.length() - 1) + "ies";
        } else {
            return name + "s";
        }
    }

    /**
     * Convert a page of entities to a page of DTOs.
     *
     * @param entityPage the page of entities
     * @return the page of DTOs
     */
    protected Page<Dto> toDTOPage(Page<Entity> entityPage) {
        List<Dto> dtoList = mapper.toDTOList(entityPage.getContent());
        return new PageImpl<>(dtoList, entityPage.getPageable(), entityPage.getTotalElements());
    }
}
