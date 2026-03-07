package com.yasirkhan.auth.exceptions;

import com.yasirkhan.auth.responses.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistException(UserAlreadyExistException ex) {

        ErrorResponse error =
                ErrorResponse.builder()
                        .message(ex.getMessage())
                        .status(ex.getStatus())
                        .timeStamp(LocalDateTime.now())
                        .build();

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex){

        ErrorResponse error =
                ErrorResponse.builder()
                        .message(ex.getMessage())
                        .status(ex.getStatus())
                        .timeStamp(LocalDateTime.now())
                        .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(TokenNotFoundException ex){

        ErrorResponse error =
                ErrorResponse.builder()
                        .message(ex.getMessage())
                        .status(ex.getStatus())
                        .timeStamp(LocalDateTime.now())
                        .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(TokenExpiredException ex){

        ErrorResponse error =
                ErrorResponse.builder()
                        .message(ex.getMessage())
                        .status(ex.getStatus())
                        .timeStamp(LocalDateTime.now())
                        .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }


}
