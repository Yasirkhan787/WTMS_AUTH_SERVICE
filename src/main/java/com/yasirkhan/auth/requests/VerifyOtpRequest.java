package com.yasirkhan.auth.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    @NotBlank(message = "OTP code is required")
    private String otp;
}