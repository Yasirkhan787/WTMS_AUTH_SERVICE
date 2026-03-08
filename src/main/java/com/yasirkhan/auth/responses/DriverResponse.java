package com.yasirkhan.auth.responses;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse extends UserResponse{

    private String name;

    private String fatherName;

    private String email;

    private String cnic;

    private String phoneNo;

    private String address;

    private String gender;

    private String licenseNo;

    private LocalDate licenseExpiry;
}
