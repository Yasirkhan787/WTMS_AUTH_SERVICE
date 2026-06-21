package com.yasirkhan.auth.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yasirkhan.auth.models.enums.Role;
import com.yasirkhan.auth.utils.validators.ValidUserRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ValidUserRequest
public class UserRequest {

    @NotBlank(message = "Username is required")
    /// @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email format (e.g., user@example.com)")
    private String email;

    @NotBlank(message = "Password is required")
    /// @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name cannot exceed 50 characters")
    private String name;

    @NotBlank(message = "Phone Number is required")
    @Pattern(regexp = "^[0-9]{4}-[0-9]{7}$", message = "Phone Number must follow format: 0333-1234567")
    private String phoneNo;

    private String fatherName;

    private String cnic;

    private String address;

    private String gender;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate dob;

    private String licenseNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate licenseExpiry;

    private String tehsilId;

    private String yardId;
}