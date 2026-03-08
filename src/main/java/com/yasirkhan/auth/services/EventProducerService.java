package com.yasirkhan.auth.services;

import com.yasirkhan.auth.models.dto.DriverDto;
import com.yasirkhan.auth.models.dto.DriverStatusChangedEventDto;

public interface EventProducerService {

    void sendDriverCreationEvent(DriverDto dto);

    void sendDriverStatusEvent(DriverStatusChangedEventDto event);
}
