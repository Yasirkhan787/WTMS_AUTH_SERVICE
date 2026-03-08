package com.yasirkhan.auth.services;

import com.yasirkhan.auth.requests.DriverRequest;
import com.yasirkhan.auth.responses.DriverResponse;

import java.util.UUID;

public interface DriverService {

    DriverResponse addDriver(DriverRequest driverRequest);

    void toggleDriverStatus(UUID id, Boolean blockStatus);
}
