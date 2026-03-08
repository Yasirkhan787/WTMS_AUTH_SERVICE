package com.yasirkhan.auth.services.implementations;

import com.yasirkhan.auth.models.dto.DriverDto;
import com.yasirkhan.auth.models.dto.DriverStatusChangedEventDto;
import com.yasirkhan.auth.requests.DriverRequest;
import com.yasirkhan.auth.responses.DriverResponse;
import com.yasirkhan.auth.responses.UserResponse;
import com.yasirkhan.auth.services.DriverService;
import com.yasirkhan.auth.services.EventProducerService;
import com.yasirkhan.auth.services.UserService;
import com.yasirkhan.auth.utils.RequestConversion;
import com.yasirkhan.auth.utils.ResponseConversions;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DriverServiceImpl implements DriverService {

    private final UserService userService;

    private final EventProducerService eventProducerService;

    public DriverServiceImpl(UserService userService, EventProducerService eventProducerService) {
        this.userService = userService;
        this.eventProducerService = eventProducerService;
    }

    @Override
    public DriverResponse addDriver(DriverRequest driverRequest) {


        UserResponse userResponse =
                userService.addUser(RequestConversion.toUserRequest(driverRequest));

        // Send Event
        DriverDto driverDto =
                DriverDto
                        .builder()
                        .userID(userResponse.getId())
                        .name(driverRequest.getName())
                        .fatherName(driverRequest.getFatherName())
                        .email(userResponse.getEmail())
                        .cnic(driverRequest.getCnic())
                        .phoneNo(driverRequest.getPhoneNo())
                        .address(driverRequest.getAddress())
                        .gender(driverRequest.getGender())
                        .licenseNo(driverRequest.getLicenseNo())
                        .licenseExpiry(driverRequest.getLicenseExpiry())
                        .build();

        eventProducerService.sendDriverCreationEvent(driverDto);

        return
                ResponseConversions.toDriverResponse(userResponse, driverDto);
    }

    @Override
    public void toggleDriverStatus(UUID id, Boolean blockStatus) {

        String status = blockStatus ? "BLOCKED" : "ACTIVE";

        DriverStatusChangedEventDto event =
                DriverStatusChangedEventDto
                        .builder()
                        .userID(id)
                        .fleetStatus(status)
                        .updateType("STATUS_UPDATE")
                        .build();

        eventProducerService.sendDriverStatusEvent(event);

    }

}
