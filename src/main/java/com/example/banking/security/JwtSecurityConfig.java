package com.example.banking.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * @author nimatullah
 */

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class JwtSecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(JwtSecurityConfig.class);

    private final JwtRequestFilter jwtRequestFilter;

    @Autowired
    public JwtSecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    /**
     * Конфигурация безопасности HTTP запросов.
     *
     * @param http объект HttpSecurity для конфигурирования безопасности HTTP запросов.
     * @return объект SecurityFilterChain с настройками безопасности.
     * @throws Exception в случае ошибки конфигурирования безопасности.
     */
    @Bean
    public SecurityFilterChain configure(final HttpSecurity http) throws Exception {
        logger.info("Configuring HTTP security");

        return http.cors(withDefaults())  // Разрешение CORS с настройками по умолчанию
                .csrf(AbstractHttpConfigurer::disable)  // Отключение защиты CSRF
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/api/clients/register", "/api/clients/authenticate").permitAll()  // Разрешение доступа к указанным URL без аутентификации
                        .anyRequest().authenticated()  // Требование аутентификации для всех остальных URL
                )
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // Настройка политики управления сессиями как stateless
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)  // Добавление фильтра JWT перед фильтром аутентификации по логину и паролю
                .build();
    }

    /**
     * Создание AuthenticationManager на основе конфигурации аутентификации.
     *
     * @param authenticationConfiguration конфигурация аутентификации.
     * @return объект AuthenticationManager.
     * @throws Exception в случае ошибки создания AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration authenticationConfiguration) throws Exception {
        logger.info("Creating AuthenticationManager");
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Создание PasswordEncoder для кодирования паролей.
     *
     * @return объект PasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.info("Creating PasswordEncoder");
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Настройка WebSecurity для игнорирования определенных URL.
     *
     * @return объект WebSecurityCustomizer с настройками игнорирования URL.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        logger.info("Configuring WebSecurity to ignore specific URLs");
        return (web) -> web.ignoring().requestMatchers(
                "/swagger-ui.html", "/swagger-ui/**",
                "/v3/api-docs/**", "/v3/api-docs.yaml",
                "/swagger-resources", "/swagger-resources/**",
                "/configuration/ui", "/configuration/security",
                "/swagger-ui/**", "/webjars/**");
    }
}
