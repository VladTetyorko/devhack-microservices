package com.vladte.devhack.common.service.view;

import org.springframework.data.domain.Page;
import org.springframework.ui.Model;

import java.util.function.Consumer;

/**
 * A generic builder for constructing Spring MVC models.
 * This class provides a fluent API for adding attributes to a model using lambda functions.
 * It follows the builder pattern to allow chaining of method calls.
 */
public class ModelBuilder {

    private final Model model;

    /**
     * Private constructor to enforce the use of static factory methods.
     *
     * @param model the Spring MVC model to build
     */
    private ModelBuilder(Model model) {
        this.model = model;
    }

    /**
     * Create a new ModelBuilder for the given model.
     *
     * @param model the Spring MVC model to build
     * @return a new ModelBuilder instance
     */
    public static ModelBuilder of(Model model) {
        return new ModelBuilder(model);
    }

    /**
     * Add an attribute to the model.
     *
     * @param name  the attribute name
     * @param value the attribute value
     * @return this ModelBuilder instance for method chaining
     */
    public ModelBuilder addAttribute(String name, Object value) {
        model.addAttribute(name, value);
        return this;
    }

    /**
     * Set the page title in the model.
     *
     * @param title the page title
     * @return this ModelBuilder instance for method chaining
     */
    public ModelBuilder setPageTitle(String title) {
        model.addAttribute("pageTitle", title);
        return this;
    }

    /**
     * Apply a consumer function to the model.
     *
     * @param modelConsumer a consumer function that accepts the model
     * @return this ModelBuilder instance for method chaining
     */
    public ModelBuilder apply(Consumer<Model> modelConsumer) {
        modelConsumer.accept(model);
        return this;
    }

    /**
     * Add pagination data to the model.
     *
     * @param <T>                  the type of the page content
     * @param page                 the page object
     * @param currentPage          the current page number
     * @param size                 the page size
     * @param contentAttributeName the name of the attribute for the page content
     * @return this ModelBuilder instance for method chaining
     */
    public <T> ModelBuilder addPagination(Page<T> page, int currentPage, int size, String contentAttributeName) {
        model.addAttribute(contentAttributeName, page.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("size", size);
        return this;
    }

    /**
     * Build just for code beauty.
     */
    public void build() {
    }
}
