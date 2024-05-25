package com.example.banking.security;

import com.example.banking.service.JwtUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

/**
 * Фильтр для проверки JWT в каждом HTTP-запросе.
 *
 * @author nimatullah
 */

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final JwtUserDetailsService jwtUserDetailsService;
    private final JwtTokenService jwtTokenService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Autowired
    public JwtRequestFilter(JwtUserDetailsService jwtUserDetailsService, JwtTokenService jwtTokenService, HandlerExceptionResolver handlerExceptionResolver) {
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.jwtTokenService = jwtTokenService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    /**
     * Метод для фильтрации входящих запросов и проверки JWT.
     *
     * @param request     HTTP-запрос
     * @param response    HTTP-ответ
     * @param filterChain цепочка фильтров
     * @throws IOException      если произошла ошибка ввода-вывода
     * @throws ServletException если произошла ошибка сервлета
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {

        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        logger.debug("Authorization header: {}", header);

        if (header == null || !header.startsWith("Bearer ")) {
            logger.warn("JWT Token does not begin with Bearer String");

            // Пропускаем запросы без токена
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String token = header.substring(7);
            logger.debug("JWT Token: {}", token);

            String username = jwtTokenService.extractUsername(token);
            logger.debug("Extracted Username: {}", username);

            // Проверяем токен и аутентифицируем пользователя
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);
                if (jwtTokenService.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    // Устанавливаем аутентификацию в контекст безопасности
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("User authenticated: {}", username);
                }
            }

            // Пропускаем запрос к следующему фильтру
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            // Обрабатываем любые исключения, возникшие во время обработки запроса
            logger.error("Error during JWT validation", exception);
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }
    }
}
