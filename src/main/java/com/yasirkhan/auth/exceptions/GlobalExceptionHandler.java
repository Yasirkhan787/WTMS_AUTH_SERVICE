package com.yasirkhan.auth.exceptions;

import com.yasirkhan.auth.responses.ErrorResponse;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.MalformedJwtException;

import java.time.LocalDateTime;

@Slf4j // 👈 1. Added Lombok Logging
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Tracer tracer;

    public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    // Helper method to safely extract the Trace ID
    private String getTraceId() {
        return (tracer != null && tracer.currentSpan() != null)
                ? tracer.currentSpan().context().traceId()
                : "unknown";
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistException(UserAlreadyExistException ex, HttpServletRequest request){

        log.warn("Registration rejected: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .timeStamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .traceId(getTraceId())
                .build();

        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex, HttpServletRequest request){

        log.warn("User lookup failed: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .timeStamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .traceId(getTraceId())
                .build();

        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTokenNotFoundException(TokenNotFoundException ex, HttpServletRequest request){

        log.warn("Token missing from request: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .timeStamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .traceId(getTraceId())
                .build();

        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpiredException(TokenExpiredException ex, HttpServletRequest request){

        log.warn("Authentication failed due to expired token: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .timeStamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .traceId(getTraceId())
                .build();

        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(SessionExpiredException.class)
    public ResponseEntity<ErrorResponse> handleSessionExpiredException(SessionExpiredException ex, HttpServletRequest request){

        log.warn("Session expired: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .timeStamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .traceId(getTraceId())
                .build();

        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsExceptions(BadCredentialsException ex, HttpServletRequest request){

        log.warn("Invalid login attempt: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .timeStamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .traceId(getTraceId())
                .build();

        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseException(DatabaseException exception, HttpServletRequest request) {

        log.error("Database operation failed!", exception);

        ErrorResponse response = ErrorResponse.builder()
                .message(exception.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .timeStamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .traceId(getTraceId())
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({ExpiredJwtException.class, SignatureException.class, MalformedJwtException.class})
    public ResponseEntity<ErrorResponse> handleJwtExceptions(Exception ex, HttpServletRequest request){

        log.warn("JWT validation failed: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .timeStamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .traceId(getTraceId())
                .build();

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, HttpServletRequest request) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (ex instanceof org.springframework.web.bind.MethodArgumentNotValidException) {
            status = HttpStatus.BAD_REQUEST;
        } else if (ex instanceof org.springframework.dao.DataIntegrityViolationException) {
            status = HttpStatus.CONFLICT;
        } else if (ex instanceof org.springframework.web.HttpRequestMethodNotSupportedException) {
            status = HttpStatus.METHOD_NOT_ALLOWED;
        } else if (ex instanceof org.springframework.web.servlet.resource.NoResourceFoundException) {
            status = HttpStatus.NOT_FOUND;
        }

        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            log.error("Unhandled system exception occurred!", ex);
        } else {
            log.warn("Client error occurred: {} - {}", status.getReasonPhrase(), ex.getMessage());
        }

        ErrorResponse response = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(status.value())
                .error(status.getReasonPhrase())
                .timeStamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .traceId(getTraceId())
                .build();

        return new ResponseEntity<>(response, status);
    }
}