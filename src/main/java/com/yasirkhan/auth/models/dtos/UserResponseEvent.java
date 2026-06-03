package com.yasirkhan.auth.models.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yasirkhan.auth.models.enums.EventStatus;
import com.yasirkhan.auth.models.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponseEvent {

    private EventType type;
    private EventStatus eventTypeStatus;
    private UserEventDto userData;

}
