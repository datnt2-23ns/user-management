package org.example.usermanagement.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.usermanagement.dto.response.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authenticationException
    ) throws IOException {

        Object accountForbidden =
                request.getAttribute(
                        JwtAuthenticationFilter
                                .ACCOUNT_FORBIDDEN_ATTRIBUTE
                );

        Object jwtError =
                request.getAttribute(
                        JwtAuthenticationFilter
                                .JWT_ERROR_ATTRIBUTE
                );

        HttpStatus status;
        String message;

        if (accountForbidden instanceof String forbiddenMessage) {
            status = HttpStatus.FORBIDDEN;
            message = forbiddenMessage;
        } else {
            status = HttpStatus.UNAUTHORIZED;

            message = jwtError instanceof String jwtMessage
                    ? jwtMessage
                    : "Bạn chưa đăng nhập hoặc token không hợp lệ";
        }

        ApiErrorResponse responseBody =
                ApiErrorResponse.builder()
                        .status(status.value())
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .errors(null)
                        .build();

        response.setStatus(status.value());
        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getOutputStream(),
                responseBody
        );
    }
}