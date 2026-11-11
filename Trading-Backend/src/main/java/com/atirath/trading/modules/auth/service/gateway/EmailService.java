package com.atirath.trading.modules.auth.service.gateway;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Year;

@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@atirathgroup.com}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

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
                        + "\n\nThis code will expire in 60 seconds. Do not share this OTP with anyone.\n\nRegards,\nAtirath Security Team");
                mailSender.send(message);
                log.info("✅ Email successfully dispatched via SMTP gateway to {}", toEmail);
                return true;
            } catch (Exception e) {
                log.warn("⚠️ SMTP server not reachable ({}), OTP was logged to console.", e.getMessage());
            }
        }
        return true;
    }

    public boolean sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetLink = frontendUrl.replaceAll("/+$", "") + "/reset-password?token=" + resetToken;

        log.info("=================================================================");
        log.info("📧 [PASSWORD RESET] Preparing to send reset email to: {}", toEmail);
        log.info("🔗 Reset Link: {}", resetLink);
        log.info("🔑 Reset Token: {}", resetToken);
        log.info("=================================================================");

        if (mailSender != null) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(fromEmail);
                helper.setTo(toEmail);
                helper.setSubject("Atirath Trading Platform - Password Reset Request");

                String html = "<div style=\"font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: 0 auto; padding: 28px; border: 1px solid #e2e8f0; border-radius: 12px; background-color: #ffffff;\">"
                        + "<div style=\"text-align: center; margin-bottom: 24px;\">"
                        + "<h2 style=\"color: #0b2265; margin: 0; font-size: 24px; font-weight: 800; letter-spacing: 0.5px;\">ATIRATH GROUP</h2>"
                        + "<p style=\"color: #64748b; font-size: 13px; margin: 4px 0 0 0;\">Invest. Grow. Together.</p>"
                        + "</div>"
                        + "<div style=\"border-bottom: 2px solid #f1f5f9; margin-bottom: 20px;\"></div>"
                        + "<h3 style=\"color: #1e293b; font-size: 18px; margin-top: 0;\">Password Reset Request</h3>"
                        + "<p style=\"color: #475569; font-size: 14px; line-height: 1.6;\">We received a request to reset your password for your Atirath Trading account. Click the button below to set a new password:</p>"
                        + "<div style=\"text-align: center; margin: 28px 0;\">"
                        + "<a href=\"" + resetLink + "\" style=\"background-color: #0b2265; color: #ffffff; padding: 13px 32px; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 15px; display: inline-block;\">Reset Password</a>"
                        + "</div>"
                        + "<p style=\"color: #64748b; font-size: 13px; line-height: 1.5;\">Or copy and paste this link into your web browser:<br/><a href=\"" + resetLink + "\" style=\"color: #2563eb; word-break: break-all;\">" + resetLink + "</a></p>"
                        + "<div style=\"background-color: #f8fafc; padding: 14px; border-radius: 8px; margin: 20px 0; border: 1px solid #e2e8f0;\">"
                        + "<span style=\"color: #64748b; font-size: 12px; font-weight: 600;\">Direct Reset Token:</span><br/>"
                        + "<code style=\"color: #0b2265; font-size: 14px; font-weight: bold; word-break: break-all;\">" + resetToken + "</code>"
                        + "</div>"
                        + "<p style=\"color: #94a3b8; font-size: 12px; line-height: 1.5;\">⚠️ This reset link and token will expire in <strong>15 minutes</strong>. If you did not request this password reset, please ignore this email or contact support.</p>"
                        + "<hr style=\"border: none; border-top: 1px solid #f1f5f9; margin: 24px 0;\" />"
                        + "<p style=\"color: #94a3b8; font-size: 11px; text-align: center; margin: 0;\">© " + Year.now().getValue() + " Atirath Trading Platform. All rights reserved.</p>"
                        + "</div>";

                helper.setText(html, true);
                mailSender.send(mimeMessage);
                log.info("✅ Password reset email successfully dispatched via SMTP to {}", toEmail);
                return true;
            } catch (Exception e) {
                log.warn("⚠️ SMTP server dispatch failed ({}). Ensure SMTP credentials are configured.", e.getMessage());
            }
        } else {
            log.warn("⚠️ JavaMailSender is not initialized. Please verify spring.mail configuration.");
        }
        return true;
    }
}
