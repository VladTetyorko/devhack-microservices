package com.vladte.devhack.common.controller;

import com.vladte.devhack.common.dto.TagDTO;
import com.vladte.devhack.common.mapper.TagMapper;
import com.vladte.devhack.common.service.domain.TagService;
import com.vladte.devhack.common.service.domain.UserService;
import com.vladte.devhack.common.service.generations.QuestionGenerationOrchestrationService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import com.vladte.devhack.entities.Tag;
import com.vladte.devhack.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/tags")
public class TagController extends BaseCrudController<Tag, TagDTO, UUID, TagService, TagMapper> {

    private final TagMapper mapper;

    private final UserService userService;
    private final QuestionGenerationOrchestrationService questionGenerationOrchestrationService;

    @Autowired
    public TagController(TagService tagService, TagMapper tagMapper, UserService userService, QuestionGenerationOrchestrationService questionGenerationOrchestrationService) {
        super(tagService, tagMapper);
        this.mapper = tagMapper;
        this.userService = userService;
        this.questionGenerationOrchestrationService = questionGenerationOrchestrationService;
    }

    @Override
    protected String getListViewName() {
        return "tags/list";
    }

    @Override
    protected String getDetailViewName() {
        return "tags/view";
    }

    @Override
    protected String getListPageTitle() {
        return "Tags";
    }

    @Override
    protected String getDetailPageTitle() {
        return "Tag Details";
    }

    @Override
    protected String getEntityName() {
        return "Tag";
    }

    @Override
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size) {
        // Get all tags first to calculate progress
        List<Tag> allTags = service.findAll();

        // Get the first user for progress calculation (default user)
        Optional<User> userOpt = userService.findAll().stream().findFirst();
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Calculate progress for all tags
            allTags = service.calculateProgressForAll(allTags, user);
        }

        // Create a pageable object
        Pageable pageable = PageRequest.of(page, size);

        // Create a Page from the list
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allTags.size());

        List<Tag> pageContent = start < end ?
                allTags.subList(start, end) :
                List.of();

        // Convert entity page to DTO page
        List<TagDTO> tagDTOs = mapper.toDTOList(pageContent);
        Page<TagDTO> dtoPage = new PageImpl<>(tagDTOs, pageable, allTags.size());

        // Use ModelBuilder to build the model with pagination
        ModelBuilder.of(model)
                .addPagination(dtoPage, page, size, "tags")
                .setPageTitle("Tags")
                .build();

        return "tags/list";
    }

    @GetMapping("/new")
    public String newTagForm(Model model) {
        ModelBuilder.of(model)
                .addAttribute("tag", new TagDTO())
                .setPageTitle("Create New Tag")
                .build();
        return "tags/form";
    }

    @GetMapping("/{id}/edit")
    public String editTagForm(@PathVariable UUID id, Model model) {
        Tag tag = getEntityOrThrow(service.findById(id), "Tag not found");
        // Convert entity to DTO
        TagDTO tagDTO = mapper.toDTO(tag);

        ModelBuilder.of(model)
                .addAttribute("tag", tagDTO)
                .setPageTitle("Edit Tag")
                .build();

        return "tags/form";
    }

    @PostMapping
    public String saveTag(@ModelAttribute TagDTO tagDTO) {
        // Convert DTO to entity
        Tag tag = mapper.toEntity(tagDTO);
        service.save(tag);
        // Start the asynchronous generation process without blocking
        questionGenerationOrchestrationService.startEasyQuestionGeneration(tag.getName());
        return "redirect:/tags";
    }

    @GetMapping("/{id}/delete")
    public String deleteTag(@PathVariable UUID id) {
        service.deleteById(id);
        return "redirect:/tags";
    }

    @GetMapping("/search")
    public String searchTags(@RequestParam String name, Model model) {
        Optional<Tag> tagOpt = service.findTagByName(name);

        // Create a ModelBuilder instance
        ModelBuilder modelBuilder = ModelBuilder.of(model)
                .setPageTitle("Search Results for: " + name);

        if (tagOpt.isPresent()) {
            Tag tag = tagOpt.get();

            // Get the first user for progress calculation (default user)
            Optional<User> userOpt = userService.findAll().stream().findFirst();
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Calculate progress for the tag
                tag = service.calculateProgress(tag, user);
            }

            // Convert entity to DTO
            TagDTO tagDTO = mapper.toDTO(tag);
            modelBuilder.addAttribute("tags", List.of(tagDTO));
        } else {
            modelBuilder.addAttribute("tags", List.of())
                    .addAttribute("message", "No tags found with name: " + name);
        }

        modelBuilder.build();
        return "tags/list";
    }

    @Override
    public String view(@PathVariable UUID id, Model model) {
        Tag tag = getEntityOrThrow(service.findById(id), "Tag not found");

        Optional<User> userOpt = userService.findAll().stream().findFirst();
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            tag = service.calculateProgress(tag, user);
        }

        ModelBuilder.of(model)
                .addAttribute("tag", tag)
                .setPageTitle("Tag Details")
                .build();

        return "tags/view";
    }
}
