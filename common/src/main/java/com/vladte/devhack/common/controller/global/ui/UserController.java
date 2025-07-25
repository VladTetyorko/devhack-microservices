package com.vladte.devhack.common.controller.global.ui;

import com.vladte.devhack.common.controller.BaseCrudController;
import com.vladte.devhack.common.model.dto.user.UserDTO;
import com.vladte.devhack.common.model.mapper.user.UserMapper;
import com.vladte.devhack.common.service.domain.user.UserService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import com.vladte.devhack.entities.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for handling requests related to users.
 */
@Controller
@RequestMapping("/users")
public class UserController extends BaseCrudController<User, UserDTO, UUID, UserService, UserMapper> {


    public UserController(UserService userService, UserMapper userMapper) {
        super(userService, userMapper);
    }

    @Override
    protected String getListViewName() {
        return "users/list";
    }

    @Override
    protected String getDetailViewName() {
        return "users/view";
    }

    @Override
    protected String getListPageTitle() {
        return "Users";
    }

    @Override
    protected String getDetailPageTitle() {
        return "User Profile";
    }

    @Override
    protected String getEntityName() {
        return "User";
    }

    /**
     * Display the form for creating a new user.
     *
     * @param model the model to add attributes to
     * @return the name of the view to render
     */
    @GetMapping("/new")
    public String newUserForm(Model model) {
        ModelBuilder.of(model)
                .addAttribute("user", new User())
                .setPageTitle("Create New User")
                .build();
        return "users/form";
    }

    /**
     * Display the form for editing an existing user.
     *
     * @param id    the ID of the user to edit
     * @param model the model to add attributes to
     * @return the name of the view to render
     */
    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable UUID id, Model model) {
        User user = getEntityOrThrow(service.findById(id), "User not found");
        ModelBuilder.of(model)
                .addAttribute("user", user)
                .setPageTitle("Edit User")
                .build();
        return "users/form";
    }

    /**
     * Process the form submission for creating or updating a user.
     *
     * @param user the user data from the form
     * @return a redirect to the user list
     */
    @PostMapping
    public String saveUser(@ModelAttribute User user) {
        service.save(user);
        return "redirect:/users";
    }

    /**
     * Delete a user.
     *
     * @param id the ID of the user to delete
     * @return a redirect to the user list
     */
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable UUID id) {
        service.deleteById(id);
        return "redirect:/users";
    }
}
