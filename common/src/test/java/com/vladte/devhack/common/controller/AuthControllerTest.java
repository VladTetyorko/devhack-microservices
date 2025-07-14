package com.vladte.devhack.common.controller;

import com.vladte.devhack.common.controller.global.basic.ui.AuthController;
import com.vladte.devhack.common.service.domain.UserService;
import com.vladte.devhack.common.service.view.BaseViewService;
import com.vladte.devhack.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private BaseViewService baseViewService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AuthController authController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setName("Test User");
    }

    @Test
    void showLoginForm_ShouldReturnLoginView() {
        // Act
        String viewName = authController.showLoginForm(model);

        // Assert
        assertEquals("auth/login", viewName);
        verify(baseViewService).setPageTitle(eq(model), eq("Login"));
    }

    @Test
    void showRegistrationForm_ShouldReturnRegisterView() {
        // Act
        String viewName = authController.showRegistrationForm(model);

        // Assert
        assertEquals("auth/register", viewName);
        verify(baseViewService).setPageTitle(eq(model), eq("Register"));
        verify(model).addAttribute(eq("user"), any(User.class));
    }

    @Test
    void registerUser_ShouldRedirectToLogin_WhenSuccessful() {
        // Arrange
        when(userService.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());
        when(bindingResult.hasErrors()).thenReturn(false);

        // Act
        String viewName = authController.registerUser(testUser, bindingResult, redirectAttributes);

        // Assert
        assertEquals("redirect:/login", viewName);
        verify(userService).reguister(testUser);
        verify(redirectAttributes).addAttribute("registered", true);
    }

    @Test
    void registerUser_ShouldReturnRegisterView_WhenEmailExists() {
        // Arrange
        when(userService.findByEmail(testUser.getEmail())).thenReturn(Optional.of(new User()));

        // Act
        String viewName = authController.registerUser(testUser, bindingResult, redirectAttributes);

        // Assert
        assertEquals("auth/register", viewName);
        verify(bindingResult).rejectValue("email", "error.user", "Email already exists");
        verify(userService, never()).reguister(any(User.class));
    }

    @Test
    void registerUser_ShouldReturnRegisterView_WhenValidationErrors() {
        // Arrange
        when(userService.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());
        when(bindingResult.hasErrors()).thenReturn(true);

        // Act
        String viewName = authController.registerUser(testUser, bindingResult, redirectAttributes);

        // Assert
        assertEquals("auth/register", viewName);
        verify(userService, never()).reguister(any(User.class));
    }
}
