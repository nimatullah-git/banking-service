package com.example.banking.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Сервис для работы с JWT токенами.
 * Генерация, извлечение и валидация JWT токенов.
 *
 * @autor nimatullah
 */

@Service
public class JwtTokenService {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenService.class);
    private static final Duration JWT_TOKEN_VALIDITY = Duration.ofMinutes(20);

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Генерация JWT токена на основе данных пользователя.
     *
     * @param userDetails данные пользователя.
     * @return сгенерированный JWT токен.
     */
    public String generateToken(UserDetails userDetails) {
        logger.info("Generating JWT token for user: {}", userDetails.getUsername());
        String token = JWT.create()
                .withSubject(userDetails.getUsername())
                .withIssuer("Unnamed Banking Service")
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(Instant.now().plus(JWT_TOKEN_VALIDITY)))
                .sign(Algorithm.HMAC512(secret.getBytes()));
        logger.debug("Generated JWT token: {}", token);
        return token;
    }

    /**
     * Извлечение имени пользователя из JWT токена.
     *
     * @param token JWT токен.
     * @return имя пользователя.
     */
    public String extractUsername(String token) {
        logger.info("Extracting username from JWT token");
        String username = JWT.require(Algorithm.HMAC512(secret.getBytes()))
                .build()
                .verify(token)
                .getSubject();
        logger.debug("Extracted username: {}", username);
        return username;
    }

    /**
     * Валидация JWT токена.
     *
     * @param token       JWT токен.
     * @param userDetails данные пользователя.
     * @return true, если токен валиден, иначе false.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        logger.info("Validating JWT token for user: {}", userDetails.getUsername());
        final String username = extractUsername(token);
        boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        logger.debug("Token is valid: {}", isValid);
        return isValid;
    }

    /**
     * Проверка, истек ли срок действия JWT токена.
     *
     * @param token JWT токен.
     * @return true, если срок действия токена истек, иначе false.
     */
    private boolean isTokenExpired(String token) {
        logger.info("Checking if JWT token is expired");
        final Date expiration = getExpirationDateFromToken(token);
        boolean isExpired = expiration.before(new Date());
        logger.debug("Token is expired: {}", isExpired);
        return isExpired;
    }

    /**
     * Получение даты истечения срока действия JWT токена.
     *
     * @param token JWT токен.
     * @return дата истечения срока действия токена.
     */
    private Date getExpirationDateFromToken(String token) {
        logger.info("Getting expiration date from JWT token");
        Date expirationDate = JWT.require(Algorithm.HMAC512(secret.getBytes()))
                .build()
                .verify(token)
                .getExpiresAt();
        logger.debug("Expiration date: {}", expirationDate);
        return expirationDate;
    }
}
