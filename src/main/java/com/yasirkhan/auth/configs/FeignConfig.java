package com.yasirkhan.auth.configs;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
public class FeignConfig {

    private static final String SERVICE_NAME = "AUTH_SERVICE";

    // CHANGED: removed the hardcoded fallback ("my-super-secret-service-key"). A default
    // secret baked into source control defeats the point of signing requests - if the
    // property is missing, startup should fail loudly instead of silently signing with
    // a publicly known value.
    @Value("${app.security.internal-secret}")
    private String internalSecret;

    @Bean
    public RequestInterceptor requestInterceptor() {
        // CHANGED: replaced the anonymous inner class with a lambda - RequestInterceptor
        // is a single-method functional interface.
        return template -> {
            try {
                long timestamp = System.currentTimeMillis();
                String signature = sign(SERVICE_NAME + timestamp + internalSecret);

                template.header("X-Service-Name", SERVICE_NAME);
                template.header("X-Timestamp", String.valueOf(timestamp));
                template.header("X-Signature", signature);
            } catch (NoSuchAlgorithmException e) {
                // CHANGED: previously this only logged and let the request proceed unsigned.
                // A downstream service trusting the signature header should not receive an
                // unsigned request, so we fail the call instead.
                log.error("Failed to sign internal request", e);
                throw new IllegalStateException("Unable to sign internal service request", e);
            }
        };
    }

    private String sign(String rawData) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(rawData.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}