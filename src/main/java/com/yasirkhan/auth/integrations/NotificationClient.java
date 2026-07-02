package com.yasirkhan.auth.integrations;

import com.yasirkhan.auth.configs.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "notification-service", path = "/notification", configuration = FeignConfig.class)
public interface NotificationClient {

    @PutMapping("/delete")
    void deleteFCMToken(@RequestParam String userId);
}