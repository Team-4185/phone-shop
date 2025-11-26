package com.challengeteam.shop.exceptionHandling;

import com.challengeteam.shop.exceptionHandling.exception.*;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

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
    public ResponseEntity<ProblemDetail> handleCriticalSystemException(CriticalSystemException e) {
        log.error("A critical error occurred that should not have occurred: {}", e.getMessage(), e);

        String message = """
                Occurred an unexpected error on the server side. We are already working on it. Please, try again later.
                """;
        var problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        problem.setTitle("Unexpected Internal Server Error");
        problem.setDetail(message);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problem);
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
    public ResponseEntity<ProblemDetail> handleException(Exception e) {
        log.error("Unhandled exception: {}", e.getMessage(), e);

        String message = """
                Occurred an error on the server side. We are already working on it. Please, try again later.
                """;
        var problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        problem.setTitle("Internal Server Error");
        problem.setDetail(message);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problem);
    }

    // ======================= Logic Exception 4xx =======================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("Not found resource: {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND.value());
        problem.setTitle("Not Found Resource");
        problem.setDetail(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(problem);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ProblemDetail> handleInvalidTokenException(InvalidTokenException e) {
        log.warn("Invalid jwt: {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED.value());
        problem.setTitle("Invalid Token");
        problem.setDetail(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(problem);
    }

    @ExceptionHandler(InvalidAPIRequestException.class)
    public ResponseEntity<ProblemDetail> handleInvalidAPIRequestException(InvalidAPIRequestException e) {
        log.warn("Invalid API request: {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problem.setTitle("Bad Request");
        problem.setDetail(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problem);
    }

    @ExceptionHandler(EmailOrPasswordWrongException.class)
    public ResponseEntity<ProblemDetail> handleEmailOrPasswordWrongException(EmailOrPasswordWrongException e) {
        log.warn("Email or password are wrong: {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED.value());
        problem.setTitle("Bad Credentials");
        problem.setDetail(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(problem);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        log.warn("Email already exists: {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problem.setTitle("Email Already Taken");
        problem.setDetail(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problem);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationFailedException(AuthenticationFailedException e) {
        log.warn("Authentication failed: {}", e.getMessage());

        String message = "Authentication failed";
        var problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED.value());
        problem.setTitle("Authentication Failed");
        problem.setDetail(message);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(problem);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ProblemDetail> handleAccountLockedException(AccountLockedException e) {
        log.warn("Account locked: {}", e.getMessage());

        String message = "Account is locked";
        var problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN.value());
        problem.setTitle("Account Locked");
        problem.setDetail(message);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(problem);
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ProblemDetail> handleAccountDisabledException(AccountDisabledException e) {
        log.warn("Account disabled: {}", e.getMessage());

        String message = "Account is disabled. You can ask 'Support service' about account status";
        var problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN.value());
        problem.setTitle("Account Disabled");
        problem.setDetail(message);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(problem);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN.value());
        problem.setTitle("Access Denied");
        problem.setDetail(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(problem);
    }

    @ExceptionHandler(UnsupportedImageContentTypeException.class)
    public ResponseEntity<ProblemDetail> handleUnsupportedImageContentTypeException(UnsupportedImageContentTypeException e) {
        log.warn("Unsupported: Unsupported Content-Type {}", e.getContentType());

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problem.setTitle("Unsupported Image Content Type");
        problem.setDetail("Unsupported Content-Type = " + e.getContentType());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problem);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("Payload too large: {}", e.getMessage());

        var problem = e.getBody();
        problem.setTitle("Payload Too Large");

        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(problem);
    }

    @ExceptionHandler(PhoneAlreadyInCartException.class)
    public ResponseEntity<ProblemDetail> handlePhoneAlreadyInCartException(PhoneAlreadyInCartException e) {
        log.warn("Cart exception: {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problem.setTitle("Phone Already In Cart");
        problem.setDetail(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problem);
    }

    @ExceptionHandler(TestDataGeneratorOutOfLimitException.class)
    public ResponseEntity<ProblemDetail> handleTestDataGeneratorOutOfLimitException(TestDataGeneratorOutOfLimitException e) {
        log.warn("Test Data Generator exception: {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problem.setTitle("Amount Out Of Limit");
        problem.setDetail(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("Validation error: {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problem.setTitle("Validation Failed");
        problem.setDetail(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("Constraint violation: {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problem.setTitle("Constraint Violation");
        problem.setDetail(e.getMessage());

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ProblemDetail> handleMissingServletRequestPartException(MissingServletRequestPartException e) {
        log.warn("4xx exception: {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problem.setTitle("Missing Request Part");
        problem.setDetail(e.getMessage());

        return ResponseEntity.badRequest().body(problem);
    }

}
