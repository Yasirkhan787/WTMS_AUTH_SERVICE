package com.yasirkhan.auth.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotFoundException extends RuntimeException{

    private String message;

    private HttpStatus status;

    public UserNotFoundException(String message){
        this.message = message;
        this.status = HttpStatus.NOT_FOUND;
    }
}
