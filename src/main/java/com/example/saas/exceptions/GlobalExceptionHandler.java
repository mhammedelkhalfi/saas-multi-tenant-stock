package com.example.saas.exceptions;

import com.example.saas.enums.ErrorCode;
import com.example.saas.response.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            final BusinessException ex,
            final HttpServletRequest request
    ) {
        final HttpStatus status = resolveHttpStatus(ex);
        log.warn("Business error [{}] {} - {}", ex.getCode(), request.getRequestURI(), ex.getMessage());
        return buildResponse(ex.getCode(), status, ex.getMessage(), request, null);
    }

    @ExceptionHandler({ResourceNotFoundException.class, EntityNotFoundException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            final Exception ex,
            final HttpServletRequest request
    ) {
        log.warn("Resource not found {} - {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(
                ErrorCode.NOT_FOUND.name(),
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            final MethodArgumentNotValidException ex,
            final HttpServletRequest request
    ) {
        final List<ErrorResponse.ValidationError> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            final String fieldName = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
            errors.add(ErrorResponse.ValidationError.builder()
                    .field(fieldName)
                    .code(error.getCode())
                    .message(error.getDefaultMessage())
                    .build());
        });

        log.warn("Validation failed on {} - {} error(s)", request.getRequestURI(), errors.size());
        return buildResponse(
                ErrorCode.VALIDATION_ERROR.name(),
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                request,
                errors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            final ConstraintViolationException ex,
            final HttpServletRequest request
    ) {
        final List<ErrorResponse.ValidationError> errors = ex.getConstraintViolations()
                .stream()
                .map(this::toValidationError)
                .toList();

        log.warn("Constraint violation on {} - {} error(s)", request.getRequestURI(), errors.size());
        return buildResponse(
                ErrorCode.VALIDATION_ERROR.name(),
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                request,
                errors
        );
    }

    @ExceptionHandler({
            InvalidRequestException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(
            final Exception ex,
            final HttpServletRequest request
    ) {
        log.warn("Bad request on {} - {}", request.getRequestURI(), ex.getMessage());
        final String message = ex instanceof HttpMessageNotReadableException
                ? "Malformed JSON request"
                : ex.getMessage();

        return buildResponse(
                ErrorCode.INVALID_REQUEST.name(),
                HttpStatus.BAD_REQUEST,
                message,
                request,
                null
        );
    }

    @ExceptionHandler({UnauthorizedException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            final Exception ex,
            final HttpServletRequest request
    ) {
        log.warn("Unauthorized access on {} - {}", request.getRequestURI(), ex.getMessage());
        final String message = ex instanceof BadCredentialsException
                ? "Login and/or password are incorrect"
                : ex.getMessage();

        return buildResponse(
                ErrorCode.UNAUTHORIZED.name(),
                HttpStatus.UNAUTHORIZED,
                message,
                request,
                null
        );
    }

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleForbidden(
            final Exception ex,
            final HttpServletRequest request
    ) {
        log.warn("Forbidden access on {} - {}", request.getRequestURI(), ex.getMessage());
        final String message = ex instanceof AccessDeniedException
                ? "You do not have permission to access this resource"
                : ex.getMessage();

        return buildResponse(
                ErrorCode.FORBIDDEN.name(),
                HttpStatus.FORBIDDEN,
                message,
                request,
                null
        );
    }

    @ExceptionHandler(DuplicateResouceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            final DuplicateResouceException ex,
            final HttpServletRequest request
    ) {
        log.warn("Duplicate resource on {} - {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(
                ErrorCode.DUPLICATE_RESOURCE.name(),
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(TenantProvisioningException.class)
    public ResponseEntity<ErrorResponse> handleTenantProvisioning(
            final TenantProvisioningException ex,
            final HttpServletRequest request
    ) {
        log.error("Tenant provisioning failed on {} - {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(
                ErrorCode.TENANT_PROVISIONING_ERROR.name(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            final DataIntegrityViolationException ex,
            final HttpServletRequest request
    ) {
        log.warn("Data integrity violation on {} - {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(
                ErrorCode.DATA_INTEGRITY_ERROR.name(),
                HttpStatus.CONFLICT,
                "Data integrity constraint violated",
                request,
                null
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            final HttpRequestMethodNotSupportedException ex,
            final HttpServletRequest request
    ) {
        log.warn("Method not supported on {} - {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(
                ErrorCode.METHOD_NOT_ALLOWED.name(),
                HttpStatus.METHOD_NOT_ALLOWED,
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            final NoResourceFoundException ex,
            final HttpServletRequest request
    ) {
        log.warn("Route not found {} - {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(
                ErrorCode.NOT_FOUND.name(),
                HttpStatus.NOT_FOUND,
                "Resource not found",
                request,
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            final Exception ex,
            final HttpServletRequest request
    ) {
        log.error("Unexpected error on {}", request.getRequestURI(), ex);
        return buildResponse(
                ErrorCode.INTERNAL_ERROR.name(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request,
                null
        );
    }

    private HttpStatus resolveHttpStatus(final BusinessException ex) {
        return switch (ex.getErrorCode()) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case DUPLICATE_RESOURCE, DATA_INTEGRITY_ERROR -> HttpStatus.CONFLICT;
            case INVALID_REQUEST, VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case METHOD_NOT_ALLOWED -> HttpStatus.METHOD_NOT_ALLOWED;
            case TENANT_PROVISIONING_ERROR, INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private ErrorResponse.ValidationError toValidationError(final ConstraintViolation<?> violation) {
        return ErrorResponse.ValidationError.builder()
                .field(violation.getPropertyPath().toString())
                .code(violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName())
                .message(violation.getMessage())
                .build();
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            final String code,
            final HttpStatus status,
            final String message,
            final HttpServletRequest request,
            final List<ErrorResponse.ValidationError> validationErrors
    ) {
        final ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .code(code)
                .message(message)
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }
}
