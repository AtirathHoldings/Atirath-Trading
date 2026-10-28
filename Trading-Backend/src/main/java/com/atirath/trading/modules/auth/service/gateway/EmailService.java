package com.atirath.trading.modules.auth.service.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@atirathgroup.com}")
    private String fromEmail;

    public boolean sendEmailOtp(String toEmail, String otpCode, String purpose) {
        log.info("=================================================================");
        log.info("📧 [EMAIL GATEWAY] Sending OTP to: {}", toEmail);
        log.info("👉 OTP CODE: [{}] for {}", otpCode, purpose);
        log.info("=================================================================");

        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(toEmail);
                message.setSubject("Atirath Trading Platform - Your Verification OTP: " + otpCode);
                message.setText("Dear User,\n\nYour verification OTP for " + purpose + " is: " + otpCode
                        + "\n\nThis code will expire in 90 seconds. Do not share this OTP with anyone.\n\nRegards,\nAtirath Security Team");
                mailSender.send(message);
                log.info("✅ Email successfully dispatched via SMTP gateway to {}", toEmail);
                return true;
            } catch (Exception e) {
                log.warn("⚠️ SMTP server not reachable ({}), OTP was logged to console.", e.getMessage());
            }
        }
        return true;
    }
}
