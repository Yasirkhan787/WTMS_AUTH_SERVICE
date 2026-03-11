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
                        .status(ex.getStatus())
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
                        .status(ex.getStatus())
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
                        .status(ex.getStatus())
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
                        .status(ex.getStatus())
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
                        .status(ex.getStatus())
                        .timeStamp(LocalDateTime.now())
                        .path(request.getRequestURI())
                        .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}
