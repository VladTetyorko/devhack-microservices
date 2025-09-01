package com.vladte.devhack.common.config.security;

import com.vladte.devhack.domain.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration class for Spring Security.
 * This class configures authentication, authorization, and other security settings.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(UserService userService, PasswordEncoder passwordEncoder,
                          @Autowired(required = false) JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configure the security filter chain for Angular UI (JWT-based authentication).
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs
     */
    @Bean
    @Profile("angular")
    public SecurityFilterChain angularSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Public resources
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/ws/**")
                        .permitAll()
                        // Public pages
                        .requestMatchers("/", "/login", "/register", "/about")
                        .permitAll()
                        // Public API endpoints
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/password-reset/**")
                        .permitAll()
                        // API documentation
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html")
                        .permitAll()
                        // Manager-only pages
                        .requestMatchers("/admin/**")
                        .hasAnyRole("MANAGER", "ADMIN", "SYSTEM")
                        // Manager-only API endpoints
                        .requestMatchers("/api/admin/**")
                        .hasAnyRole("MANAGER", "ADMIN", "SYSTEM")
                        // User-specific resources
                        .requestMatchers("/answers/**", "/notes/**", "/vacancies/my-responses/**")
                        .hasAnyRole("USER", "MANAGER", "ADMIN", "SYSTEM")
                        // User-specific API endpoints
                        .requestMatchers("/api/answers/**", "/api/notes/**", "/api/vacancies/my-responses/**")
                        .hasAnyRole("USER", "MANAGER", "ADMIN", "SYSTEM")
                        // Secured pages and API endpoints
                        .anyRequest().authenticated()
                )
                // Disable form login for JWT-based authentication
                .formLogin(AbstractHttpConfigurer::disable)
                // Disable logout for JWT-based authentication (handled by client)
                .logout(AbstractHttpConfigurer::disable)
                // Set session management to stateless for JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Disable CSRF for stateless JWT authentication
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults());

        // Add JWT authentication filter if available
        if (jwtAuthenticationFilter != null) {
            http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }

        http
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .contentTypeOptions(contentTypeOptions -> {
                        })
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(31536000)
                                .includeSubDomains(true)
                        )
                );

        return http.build();
    }

    /**
     * Configure the security filter chain for Thymeleaf UI (form-based authentication).
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs
     */
    @Bean
    @Primary
    @Profile("thymeleaf")
    public SecurityFilterChain thymeleafSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Public resources
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**")
                        .permitAll()
                        // Public pages
                        .requestMatchers("/", "/login", "/register", "/about")
                        .permitAll()
                        // API documentation
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html")
                        .permitAll()
                        // Manager-only pages
                        .requestMatchers("/admin/**")
                        .hasAnyRole("MANAGER", "ADMIN", "SYSTEM")
                        // User-specific resources
                        .requestMatchers("/answers/**", "/notes/**", "/vacancies/my-responses/**")
                        .hasAnyRole("USER", "MANAGER", "ADMIN", "SYSTEM")
                        // Secured pages
                        .anyRequest().authenticated()
                )
                // Enable form login for Thymeleaf UI
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                // Enable logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                // Use session-based authentication
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                // Enable CSRF protection for form-based authentication
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**") // Disable CSRF for API endpoints if needed
                )
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .contentTypeOptions(contentTypeOptions -> {
                        })
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(31536000)
                                .includeSubDomains(true)
                        )
                );

        return http.build();
    }

    /**
     * Configure the authentication provider.
     *
     * @return the configured DaoAuthenticationProvider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * Configure the authentication manager.
     *
     * @param authConfig the AuthenticationConfiguration
     * @return the configured AuthenticationManager
     * @throws Exception if an error occurs
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }


}
