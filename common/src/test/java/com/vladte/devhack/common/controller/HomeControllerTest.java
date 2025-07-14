package com.vladte.devhack.common.controller;

import com.vladte.devhack.common.controller.global.basic.ui.HomeController;
import com.vladte.devhack.common.service.view.BaseViewService;
import com.vladte.devhack.common.service.view.DashboardViewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private DashboardViewService dashboardViewService;

    @Mock
    private BaseViewService baseViewService;

    @Mock
    private Model model;

    @InjectMocks
    private HomeController homeController;

    @BeforeEach
    void setUp() {
        // Set the baseViewService in the controller
        ReflectionTestUtils.setField(homeController, "baseViewService", baseViewService);
    }

    @Test
    void home_ShouldReturnHomeView() {
        // Arrange
        // No specific arrangement needed for dashboardViewService mock

        // Act
        String viewName = homeController.home(model);

        // Assert
        assertEquals("home", viewName);
        verify(dashboardViewService).prepareDashboardModel(model);
        verify(baseViewService).setPageTitle(eq(model), eq("DevHack - Interview Preparation Dashboard"));
    }

    @Test
    void about_ShouldReturnAboutView() {
        // Arrange
        // No specific arrangement needed

        // Act
        String viewName = homeController.about(model);

        // Assert
        assertEquals("about", viewName);
        verify(baseViewService).setPageTitle(eq(model), eq("About DevHack"));
    }

    @Test
    void home_ShouldFallbackToDirectModelAttribute_WhenBaseViewServiceIsNull() {
        // Arrange
        ReflectionTestUtils.setField(homeController, "baseViewService", null);

        // Act
        String viewName = homeController.home(model);

        // Assert
        assertEquals("home", viewName);
        verify(dashboardViewService).prepareDashboardModel(model);
        verify(model).addAttribute(eq("pageTitle"), eq("DevHack - Interview Preparation Dashboard"));
    }

    @Test
    void about_ShouldFallbackToDirectModelAttribute_WhenBaseViewServiceIsNull() {
        // Arrange
        ReflectionTestUtils.setField(homeController, "baseViewService", null);

        // Act
        String viewName = homeController.about(model);

        // Assert
        assertEquals("about", viewName);
        verify(model).addAttribute(eq("pageTitle"), eq("About DevHack"));
    }
}
