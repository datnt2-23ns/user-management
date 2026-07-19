package org.example.usermanagement.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.usermanagement.dto.response.ApiErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.multipart.MultipartException;

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

        for (
                FieldError fieldError
                : exception.getBindingResult().getFieldErrors()
        ) {
            fieldErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Dữ liệu không hợp lệ",
                fieldErrors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse>
    handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Dữ liệu JSON không hợp lệ hoặc sai định dạng ngày/giới tính",
                null
        );
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse>
    handleEmailAlreadyExistsException(
            EmailAlreadyExistsException exception
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse>
    handleDataIntegrityViolationException(
            DataIntegrityViolationException exception
    ) {
        log.warn(
                "Database constraint violation: {}",
                exception.getMostSpecificCause().getMessage()
        );

        return buildResponse(
                HttpStatus.CONFLICT,
                "Email đã tồn tại trong hệ thống",
                null
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse>
    handleInvalidCredentials(
            InvalidCredentialsException exception
    ) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiErrorResponse> handleLockedAccount(
            LockedException exception
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên",
                null
        );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiErrorResponse> handleDisabledAccount(
            DisabledException exception
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "Tài khoản không còn hoạt động",
                null
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException exception
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "Bạn không có quyền truy cập chức năng này",
                null
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(
            NoResourceFoundException exception
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Không tìm thấy tài nguyên hoặc API yêu cầu",
                null
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException exception
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Không tìm thấy API yêu cầu",
                null
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse>
    handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception
    ) {
        return buildResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Phương thức HTTP không được hỗ trợ cho API này",
                null
        );
    }

    @ExceptionHandler(CurrentUserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse>
    handleCurrentUserNotFound(
            CurrentUserNotFoundException exception
    ) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(
            UserNotFoundException exception
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(InvalidAvatarException.class)
    public ResponseEntity<ApiErrorResponse>
    handleInvalidAvatar(
            InvalidAvatarException exception
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                null
        );
    }

    @ExceptionHandler(
            MissingServletRequestPartException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleMissingFilePart(
            MissingServletRequestPartException exception
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Vui lòng gửi file ảnh đại diện với tên trường là file",
                null
        );
    }

    @ExceptionHandler(
            MaxUploadSizeExceededException.class
    )
    public ResponseEntity<ApiErrorResponse>
    handleMaximumUploadSizeExceeded(
            MaxUploadSizeExceededException exception
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Ảnh đại diện không được vượt quá 5 MB",
                null
        );
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiErrorResponse>
    handleMultipartException(
            MultipartException exception
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Dữ liệu upload ảnh đại diện không hợp lệ",
                null
        );
    }

    @ExceptionHandler(AvatarStorageException.class)
    public ResponseEntity<ApiErrorResponse>
    handleAvatarStorage(
            AvatarStorageException exception
    ) {
        log.error(
                "Avatar storage error",
                exception
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Không thể lưu ảnh đại diện. Vui lòng thử lại sau",
                null
        );
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

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau",
                null
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            Map<String, String> errors
    ) {
        ApiErrorResponse response =
                ApiErrorResponse.builder()
                        .status(status.value())
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .errors(errors)
                        .build();

        return ResponseEntity
                .status(status)
                .body(response);
    }
}