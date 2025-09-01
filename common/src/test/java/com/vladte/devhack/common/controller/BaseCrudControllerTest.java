package com.vladte.devhack.common.controller;

import com.vladte.devhack.common.service.view.BaseCrudViewService;
import com.vladte.devhack.common.service.view.BaseViewService;
import com.vladte.devhack.domain.entities.BasicEntity;
import com.vladte.devhack.domain.model.dto.BaseDTO;
import com.vladte.devhack.domain.model.mapper.EntityDTOMapper;
import com.vladte.devhack.domain.service.CrudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseCrudControllerTest {

    // Test entity class
    static class TestEntity extends BasicEntity {
        private String name;

        public TestEntity() {
        }

        public TestEntity(UUID id, String name) {
            setId(id);
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    // Test DTO class
    static class TestDTO implements BaseDTO {
        private UUID id;
        private String name;

        public TestDTO() {
        }

        public TestDTO(UUID id, String name) {
            this.id = id;
            this.name = name;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    // Test mapper class
    static class TestMapper implements EntityDTOMapper<TestEntity, TestDTO> {
        @Override
        public TestDTO toDTO(TestEntity entity) {
            return new TestDTO(entity.getId(), entity.getName());
        }

        @Override
        public TestEntity toEntity(TestDTO dto) {
            return new TestEntity(dto.getId(), dto.getName());
        }

        @Override
        public List<TestDTO> toDTOList(List<TestEntity> entities) {
            return entities.stream().map(this::toDTO).toList();
        }

        @Override
        public List<TestEntity> toEntityList(List<TestDTO> dtos) {
            return dtos.stream().map(this::toEntity).toList();
        }

        @Override
        public TestEntity updateEntityFromDTO(TestEntity entity, TestDTO dto) {
            entity.setName(dto.getName());
            return entity;
        }
    }

    // Test service interface
    interface TestService extends CrudService<TestEntity, UUID> {
    }

    // Concrete implementation of BaseCrudController for testing
    static class TestController extends BaseCrudController<TestEntity, TestDTO, UUID, TestService, TestMapper> {

        public TestController(TestService service, TestMapper mapper, BaseViewService baseViewService, BaseCrudViewService baseCrudViewService) {
            super(service, mapper, baseViewService, baseCrudViewService);
        }

        public TestController(TestService service, TestMapper mapper) {
            super(service, mapper);
        }

        @Override
        protected String getListViewName() {
            return "test/list";
        }

        @Override
        protected String getDetailViewName() {
            return "test/detail";
        }

        @Override
        protected String getListPageTitle() {
            return "Test List";
        }

        @Override
        protected String getDetailPageTitle() {
            return "Test Detail";
        }

        @Override
        protected String getEntityName() {
            return "Test";
        }
    }

    @Mock
    private TestService testService;

    @Mock
    private BaseViewService baseViewService;

    @Mock
    private BaseCrudViewService baseCrudViewService;

    @Mock
    private Model model;

    private TestMapper testMapper;
    private TestController testController;
    private List<TestEntity> testEntities;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testMapper = new TestMapper();
        testController = new TestController(testService, testMapper, baseViewService, baseCrudViewService);

        testId = UUID.randomUUID();
        testEntities = Arrays.asList(
                new TestEntity(testId, "Test 1"),
                new TestEntity(UUID.randomUUID(), "Test 2")
        );
    }

    @Test
    void list_ShouldReturnListView() {
        // Arrange
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<TestEntity> entityPage = new PageImpl<>(testEntities, pageable, testEntities.size());

        when(testService.findAll(any(Pageable.class))).thenReturn(entityPage);

        // Act
        String viewName = testController.list(model, page, size);

        // Assert
        assertEquals("test/list", viewName);
        verify(testService).findAll(any(Pageable.class));
        verify(baseCrudViewService).prepareListModel(eq(testEntities), eq("Test"), eq("Test List"), eq(model));
    }

    @Test
    void list_ShouldFallbackToDirectModelAttribute_WhenBaseCrudViewServiceIsNull() {
        // Arrange
        testController.setBaseCrudViewService(null);
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<TestEntity> entityPage = new PageImpl<>(testEntities, pageable, testEntities.size());

        when(testService.findAll(any(Pageable.class))).thenReturn(entityPage);

        // Act
        String viewName = testController.list(model, page, size);

        // Assert
        assertEquals("test/list", viewName);
        verify(testService).findAll(any(Pageable.class));
        // Verify model was built without using baseCrudViewService
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    void view_ShouldReturnDetailView() {
        // Arrange
        TestEntity entity = testEntities.get(0);

        when(testService.findById(testId)).thenReturn(Optional.of(entity));

        // Act
        String viewName = testController.view(testId, model);

        // Assert
        assertEquals("test/detail", viewName);
        verify(testService).findById(testId);
        verify(baseCrudViewService).prepareDetailModel(eq(entity), eq("Test"), eq("Test Detail"), eq(model));
    }

    @Test
    void view_ShouldFallbackToDirectModelAttribute_WhenBaseCrudViewServiceIsNull() {
        // Arrange
        testController.setBaseCrudViewService(null);
        TestEntity entity = testEntities.get(0);

        when(testService.findById(testId)).thenReturn(Optional.of(entity));

        // Act
        String viewName = testController.view(testId, model);

        // Assert
        assertEquals("test/detail", viewName);
        verify(testService).findById(testId);
        // Verify model was built without using baseCrudViewService
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    void view_ShouldThrowResponseStatusException_WhenEntityNotFound() {
        // Arrange
        when(testService.findById(testId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            testController.view(testId, model);
        });

        assertEquals("404 NOT_FOUND \"Test not found\"", exception.getMessage());
        verify(testService).findById(testId);
    }
}
