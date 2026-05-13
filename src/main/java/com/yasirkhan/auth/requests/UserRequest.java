package com.yasirkhan.auth.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yasirkhan.auth.models.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    private String username;

    private String email;

    private String password;

    private Role role;

    private String name;

    private String fatherName;

    private String cnic;

    private String phoneNo;

    private String address;

    private String gender;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate dob;

    private String licenseNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate licenseExpiry;
}
