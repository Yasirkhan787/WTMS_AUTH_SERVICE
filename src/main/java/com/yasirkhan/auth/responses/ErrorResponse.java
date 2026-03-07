package com.yasirkhan.auth.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private String message;

    private HttpStatus status;

    private LocalDateTime timeStamp;

}
