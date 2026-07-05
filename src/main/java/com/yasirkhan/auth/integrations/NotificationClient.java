package com.yasirkhan.auth.integrations;

import com.yasirkhan.auth.configs.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Trace context (traceId/spanId) is propagated to this downstream call automatically by
// Micrometer's Feign instrumentation, as long as micrometer-tracing is on the classpath -
// no manual header handling needed here.
@FeignClient(name = "notification-service", path = "/notification", configuration = FeignConfig.class)
public interface NotificationClient {

    @PutMapping("/delete")
    void deleteFCMToken(@RequestParam String userId);
}