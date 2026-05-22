package com.yasirkhan.auth.utils.validators;

import com.yasirkhan.auth.models.entity.Role;
import com.yasirkhan.auth.requests.UserRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class UserRequestValidator implements ConstraintValidator<ValidUserRequest, UserRequest> {

    private static final Pattern CNIC_PATTERN = Pattern.compile("^[0-9]{5}-[0-9]{7}-[0-9]$");

    @Override
    public boolean isValid(UserRequest request, ConstraintValidatorContext context) {
        if (request.getRole() == null) {
            return true;
        }

        boolean isValid = true;
        context.disableDefaultConstraintViolation(); // Clear the default class-level message

        // Validation for BOTH Supervisors and Drivers
        if (request.getRole() == Role.SUPERVISOR || request.getRole() == Role.DRIVER) {

            if (isBlank(request.getFatherName())) {
                isValid = logError(context, "fatherName", "Father's name is required for " + request.getRole().name());
            }

            if (isBlank(request.getCnic())) {
                isValid = logError(context, "cnic", "CNIC is required for " + request.getRole().name());
            } else if (!CNIC_PATTERN.matcher(request.getCnic()).matches()) {
                isValid = logError(context, "cnic", "CNIC must follow format: 12345-1234567-1");
            }

            if (isBlank(request.getAddress())) {
                isValid = logError(context, "address", "Address is required for " + request.getRole().name());
            }

            if (isBlank(request.getGender()) || !request.getGender().matches("^(?i)(MALE|FEMALE|OTHER)$")) {
                isValid = logError(context, "gender", "Gender must be MALE, FEMALE, or OTHER");
            }

            if (request.getDob() == null) {
                isValid = logError(context, "dob", "Date of Birth is required for " + request.getRole().name());
            }
        }

        // DRIVER-Only Validation
        if (request.getRole() == Role.DRIVER) {
            if (isBlank(request.getLicenseNo())) {
                isValid = logError(context, "licenseNo", "License Number is required for Drivers");
            }
            if (request.getLicenseExpiry() == null) {
                isValid = logError(context, "licenseExpiry", "License Expiry Date is required for Drivers");
            }
            // Age Validation (Drivers must usually be 18+)
            if (request.getDob() != null) {
                LocalDate eighteenYearsAgo = LocalDate.now().minusYears(18);
                if (request.getDob().isAfter(eighteenYearsAgo)) {
                    isValid = logError(context, "dob", "Drivers must be at least 18 years old to register.");
                }
            }
        }

        // Strict Profile Rejection (Ensure Supervisors don't try to submit Licenses)
        if (request.getRole() == Role.SUPERVISOR) {
            if (!isBlank(request.getLicenseNo()) || request.getLicenseExpiry() != null) {
                isValid = logError(context, "licenseNo", "Supervisors should not submit License details");
            }
        }

        // Ensure Admins don't submit ANY downstream profile data
        if (request.getRole() == Role.ADMIN) {
            if (!isBlank(request.getCnic()) || request.getDob() != null || !isBlank(request.getAddress())) {
                isValid = logError(context, "role", "Admins only require basic profile details (Name, Email, Phone). Do not submit CNIC, DOB, etc.");
            }
        }

        return isValid;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean logError(ConstraintValidatorContext context, String field, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
        return false;
    }
}