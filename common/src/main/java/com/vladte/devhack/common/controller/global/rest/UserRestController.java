package com.vladte.devhack.common.controller.global.rest;

import com.vladte.devhack.common.controller.BaseRestController;
import com.vladte.devhack.common.model.dto.user.UserDTO;
import com.vladte.devhack.common.model.mapper.user.UserMapper;
import com.vladte.devhack.common.service.domain.files.CvStorageService;
import com.vladte.devhack.common.service.domain.user.UserService;
import com.vladte.devhack.entities.user.User;
import io.jsonwebtoken.io.IOException;
import io.minio.errors.MinioException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.management.openmbean.InvalidKeyException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for managing User entities.
 * Provides RESTful API endpoints for CRUD operations on users.
 */
@RestController
@RequestMapping("/api/users")
@io.swagger.v3.oas.annotations.tags.Tag(name = "User", description = "User management API")
@Slf4j
public class UserRestController extends BaseRestController<User, UserDTO, UUID, UserService, UserMapper> {

    private final CvStorageService resumeStorageService;

    /**
     * Constructor with service and mapper injection.
     *
     * @param userService the user service
     * @param userMapper  the user mapper
     */
    public UserRestController(UserService userService, UserMapper userMapper, CvStorageService resumeStorageService) {
        super(userService, userMapper);
        this.resumeStorageService = resumeStorageService;
    }

    /**
     * Find a user by email.
     *
     * @param email the email to search for
     * @return the user with the specified email
     */
    @GetMapping("/by-email")
    @Operation(summary = "Find a user by email", description = "Returns a user with the specified email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content)
    })
    public ResponseEntity<UserDTO> findByEmail(
            @Parameter(description = "Email to search for")
            @RequestParam String email) {
        log.debug("REST request to find user by email: {}", email);
        Optional<User> user = service.findByEmail(email);
        return user.map(u -> ResponseEntity.ok(mapper.toDTO(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Register a new user.
     *
     * @param dto the user to register
     * @return the registered user
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content)
    })
    public ResponseEntity<UserDTO> register(
            @Parameter(description = "User to register")
            @Valid @RequestBody UserDTO dto) {
        log.debug("REST request to register user: {}", dto);
        User user = mapper.toEntity(dto);
        User registeredUser = service.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(registeredUser));
    }

    /**
     * Register a new manager user.
     *
     * @param dto the user to register
     * @return the registered user
     */
    @PostMapping("/register-manager")
    @Operation(summary = "Register a new manager user", description = "Registers a new manager user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Manager user registered successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content)
    })
    public ResponseEntity<UserDTO> registerManager(
            @Parameter(description = "User to register as manager")
            @Valid @RequestBody UserDTO dto) {
        log.debug("REST request to register manager user: {}", dto);
        User user = mapper.toEntity(dto);
        User registeredUser = service.registerManager(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(registeredUser));
    }

    /**
     * Get the system user.
     *
     * @return the system user
     */
    @GetMapping("/system")
    @Operation(summary = "Get the system user", description = "Returns the system user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System user found",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<UserDTO> getSystemUser() {
        log.debug("REST request to get system user");
        User systemUser = service.getSystemUser();
        return ResponseEntity.ok(mapper.toDTO(systemUser));
    }


    /**
     * Upload a CV for a given user.
     *
     * @param id   the user ID
     * @param file the CV file to upload (e.g. PDF or DOCX)
     * @return the publicly‑accessible URL of the stored CV
     */
    @PostMapping("/{id}/cv")
    @Operation(summary = "Upload a user's CV",
            description = "Stores the uploaded CV in object storage and returns its URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "CV uploaded successfully",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid file upload")
    })
    public ResponseEntity<String> uploadCv(
            @Parameter(description = "ID of the user") @PathVariable UUID id,
            @Parameter(description = "CV file") @RequestParam("file") MultipartFile file) throws IOException, java.io.IOException {
        log.debug("REST request to upload CV for user {}", id);

        User user = service.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String cvUrl = resumeStorageService.uploadUserCv(user.getId().toString(), file);

        service.updateUsersSv(user, file.getName(), cvUrl, file.getContentType());

        return ResponseEntity.status(HttpStatus.CREATED).body(cvUrl);
    }

    /**
     * Download a user's CV.
     *
     * @param id the user ID
     * @return the CV file as an attachment stream
     */
    @GetMapping("/{id}/cv")
    @Operation(summary = "Download a user's CV",
            description = "Retrieves and streams the stored CV file for the given user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CV downloaded successfully",
                    content = @Content(mediaType = "application/octet-stream")),
            @ApiResponse(responseCode = "404", description = "User or CV not found")
    })
    public ResponseEntity<InputStreamResource> downloadCv(
            @Parameter(description = "ID of the user") @PathVariable UUID id)
            throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException, java.io.IOException, java.security.InvalidKeyException {
        log.debug("REST request to download CV for user {}", id);

        User user = service.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String cvUrl = user.getProfile().getCvStoragePath() != null ? user.getProfile().getCvStoragePath() : "";
        if (cvUrl.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No CV uploaded for this user");
        }

        InputStream cvStream = resumeStorageService.downloadUserCv(cvUrl);
        InputStreamResource resource = new InputStreamResource(cvStream);

        String filename = cvUrl.substring(cvUrl.lastIndexOf('/') + 1);
        filename = URLDecoder.decode(filename, StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

}