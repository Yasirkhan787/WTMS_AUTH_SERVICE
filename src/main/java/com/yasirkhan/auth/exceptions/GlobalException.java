package com.yasirkhan.auth.exceptions;

import com.yasirkhan.auth.responses.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistException(String message){

        ErrorResponse errorResponse
                = ErrorResponse.builder()
                .message(message)
                .status(HttpStatus.CONFLICT)
                .timeStamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(String message){

        ErrorResponse errorResponse
                = ErrorResponse.builder()
                .message(message)
                .status(HttpStatus.NOT_FOUND)
                .timeStamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


}
