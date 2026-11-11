package com.atirath.trading.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Otp otp = new Otp();

    @Data
    public static class Jwt {
        private String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        private long accessTokenExpirationMs = 3600000; // 1 hour
        private long refreshTokenExpirationMs = 604800000; // 7 days
        private long mobileRefreshTokenExpirationMs = 2592000000L; // 30 days
    }

    @Data
    public static class Otp {
        private int expirationSeconds = 60;
        private int mobileExpirationSeconds = 60;
        private int length = 6;
        private int maxResendAttempts = 3;
        private boolean devModeEnabled = true;
        private String defaultTestOtp = "123456";
    }
}
