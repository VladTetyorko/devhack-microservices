package com.vladte.devhack.common.controller.global.basic.ui;

import com.vladte.devhack.common.controller.BaseController;
import com.vladte.devhack.common.service.domain.UserService;
import com.vladte.devhack.common.service.view.BaseViewService;
import com.vladte.devhack.entities.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for handling authentication operations like login, logout, and registration.
 */
@Controller
public class AuthController extends BaseController {

    private final UserService userService;


    public AuthController(UserService userService, @Qualifier("baseViewServiceImpl") BaseViewService baseViewService) {
        super(baseViewService);
        this.userService = userService;
    }

    /**
     * Display the login page.
     *
     * @param model the model to add attributes to
     * @return the name of the view to render
     */
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        setPageTitle(model, "Login");
        return "auth/login";
    }

    /**
     * Display the registration page.
     *
     * @param model the model to add attributes to
     * @return the name of the view to render
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        setPageTitle(model, "Register");
        return "auth/register";
    }

    /**
     * Process the registration form submission.
     *
     * @param user               the user data from the form
     * @param bindingResult      the binding result for validation errors
     * @param redirectAttributes attributes to add to the redirect
     * @return the redirect URL
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        // Check if email already exists
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            bindingResult.rejectValue("email", "error.user", "Email already exists");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        // Set default role is handled in the service

        // Register the user
        userService.reguister(user);

        // Add success message
        redirectAttributes.addAttribute("registered", true);

        return "redirect:/login";
    }
}
