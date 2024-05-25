package com.example.banking.service;

import com.example.banking.model.Client;
import com.example.banking.repository.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Сервис для загрузки данных пользователя по имени пользователя и проверки доступа.
 *
 * @author nimatullah
 */
@Service
public class JwtUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(JwtUserDetailsService.class);

    private final ClientRepository clientRepository;

    public JwtUserDetailsService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    /**
     * Загружает данные пользователя по имени пользователя.
     *
     * @param username имя пользователя.
     * @return данные пользователя.
     * @throws UsernameNotFoundException если пользователь не найден.
     */
    @Override
    public UserDetails loadUserByUsername(final String username) {
        logger.info("Loading user by username: {}", username);
        final Client client = clientRepository.findByUsername(username).orElseThrow(
                () -> {
                    logger.warn("User {} not found", username);
                    return new UsernameNotFoundException("User " + username + " not found");
                });
        return new Client(username, client.getPassword());
    }

    /**
     * Проверяет, истек ли срок действия токена.
     *
     * @param expirationDate дата истечения срока действия токена.
     * @return true, если срок действия токена истек, иначе false.
     */
    private boolean isTokenExpired(Date expirationDate) {
        return expirationDate != null && expirationDate.before(new Date());
    }

    /**
     * Проверяет, аутентифицирован ли клиент.
     *
     * @return true, если клиент аутентифицирован, иначе выбрасывается AccessDeniedException.
     */
    public boolean preAuthorizeClient() {
        logger.info("Pre-authorizing client");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Access denied: Not authenticated");
            throw new AccessDeniedException("Access denied: Not authenticated");
        }
        return true;
    }

}
