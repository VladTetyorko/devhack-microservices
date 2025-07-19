package com.vladte.devhack.common.controller.personalized.ui;

import com.vladte.devhack.common.controller.personalized.UserEntityController;
import com.vladte.devhack.common.model.dto.VacancyResponseDTO;
import com.vladte.devhack.common.service.domain.personalized.VacancyResponseService;
import com.vladte.devhack.common.service.domain.user.UserService;
import com.vladte.devhack.common.service.generations.VacancyParsingService;
import com.vladte.devhack.common.service.view.VacancyResponseDashboardService;
import com.vladte.devhack.common.service.view.VacancyResponseFormService;
import com.vladte.devhack.common.service.view.VacancyResponseViewService;
import com.vladte.devhack.entities.User;
import com.vladte.devhack.entities.VacancyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for handling requests related to vacancy responses.
 * This controller follows the MVC pattern with clear separation between model and view.
 * It delegates view preparation to specialized service classes to ensure proper synchronization.
 */
@Controller
@RequestMapping("/vacancies/my-responses")
public class VacancyResponseController extends UserEntityController<VacancyResponse, UUID, VacancyResponseService> {

    private static final Logger log = LoggerFactory.getLogger(VacancyResponseController.class);

    private final UserService userService;
    private final VacancyResponseViewService vacancyResponseViewService;
    private final VacancyResponseFormService vacancyResponseFormService;
    private final VacancyResponseDashboardService vacancyResponseDashboardService;
    private final VacancyParsingService vacancyParsingService;


    public VacancyResponseController(
            VacancyResponseService vacancyResponseService,
            UserService userService,
            VacancyResponseViewService vacancyResponseViewService,
            VacancyResponseFormService vacancyResponseFormService,
            VacancyResponseDashboardService vacancyResponseDashboardService,
            VacancyParsingService vacancyParsingService) {
        super(vacancyResponseService, userService);
        this.userService = userService;
        this.vacancyResponseViewService = vacancyResponseViewService;
        this.vacancyResponseFormService = vacancyResponseFormService;
        this.vacancyResponseDashboardService = vacancyResponseDashboardService;
        this.vacancyParsingService = vacancyParsingService;
    }

    @Override
    protected User getEntityUser(VacancyResponse entity) {
        return entity.getUser();
    }

    @Override
    protected String getListViewName() {
        return "vacancy-responses/list";
    }

    @Override
    protected String getDetailViewName() {
        return "vacancy-responses/view";
    }

    @Override
    protected String getListPageTitle() {
        return "Vacancy Responses";
    }

    @Override
    protected String getDetailPageTitle() {
        return "View Vacancy Response";
    }

    @Override
    protected String getEntityName() {
        return "VacancyResponse";
    }

    /**
     * List all vacancy responses for the current user with pagination.
     * This overrides the default implementation to use the specialized view service.
     *
     * @param page  the page number (zero-based)
     * @param size  the page size
     * @param model the model
     * @return the view name
     */
    @Override
    @RequestMapping(method = RequestMethod.GET)
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        log.debug("Listing vacancy responses for current user with pagination");

        // Prepare common model attributes
        prepareCommonModelAttributes(model);

        // Delegate to the view service for view-specific model preparation
        vacancyResponseViewService.prepareCurrentUserVacancyResponsesModel(page, size, model);
        vacancyResponseViewService.setCurrentUserVacancyResponsesPageTitle(model);

