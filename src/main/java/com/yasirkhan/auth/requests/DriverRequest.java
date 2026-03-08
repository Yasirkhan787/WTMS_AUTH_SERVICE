package com.yasirkhan.auth.requests;

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
public class DriverRequest extends UserRequest{

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
