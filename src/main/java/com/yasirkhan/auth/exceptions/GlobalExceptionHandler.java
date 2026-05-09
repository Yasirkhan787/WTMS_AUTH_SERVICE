package com.yasirkhan.auth.exceptions;

import com.yasirkhan.auth.responses.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.MalformedJwtException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistException(UserAlreadyExistException ex, HttpServletRequest request){

        ErrorResponse error = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .timeStamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        // Now uses ex.getStatus() instead of hardcoded HttpStatus
        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex, HttpServletRequest request){

        ErrorResponse error = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .timeStamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTokenNotFoundException(TokenNotFoundException ex, HttpServletRequest request){

        ErrorResponse error = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .timeStamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpiredException(TokenExpiredException ex, HttpServletRequest request){

        ErrorResponse error = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .timeStamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(SessionExpiredException.class)
    public ResponseEntity<ErrorResponse> handleSessionExpiredException(SessionExpiredException ex, HttpServletRequest request){

        ErrorResponse error = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .timeStamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        // This ensures Requestly shows 400 Bad Request, not 404
        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsExceptions(BadCredentialsException ex, HttpServletRequest request){

        ErrorResponse error = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .timeStamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();


        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler({ExpiredJwtException.class, SignatureException.class, MalformedJwtException.class})
    public ResponseEntity<ErrorResponse> handleJwtExceptions(Exception ex, HttpServletRequest request){

        ErrorResponse error = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .timeStamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, HttpServletRequest request) {

        // Default to 500
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // Dynamically switch status based on the exception type
        if (ex instanceof org.springframework.web.bind.MethodArgumentNotValidException) {
            status = HttpStatus.BAD_REQUEST; // Validation failed
        } else if (ex instanceof org.springframework.dao.DataIntegrityViolationException) {
            status = HttpStatus.CONFLICT;    // Database constraint
        } else if (ex instanceof org.springframework.web.HttpRequestMethodNotSupportedException) {
            status = HttpStatus.METHOD_NOT_ALLOWED;
        } else if (ex instanceof org.springframework.web.servlet.resource.NoResourceFoundException) {
            // This block so bad URLs return a proper 404 Not Found instead of 500
            status = HttpStatus.NOT_FOUND;
        }

        ErrorResponse response = ErrorResponse.builder()
                .message(ex.getMessage())
                .status(status.value())
                .error(status.getReasonPhrase())
                .timeStamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, status);
    }
}