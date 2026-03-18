package com.yasirkhan.auth.exceptions;

import com.yasirkhan.auth.responses.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistException(UserAlreadyExistException ex, HttpServletRequest request){

        ErrorResponse error =
                ErrorResponse.builder()
                        .message(ex.getMessage())
                        .status(ex.getStatus().value())
                        .error(ex.getStatus().getReasonPhrase())
                        .timeStamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build();

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex, HttpServletRequest request){

        ErrorResponse error =
                ErrorResponse.builder()
                        .message(ex.getMessage())
                        .status(ex.getStatus().value())
                        .error(ex.getStatus().getReasonPhrase())
                        .timeStamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(TokenNotFoundException ex, HttpServletRequest request){

        ErrorResponse error =
                ErrorResponse.builder()
                        .message(ex.getMessage())
                        .status(ex.getStatus().value())
                        .error(ex.getStatus().getReasonPhrase())
                        .timeStamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(TokenExpiredException ex, HttpServletRequest request){

        ErrorResponse error =
                ErrorResponse.builder()
                        .message(ex.getMessage())
                        .status(ex.getStatus().value())
                        .error(ex.getStatus().getReasonPhrase())
                        .timeStamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsExceptions(BadCredentialsException ex, HttpServletRequest request){

        ErrorResponse error =
                ErrorResponse.builder()
                        .message(ex.getMessage())
                        .status(ex.getStatus().value())
                        .error(ex.getStatus().getReasonPhrase())
                        .timeStamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, HttpServletRequest request) {

        // Default to 500
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // Dynamically switch status based on the exception type
        if (ex instanceof org.springframework.web.bind.MethodArgumentNotValidException) {
            status = HttpStatus.BAD_REQUEST; // Validation failed
        } else if (ex instanceof org.springframework.dao.DataIntegrityViolationException) {
            status = HttpStatus.CONFLICT;    // Database constraint (like your 'status' check)
        } else if (ex instanceof org.springframework.web.HttpRequestMethodNotSupportedException) {
            status = HttpStatus.METHOD_NOT_ALLOWED;
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
