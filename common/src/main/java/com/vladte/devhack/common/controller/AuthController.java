package com.vladte.devhack.common.controller;

import com.vladte.devhack.common.service.domain.UserService;
import com.vladte.devhack.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

/**
 * Controller for handling authentication-related requests.
 */
@Controller
public class AuthController extends BaseController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Display the login page.
     *
     * @param model the model to add attributes to
     * @return the name of the view to render
     */
    @GetMapping("/login")
    public String login(Model model) {
        setPageTitle(model, "Login - DevHack");
        return "auth/login";
    }

    /**
     * Display the registration page.
     *
     * @param model the model to add attributes to
     * @return the name of the view to render
     */
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        setPageTitle(model, "Register - DevHack");
        return "auth/register";
    }

    /**
     * Process the registration form submission.
     *
     * @param user   the user data from the form
     * @param result the binding result for validation
     * @param model  the model to add attributes to
     * @return the name of the view to render
     */
    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute User user, BindingResult result, Model model) {
        // Check if email already exists
        Optional<User> existingUser = userService.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            result.rejectValue("email", "error.user", "An account already exists for this email.");
        }

        if (result.hasErrors()) {
            setPageTitle(model, "Register - DevHack");
            return "auth/register";
        }

        userService.reguister(user);

        // Redirect to login page with success message
        return "redirect:/login?registered";
    }

    /**
     * Display the dashboard page.
     *
     * @param model the model to add attributes to
     * @return the name of the view to render
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        setPageTitle(model, "Dashboard - DevHack");
        return "dashboard";
    }
}