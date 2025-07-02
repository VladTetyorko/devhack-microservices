package com.vladte.devhack.common.controller;

import com.vladte.devhack.common.service.view.DashboardViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling requests to the home page and other general pages.
 */
@Controller
public class HomeController extends BaseController {


    private final DashboardViewService dashboardViewService;

    @Autowired
    public HomeController(
            DashboardViewService dashboardViewService) {
        this.dashboardViewService = dashboardViewService;
    }

    /**
     * Display the home page with progress tracking information.
     *
     * @param model the model to add attributes to
     * @return the name of the view to render
     */
    @GetMapping("/")
    public String home(Model model) {
        dashboardViewService.prepareDashboardModel(model);
        setPageTitle(model, "DevHack - Interview Preparation Dashboard");
        return "home";
    }

    /**
     * Display the about page.
     *
     * @param model the model to add attributes to
     * @return the name of the view to render
     */
    @GetMapping("/about")
    public String about(Model model) {
        setPageTitle(model, "About DevHack");
        return "about";
    }
}
