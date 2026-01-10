package com.challengeteam.shop.exceptionHandling;

import com.challengeteam.shop.dto.validation.ValidationDetailsDto;
import com.challengeteam.shop.exceptionHandling.exception.*;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

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
        log.error("500  A critical error occurred that should not have occurred: {}", e.getMessage(), e);

        // todo: error showing should be cut out after developing end
        String message = """
                Occurred an unexpected error on the server side. We are already working on it. Please, try again later.
                
                error occurred:
                %s
                """.formatted(e.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        problem.setTitle("Unexpected Internal Server Error");
        problem.setDetail(message);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
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
        log.error("500  Unhandled exception: {}", e.getMessage(), e);

        // todo: error showing should be cut out after developing end
        String message = """
                Occurred unhandled error on the server side. We are already working on it. Please, try again later.
                
                error occurred:
                %s
                """.formatted(e.getMessage());
        var problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        problem.setTitle("Internal Server Error");
        problem.setDetail(message);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    // ======================= Logic Exception 4xx =======================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("404  {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND.value());
        problem.setTitle("Not Found Resource");
        problem.setDetail(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ProblemDetail> handleInvalidTokenException(InvalidTokenException e) {
        log.warn("401  Invalid jwt: {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED.value());
        problem.setTitle("Invalid Token");
        problem.setDetail(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler(InvalidAPIRequestException.class)
    public ResponseEntity<ProblemDetail> handleInvalidAPIRequestException(InvalidAPIRequestException e) {
        log.warn("400  {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problem.setTitle("Bad Request");
        problem.setDetail(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler(EmailOrPasswordWrongException.class)
    public ResponseEntity<ProblemDetail> handleEmailOrPasswordWrongException(EmailOrPasswordWrongException e) {
        log.warn("401  {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED.value());
        problem.setTitle("Bad Credentials");
        problem.setDetail(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        log.warn("400  {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problem.setTitle("Email Already Taken");
        problem.setDetail(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationFailedException(AuthenticationFailedException e) {
        log.warn("401  {}", e.getMessage());

        String message = "Authentication failed";
        var problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED.value());
        problem.setTitle("Authentication Failed");
        problem.setDetail(message);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ProblemDetail> handleAccountLockedException(AccountLockedException e) {
        log.warn("403  {}", e.getMessage());

        String message = "Account is locked";
        var problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN.value());
        problem.setTitle("Account Locked");
        problem.setDetail(message);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ProblemDetail> handleAccountDisabledException(AccountDisabledException e) {
        log.warn("403  {}", e.getMessage());

        String message = "Account is disabled. You can ask 'Support service' about account status";
        var problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN.value());
        problem.setTitle("Account Disabled");
        problem.setDetail(message);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("403  {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN.value());
        problem.setTitle("Access Denied");
        problem.setDetail(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler(UnsupportedImageContentTypeException.class)
    public ResponseEntity<ProblemDetail> handleUnsupportedImageContentTypeException(UnsupportedImageContentTypeException e) {
        log.warn("400  Unsupported Content-Type '{}'", e.getContentType());

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problem.setTitle("Unsupported Image Content Type");
        problem.setDetail("Unsupported Content-Type = " + e.getContentType());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("413  Payload too large: {}", e.getMessage());

        var problem = e.getBody();
        problem.setTitle("Payload Too Large");

        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler(PhoneAlreadyInCartException.class)
    public ResponseEntity<ProblemDetail> handlePhoneAlreadyInCartException(PhoneAlreadyInCartException e) {
        log.warn("400  Cart exception: {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problem.setTitle("Phone Already In Cart");
        problem.setDetail(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler(TestDataGeneratorOutOfLimitException.class)
    public ResponseEntity<ProblemDetail> handleTestDataGeneratorOutOfLimitException(TestDataGeneratorOutOfLimitException e) {
        log.warn("400  Test Data Generator exception: {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problem.setTitle("Amount Out Of Limit");
        problem.setDetail(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("400  {}", e.getMessage());

        String parameterName = e.getParameter().getParameterName();
        List<String> errorMessages = e.getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .toList();
        int countErrors = e.getErrorCount();

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problem.setTitle("Validation Failed");
        problem.setDetail("Request parameters have " + countErrors + " validation problems");
        problem.setProperty("validationDetails", new ValidationDetailsDto(parameterName, errorMessages));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("400  {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problem.setTitle("Constraint Violation");
        problem.setDetail(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ProblemDetail> handleMissingServletRequestPartException(MissingServletRequestPartException e) {
        log.warn("400  Missing request part: {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problem.setTitle("Missing Request Part");
        problem.setDetail(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("400  Invalid request body:  {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problem.setTitle("Bad Request Body");
        problem.setDetail("Request body is invalid");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("404  {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND.value());
        problem.setTitle("Not Found");
        problem.setDetail(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        log.warn("400  {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problem.setTitle("Bad Request");
        problem.setDetail("This endpoint supported other Content Type");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("405  {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.METHOD_NOT_ALLOWED.value());
        problem.setTitle("Method Not Allowed");
        problem.setDetail("Method '%s' is not supported for this URL".formatted(e.getMethod()));

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    @ExceptionHandler(InvalidCartItemAmountException.class)
    public ResponseEntity<ProblemDetail> handleInvalidCartItemAmountException(InvalidCartItemAmountException e) {
        log.warn("400  {}", e.getMessage());

        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problem.setTitle("Validation Failed");
        problem.setDetail(e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

}
