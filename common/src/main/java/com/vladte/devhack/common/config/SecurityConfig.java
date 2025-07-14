package com.vladte.devhack.common.config;

import com.vladte.devhack.common.service.domain.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration class for Spring Security.
 * This class configures authentication, authorization, and other security settings.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Configure the security filter chain.
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Public resources
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        // Public pages
                        .requestMatchers("/", "/login", "/register", "/about").permitAll()
                        // Manager-only pages
                        .requestMatchers("/admin/**").hasRole("MANAGER")
                        // User-specific resources
                        .requestMatchers("/answers/**", "/notes/**", "/vacancies/my-responses/**").hasAnyRole("USER", "MANAGER")
                        // Secured pages
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
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
