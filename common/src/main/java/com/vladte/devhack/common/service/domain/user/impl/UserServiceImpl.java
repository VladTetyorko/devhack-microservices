package com.vladte.devhack.common.service.domain.user.impl;

import com.vladte.devhack.common.repository.user.UserRepository;
import com.vladte.devhack.common.service.domain.AuditableCrudService;
import com.vladte.devhack.common.service.domain.audit.AuditService;
import com.vladte.devhack.common.service.domain.user.AuthenticationProviderService;
import com.vladte.devhack.common.service.domain.user.ProfileService;
import com.vladte.devhack.common.service.domain.user.UserAccessService;
import com.vladte.devhack.common.service.domain.user.UserService;
import com.vladte.devhack.entities.enums.AuthProviderType;
import com.vladte.devhack.entities.user.AuthenticationProvider;
import com.vladte.devhack.entities.user.Profile;
import com.vladte.devhack.entities.user.User;
import com.vladte.devhack.entities.user.UserAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the UserService, delegating specific work to
 * AuthenticationProviderService, ProfileService, and UserAccessService.
 */
@Service
public class UserServiceImpl
        extends AuditableCrudService<User, UUID, UserRepository>
        implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String SYSTEM_ROLE = "SYSTEM";
    private static final String SYSTEM_USER_NAME = "system";
    private static final String SYSTEM_USER_EMAIL = "system@devhack.com";
    private static final String SYSTEM_USER_PASSWORD = "system";

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationProviderService authProviderService;
    private final ProfileService profileService;
    private final UserAccessService userAccessService;
    private final UserService self;

    public UserServiceImpl(
            UserRepository repository,
            AuditService auditService,
            PasswordEncoder passwordEncoder,
            @Lazy AuthenticationProviderService authProviderService,
            @Lazy ProfileService profileService,
            @Lazy UserAccessService userAccessService,
            @Lazy UserService self
    ) {
        super(repository, auditService);
        this.passwordEncoder = passwordEncoder;
        this.authProviderService = authProviderService;
        this.profileService = profileService;
        this.userAccessService = userAccessService;
        this.self = self;
    }

    @Override
    @Transactional
    @Cacheable(value = "users", key = "#root.methodName")
    public User getSystemUser() {
        log.debug("Getting system user");
        // Check admin settings for SYSTEM role
        List<UserAccess> admins = userAccessService.findAllByRole(SYSTEM_ROLE);
        if (!admins.isEmpty()) {
            return admins.get(0).getUser();
        }
        log.info("System user not found, creating a new one");

        // 1) Create base user
        User user = new User();
        user = super.save(user, null, "Create system user");

        // 2) Create LOCAL credentials
        AuthenticationProvider cred = new AuthenticationProvider();
        cred.setUser(user);
        cred.setProvider(AuthProviderType.LOCAL);
        cred.setEmail(SYSTEM_USER_EMAIL);
        cred.setPasswordHash(passwordEncoder.encode(SYSTEM_USER_PASSWORD));
        authProviderService.save(cred);

        // 3) Create profile
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setName(SYSTEM_USER_NAME);
        profileService.save(profile);

        // 4) Create admin settings
        UserAccess access = new UserAccess();
        access.setUser(user);
        access.setRole(SYSTEM_ROLE);
        userAccessService.save(access);

        return user;
    }

    @Override
    public void updateUsersSv(User user, String fileName, String cvUrl, String contentType) {
        User loadedUser = repository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Profile profile = loadedUser.getProfile();
        profile.setCvFileHref(cvUrl);
        profile.setCvFileName(fileName);
        profile.setCvFileType(contentType);
        save(loadedUser);
    }

    @Override
    @Transactional
    public User register(User user) {
        return registerWithRole(user, "USER", "Register base user");
    }

    @Override
    @Transactional
    public User registerManager(User user) {
        return registerWithRole(user, "MANAGER", "Register manager base user");
    }

    private User registerWithRole(User incoming, String role, String activityDesc) {
        log.debug("Registering {} user: {}", role, incoming);

        User saved = super.save(incoming, self.getSystemUser(), activityDesc);

        createLocalCredentials(saved, incoming.getLocalAuth().get().getPasswordHash());
        createProfile(saved);
        grantRole(saved, role);

        return saved;
    }


    @Override
    @Transactional
    public Optional<User> findByEmail(String email) {
        return authProviderService
                .findByProviderAndEmail(AuthProviderType.LOCAL, email)
                .map(AuthenticationProvider::getUser)
                .map(user ->
                        repository.findWithProfileById(user.getId()).get()
                );
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AuthenticationProvider cred = findLocalAuthProvider(email);
        User user = findUser(cred.getUser().getId());
        UserAccess access = findUserAccess(user.getId());
        List<SimpleGrantedAuthority> roles = mapRoles(user);

        return new org.springframework.security.core.userdetails.User(
                cred.getEmail(),
                cred.getPasswordHash(),
                /* accountNonExpired */     true,
                /* credentialsNonExpired*/  true,
                /* accountNonLocked */      !access.getIsAccountLocked(),
                /* enabled */               !access.getIsAccountLocked(),
                roles
        );
    }

    private AuthenticationProvider findLocalAuthProvider(String email) {
        return authProviderService
                .findByProviderAndEmail(AuthProviderType.LOCAL, email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("No LOCAL credentials found for " + email));
    }

    private UserAccess findUserAccess(UUID userId) {
        return userAccessService.findByUserId(userId).orElseThrow(
                () -> new UsernameNotFoundException("User access settings not found for ID " + userId)
        );
    }

    private User findUser(UUID userId) {
        return repository.findById(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found for ID " + userId));
    }

    private List<SimpleGrantedAuthority> mapRoles(User user) {
        return userAccessService.findByUserId(user.getId())
                .map(access ->
                        List.of(new SimpleGrantedAuthority("ROLE_" + access.getRole()))
                )
                .orElseGet(() ->
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
    }

    private void createLocalCredentials(User user, String rawPassword) {
        var local = user.getLocalAuth()
                .orElseThrow(() -> new IllegalStateException("LocalAuth must be present"));
        AuthenticationProvider cred = new AuthenticationProvider();
        cred.setUser(user);
        cred.setProvider(AuthProviderType.LOCAL);
        cred.setEmail(local.getEmail());
        cred.setPasswordHash(passwordEncoder.encode(rawPassword));
        authProviderService.save(cred);
    }

    private void createProfile(User user) {
        var name = user.getProfile() != null
                ? user.getProfile().getName()
                : user.getLocalAuth().get().getEmail();
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setName(name);
        profileService.save(profile);
    }

    private void grantRole(User user, String role) {
        UserAccess access = new UserAccess();
        access.setUser(user);
        access.setRole(role);
        userAccessService.save(access);
    }
}
