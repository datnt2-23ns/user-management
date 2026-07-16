package org.example.usermanagement.exception;

import org.example.usermanagement.dto.response.ApiErrorResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> fieldErrors =
                new LinkedHashMap<>();

        for (FieldError fieldError
                : exception.getBindingResult().getFieldErrors()) {

            fieldErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        ApiErrorResponse response =
                ApiErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Dữ liệu đăng ký không hợp lệ")
                        .timestamp(LocalDateTime.now())
                        .errors(fieldErrors)
                        .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse>
    handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception
    ) {
        ApiErrorResponse response =
                ApiErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(
                                "Dữ liệu JSON không hợp lệ hoặc sai định dạng ngày/giới tính"
                        )
                        .timestamp(LocalDateTime.now())
                        .errors(null)
                        .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse>
    handleEmailAlreadyExistsException(
            EmailAlreadyExistsException exception
    ) {
        ApiErrorResponse response =
                ApiErrorResponse.builder()
                        .status(HttpStatus.CONFLICT.value())
                        .message(exception.getMessage())
                        .timestamp(LocalDateTime.now())
                        .errors(null)
                        .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse>
    handleDataIntegrityViolationException(
            DataIntegrityViolationException exception
    ) {
        log.warn(
                "Database constraint violation",
                exception
        );

        ApiErrorResponse response =
                ApiErrorResponse.builder()
                        .status(HttpStatus.CONFLICT.value())
                        .message("Email đã tồn tại trong hệ thống")
                        .timestamp(LocalDateTime.now())
                        .errors(null)
                        .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse>
    handleUnexpectedException(
            Exception exception
    ) {
        log.error(
                "Unexpected server error",
                exception
        );

        ApiErrorResponse response =
                ApiErrorResponse.builder()
                        .status(
                                HttpStatus.INTERNAL_SERVER_ERROR.value()
                        )
                        .message(
                                "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau"
                        )
                        .timestamp(LocalDateTime.now())
                        .errors(null)
                        .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}