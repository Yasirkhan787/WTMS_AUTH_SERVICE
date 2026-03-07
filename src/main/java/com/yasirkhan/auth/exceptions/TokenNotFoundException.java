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
public class TokenNotFoundException extends RuntimeException{

    private String message;

    private HttpStatus status;

    public TokenNotFoundException(String message){
        this.message = message;
        this.status = HttpStatus.NOT_FOUND;
    }

}
