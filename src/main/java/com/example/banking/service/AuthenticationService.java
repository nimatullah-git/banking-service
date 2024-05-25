package com.example.banking.service;

import com.example.banking.model.ClientAuthenticationRequest;
import com.example.banking.security.JwtTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Сервис для аутентификации пользователей.
 *
 * @author nimatullah
 */
@Service
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final JwtUserDetailsService jwtUserDetailsService;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(JwtUserDetailsService jwtUserDetailsService, JwtTokenService jwtTokenService, AuthenticationManager authenticationManager) {
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.jwtTokenService = jwtTokenService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Метод для аутентификации клиента.
     *
     * @param clientAuthenticationRequest запрос на аутентификацию, содержащий имя пользователя и пароль.
     * @return ResponseEntity с сообщением об успешной аутентификации или ошибке.
     * @throws AuthenticationException если аутентификация не удалась.
     */
    public ResponseEntity<String> authenticate(ClientAuthenticationRequest clientAuthenticationRequest) throws AuthenticationException {
        try {
            logger.info("Authenticating user: {}", clientAuthenticationRequest.getUsername());
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    clientAuthenticationRequest.getUsername(), clientAuthenticationRequest.getPassword()));
        } catch (AuthenticationException e) {
            logger.error("Authentication failed for user: {}", clientAuthenticationRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

        final UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(clientAuthenticationRequest.getUsername());
        logger.debug("Loaded user details for user: {}", clientAuthenticationRequest.getUsername());

        final String accessToken = jwtTokenService.generateToken(userDetails);
        logger.info("Generated access token for user: {}", clientAuthenticationRequest.getUsername());

        return ResponseEntity.ok("Authentication successful! Access token: " + accessToken);
    }
}
