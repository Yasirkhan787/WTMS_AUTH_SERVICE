package com.yasirkhan.auth.exceptions;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends RuntimeException{

    private String message;

    private HttpStatus status;

    public UserNotFoundException(String message){
        this.message = message;
        this.status = HttpStatus.CONFLICT;
    }
}