        return getListViewName();
    }

    /**
     * Prepares common model attributes used across multiple views.
     * This centralizes the model preparation to ensure consistency.
     *
     * @param model the model to add attributes to
     */
    private void prepareCommonModelAttributes(Model model) {
        User currentUser = getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("vacancyResponse", new VacancyResponseDTO());
    }

    /**
     * Prepares the model for displaying a vacancy response modal dialog.
     * This handles both new and edit scenarios.
     *
     * @param model  the model to add attributes to
     * @param editId the ID of the vacancy response to edit, or null for a new vacancy response
     */
    private void prepareVacancyResponseModal(Model model, UUID editId) {
        if (editId != null) {
            // If editId is provided, prepare the model for editing an existing vacancy response
            VacancyResponseDTO vacancyResponseDTO = vacancyResponseFormService.prepareEditVacancyResponseForm(editId, model);
            if (vacancyResponseDTO == null) {
                throw new IllegalArgumentException("Vacancy response not found");
            }
            vacancyResponseFormService.setEditVacancyResponsePageTitle(model);
        } else {
            // Otherwise, prepare the model for creating a new vacancy response
            vacancyResponseFormService.prepareNewVacancyResponseForm(model);
            vacancyResponseFormService.setNewVacancyResponsePageTitle(model);
        }
    }

    /**
     * Display the work dashboard with application tracking information.
     *
     * @param page  the page number for the "Top Companies" section
     * @param size  the page size for the "Top Companies" section
     * @param model the model to add attributes to
     * @return the name of the view to render
     */
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        log.debug("Displaying dashboard with access control");

        // Prepare common model attributes
        prepareCommonModelAttributes(model);

        // Delegate to the dashboard service for view-specific model preparation
        vacancyResponseDashboardService.prepareDashboardModel(page, size, model);
        vacancyResponseDashboardService.setDashboardPageTitle(model);

        return "vacancy-responses/main";
    }

    /**
     * Display the Jira-style board view with vacancy responses organized by interview stage category.
     *
     * @param model        the model to add attributes to
     * @param categoryCode the code of the interview stage category to display (optional)
     * @return the name of the view to render
     */
    @GetMapping("/board")
    public String board(Model model, @RequestParam(required = false) String categoryCode) {
        log.debug("Displaying vacancy responses board view with access control for category: {}", categoryCode);
        User currentUser = getCurrentUser();

        prepareCommonModelAttributes(model);

        vacancyResponseDashboardService.prepareBoardModelByCategory(model, currentUser, categoryCode);

        return "vacancy-responses/board";
    }

    /**
     * Search and filter vacancy responses with pagination.
     *
     * @param query  the search query
     * @param stage  the interview stage to filter by
     * @param page   the page number
     * @param size   the page size
     * @param editId the ID of the vacancy response to edit (if any)
     * @param model  the model to add attributes to
     * @return the name of the view to render
     */
    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String stage,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) UUID editId,
            Model model) {
        log.debug("Searching vacancy responses with access control and pagination, editId: {}", editId);

        // Prepare common model attributes
        prepareCommonModelAttributes(model);

        // Delegate to the view service for view-specific model preparation
        vacancyResponseViewService.prepareSearchResultsModel(query, stage, page, size, model);
        vacancyResponseViewService.setSearchResultsPageTitle(model);


        return "vacancy-responses/list";
    }

    /**
     * Display a form for creating a new vacancy response.
     *
     * @param model the model to add attributes to
     * @return the name of the view to render
     */
    @GetMapping("/new")
    public String newVacancyResponseForm(
            Model model) {
        log.debug("Displaying new vacancy response form with access control");

        // Prepare common model attributes
        prepareCommonModelAttributes(model);

        // Delegate to the form service for view-specific model preparation
        vacancyResponseFormService.prepareNewVacancyResponseForm(model);
        vacancyResponseFormService.setNewVacancyResponsePageTitle(model);

        return "vacancy-responses/form";
    }

    @Override
    public String view(@PathVariable UUID id, Model model) {
        log.debug("Viewing vacancy response with ID: {} with access control", id);

        // Get the vacancy response from the service
        VacancyResponse vacancyResponse = service.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vacancy response not found with ID: " + id));

        // Check if the current user has access to the vacancy response
        if (dontHaveAccessToEntity(vacancyResponse)) {
            log.warn("Access denied to vacancy response with ID: {}", id);
            throw new SecurityException("Access denied to vacancy response with ID: " + id);
        }

        // Prepare common model attributes
        prepareCommonModelAttributes(model);

        // Delegate to the form service for view-specific model preparation
        VacancyResponseDTO vacancyResponseDTO = vacancyResponseFormService.prepareEditVacancyResponseForm(id, model);
        if (vacancyResponseDTO == null) {
            throw new IllegalArgumentException("Vacancy response not found");
        }

        vacancyResponseFormService.setEditVacancyResponsePageTitle(model);

        return "vacancy-responses/view";
    }

    /**
     * Display a form for editing an existing vacancy response.
     *
     * @param id    the ID of the vacancy response to edit
     * @param model the model to add attributes to
     * @return the name of the view to render
     */
    @GetMapping("/{id}/edit")
    public String editVacancyResponseForm(
            @PathVariable UUID id,
            Model model) {
        log.debug("Editing vacancy response with ID: {} with access control", id);

        // Get the vacancy response from the service
        VacancyResponse vacancyResponse = service.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vacancy response not found with ID: " + id));

        // Check if the current user has access to the vacancy response
        if (dontHaveAccessToEntity(vacancyResponse)) {
            log.warn("Access denied to edit vacancy response with ID: {}", id);
            throw new SecurityException("Access denied to edit vacancy response with ID: " + id);
        }

        // Prepare common model attributes
        prepareCommonModelAttributes(model);

        // Delegate to the form service for view-specific model preparation
        VacancyResponseDTO vacancyResponseDTO = vacancyResponseFormService.prepareEditVacancyResponseForm(id, model);
        if (vacancyResponseDTO == null) {
            throw new IllegalArgumentException("Vacancy response not found");
        }

        vacancyResponseFormService.setEditVacancyResponsePageTitle(model);

        return "vacancy-responses/form";
    }

    /**
     * Save a vacancy response.
     *
     * @param vacancyResponseDTO the vacancy response data from the form
     * @param userId             the ID of the user to associate with the vacancy response
     * @return a redirect to the vacancy response list
     */
    @PostMapping
    public String saveVacancyResponse(@ModelAttribute VacancyResponseDTO vacancyResponseDTO, @RequestParam UUID userId, @RequestParam UUID interviewStageId) {
        log.debug("Saving vacancy response with access control");

        // Get the current authenticated user
        User currentUser = getCurrentUser();

        // Check if the current user is a manager or is saving their own vacancy response
        if (!isCurrentUserManager() && !currentUser.getId().equals(userId)) {
            log.warn("Access denied to save vacancy response for user with ID: {}", userId);
            throw new SecurityException("Access denied to save vacancy response for user with ID: " + userId);
        }

        // Delegate to the form service for saving the vacancy response
        VacancyResponseDTO savedResponse = vacancyResponseFormService.saveVacancyResponse(vacancyResponseDTO, userId, interviewStageId);
        if (savedResponse == null) {
            throw new IllegalArgumentException("User not found");
        }

        log.info("Vacancy response saved successfully");
        return "redirect:/vacancies/my-responses";
    }

    /**
     * Delete a vacancy response.
     *
     * @param id the ID of the vacancy response to delete
     * @return a redirect to the vacancy response list
     */
    @PostMapping("/{id}/delete")
    public String deleteVacancyResponse(@PathVariable UUID id) {
        log.debug("Deleting vacancy response with ID: {} with access control", id);

        // Get the vacancy response from the service
        VacancyResponse vacancyResponse = service.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vacancy response not found with ID: " + id));

        // Check if the current user has access to the vacancy response
        if (dontHaveAccessToEntity(vacancyResponse)) {
            log.warn("Access denied to delete vacancy response with ID: {}", id);
            throw new SecurityException("Access denied to delete vacancy response with ID: " + id);
        }

        // Delegate to the form service for deleting the vacancy response
        vacancyResponseFormService.deleteVacancyResponse(id);

        log.info("Vacancy response with ID: {} deleted successfully", id);
        return "redirect:/vacancies/my-responses";
    }

    /**
     * Process plain text vacancy and generate a vacancy response asynchronously.
     * This method accepts plain text of a vacancy, sends it to the Kafka service for processing,
     * and returns to the vacancy list view.
     *
     * @param vacancyText the plain text of the vacancy
     * @return a redirect to the vacancy response list
     */
    @PostMapping("/process-text")
    public String processVacancyText(@RequestParam String vacancyText) {
        log.debug("Processing vacancy text with access control");

        // Get the current authenticated user
        User currentUser = getCurrentUser();

        // Send the vacancy text to Kafka for processing, including the current user
        vacancyParsingService.parseVacancyText(vacancyText, currentUser);

        log.info("Vacancy text sent for processing for user: {}", currentUser.getName());
        return "redirect:/vacancies/my-responses";
    }

    /**
     * Display a list of vacancy responses for a specific user.
     *
     * @param userId           the ID of the user to find vacancy responses for
     * @param page             the page number
     * @param size             the page size
     * @param showVacancyModal whether to show the vacancy response modal
     * @param editId           the ID of the vacancy response to edit (if any)
     * @param model            the model to add attributes to
     * @return the name of the view to render
     */
    @GetMapping("/user/{userId}")
    public String getVacancyResponsesByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean showVacancyModal,
            @RequestParam(required = false) UUID editId,
            Model model) {
        log.debug("Getting vacancy responses for user with ID: {} with access control, showVacancyModal: {}, editId: {}", userId, showVacancyModal, editId);

        // Get the user from the service
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Get the current authenticated user
        User currentUser = getCurrentUser();

        // Check if the current user is a manager or is viewing their own vacancy responses
        if (!isCurrentUserManager() && !currentUser.getId().equals(userId)) {
            log.warn("Access denied to view vacancy responses for user with ID: {}", userId);
            throw new SecurityException("Access denied to view vacancy responses for user with ID: " + userId);
        }

        // Prepare common model attributes
        prepareCommonModelAttributes(model);

        // Delegate to the view service for view-specific model preparation
        User userFromService = vacancyResponseViewService.prepareUserVacancyResponsesModel(userId, page, size, model);
        if (userFromService == null) {
            throw new IllegalArgumentException("User not found");
        }

        vacancyResponseViewService.setUserVacancyResponsesPageTitle(model, user);

        // If showVacancyModal is true, prepare the model for the modal
        if (Boolean.TRUE.equals(showVacancyModal)) {
            prepareVacancyResponseModal(model, editId);
        }

        return "vacancy-responses/list";
    }
}
