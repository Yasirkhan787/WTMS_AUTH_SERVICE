package com.yasirkhan.auth.models.dto;

import com.yasirkhan.auth.models.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEventDto {

    private UUID userId;

    private String username;

    private String email;

    private Role role;

    private String name;

    private String fatherName;

    private String cnic;

    private String phoneNo;

    private String address;

    private String gender;

    private Integer age;

    private String licenseNo;

    private LocalDate licenseExpiry;

    private String status;
}
