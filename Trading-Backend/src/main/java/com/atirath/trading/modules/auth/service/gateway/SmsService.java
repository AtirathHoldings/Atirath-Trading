package com.atirath.trading.modules.auth.service.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class SmsService {

    @Value("${app.sms.gateway-url:}")
    private String gatewayUrl;

    @Value("${app.sms.api-key:}")
    private String apiKey;

    @Value("${app.sms.sender-id:ATIRTH}")
    private String senderId;

    private final RestClient restClient;

    public SmsService() {
        this.restClient = RestClient.create();
    }

    public boolean sendSmsOtp(String mobileNumber, String otpCode, String purpose) {
        log.info("=================================================================");
        log.info("📱 [SMS GATEWAY] Sending SMS OTP to mobile: +91 {}", mobileNumber);
        log.info("👉 SMS OTP CODE: [{}] for {}", otpCode, purpose);
        log.info("=================================================================");

        // If an external SMS gateway URL is provided, dispatch via HTTP REST API
        if (gatewayUrl != null && !gatewayUrl.trim().isEmpty() && apiKey != null && !apiKey.trim().isEmpty()) {
            try {
                String response = restClient.post()
                        .uri(gatewayUrl)
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .body(String.format("{\"sender\":\"%s\",\"numbers\":\"%s\",\"message\":\"Your Atirath verification OTP is %s. Valid for 60 seconds.\"}",
                                senderId, mobileNumber, otpCode))
                        .retrieve()
                        .body(String.class);

                log.info("✅ SMS Gateway response: {}", response);
                return true;
            } catch (Exception e) {
                log.warn("⚠️ SMS gateway call failed ({}), fallback to logged OTP.", e.getMessage());
            }
        }

        return true;
    }
}
