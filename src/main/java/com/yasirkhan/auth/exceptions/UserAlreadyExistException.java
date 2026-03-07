package com.yasirkhan.auth.exceptions;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistException extends RuntimeException{

    private String message;

    private HttpStatus status;

    public UserAlreadyExistException(String message){
        this.message = message;
        this.status = HttpStatus.CONFLICT;
    }

}
