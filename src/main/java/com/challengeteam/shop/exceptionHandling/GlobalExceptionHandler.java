package com.challengeteam.shop.exceptionHandling;

import com.challengeteam.shop.exceptionHandling.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.ErrorDetails;
import org.springdoc.api.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ======================= Server Error Code 5xx =======================

    /**
     * Handles critical system exceptions that indicate severe bugs or inconsistencies in the application.
     * <p>
     * This handler catches {@link CriticalSystemException}, which represents unexpected errors
     * that should never occur during normal operation (e.g., user not found immediately after creation,
     * data inconsistency between service methods).
     * <p>
     * When such an exception occurs:
     * <ul>
     *   <li>Logs the error with full stack trace at ERROR level for investigation</li>
     *   <li>Returns a generic error message to the client without exposing internal details</li>
     *   <li>Sets HTTP status 500 (Internal Server Error)</li>
     * </ul>
     * <p>
     * These exceptions indicate serious bugs that require immediate developer attention.
     * Consider setting up alerts/monitoring for this handler in production.
     *
     * @param e the critical system exception that was thrown
     * @return ResponseEntity with HTTP 500 status and a generic error message for the client
     */
    @ExceptionHandler(CriticalSystemException.class)
    public ResponseEntity<ErrorResponse> handleCriticalSystemException(CriticalSystemException e) {
        log.error("A critical error occurred that should not have occurred: {}", e.getMessage(), e);

        String message = """
                Occurred an unexpected error on the server side. We are already working on it. Please, try again later.
                """;
        var body = ErrorResponse.create(e, HttpStatus.INTERNAL_SERVER_ERROR, message);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }

    /**
     * Catches all unhandled exceptions that weren't caught by more specific handlers.
     * This serves as a safety net to ensure the application always returns a proper error response
     * instead of exposing stack traces or internal error details to clients.
     *
     * @param e any unhandled exception
     * @return ResponseEntity with HTTP 500 status and a generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception: {}", e.getMessage(), e);

        String message = """
                Occurred an unexpected error on the server side. We are already working on it. Please, try again later.
                """;
        var body = ErrorResponse.create(e, HttpStatus.INTERNAL_SERVER_ERROR, message);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }

    // ======================= Logic Exception 4xx =======================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("Not found resource: {}", e.getMessage());

        var body = ErrorResponse.create(e, HttpStatus.NOT_FOUND, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException e) {
        log.warn("Invalid jwt: {}", e.getMessage());

        var body = ErrorResponse.create(e, HttpStatus.UNAUTHORIZED, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(body);
    }


    @ExceptionHandler(InvalidAPIRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAPIRequestException(InvalidAPIRequestException e) {
        log.warn("Invalid API request: {}", e.getMessage());

        var body = ErrorResponse.create(e, HttpStatus.BAD_REQUEST, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    @ExceptionHandler(EmailOrPasswordWrongException.class)
    public ResponseEntity<ErrorResponse> handleEmailOrPasswordWrongException(EmailOrPasswordWrongException e) {
        log.warn("Email or password are wrong: {}", e.getMessage());

        var body = ErrorResponse.create(e, HttpStatus.UNAUTHORIZED, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(body);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        log.warn("Email already exists: {}", e.getMessage());

        var body = ErrorResponse.create(e, HttpStatus.BAD_REQUEST, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailedException(AuthenticationFailedException e) {
        log.warn("Authentication failed: {}", e.getMessage());

        String message = "Authentication failed. You can ask 'Support service' about authentication status";
        var body = ErrorResponse.create(e, HttpStatus.UNAUTHORIZED, message);
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(body);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLockedException(AccountLockedException e) {
        log.warn("Account locked: {}", e.getMessage());

        String message = "Account is locked. You can ask 'Support service' about account status";
        var body = ErrorResponse.create(e, HttpStatus.FORBIDDEN, message);
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(body);
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ErrorResponse> handleAccountDisabledException(AccountDisabledException e) {
        log.warn("Account disabled: {}", e.getMessage());

        String message = "Account is disabled. You can ask 'Support service' about account status";
        var body = ErrorResponse.create(e, HttpStatus.FORBIDDEN, message);
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());

        var body = ErrorResponse.create(e, HttpStatus.FORBIDDEN, "Access denied");
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(body);
    }

    @ExceptionHandler(UnsupportedImageContentTypeException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedImageContentTypeException(UnsupportedImageContentTypeException e) {
        log.warn("Unsupported: Unsupported Content-Type {}", e.getContentType());

        var body = ErrorResponse.create(e, HttpStatus.BAD_REQUEST, "Unsupported Content-Type: " + e.getContentType());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    @ExceptionHandler(PhoneAlreadyInCartException.class)
    public ResponseEntity<ErrorResponse> handlePhoneAlreadyInCartException(PhoneAlreadyInCartException e) {
        log.warn("Phone already in cart: {}", e.getMessage());

        var body = ErrorResponse.create(e, HttpStatus.BAD_REQUEST, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

}
