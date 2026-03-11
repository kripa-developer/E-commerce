package com.novacart.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8"/>
                  <style>
                    body { margin:0; padding:0; background:#070b1a; font-family:'Segoe UI',sans-serif; }
                    .wrapper { max-width:560px; margin:40px auto; background:#0d1228; border-radius:16px;
                               border:1px solid rgba(109,141,255,0.15); overflow:hidden; }
                    .header { background:linear-gradient(135deg,#6d8dff,#a78bfa);
                              padding:32px 40px; text-align:center; }
                    .logo { font-size:24px; font-weight:800; color:#fff; letter-spacing:-0.5px; }
                    .body { padding:36px 40px; }
                    h2 { color:#f0f4ff; font-size:20px; margin:0 0 12px; }
                    p { color:#8896c0; font-size:14px; line-height:1.7; margin:0 0 20px; }
                    .btn { display:inline-block; background:linear-gradient(135deg,#6d8dff,#a78bfa);
                           color:#fff !important; text-decoration:none; padding:14px 32px;
                           border-radius:10px; font-weight:700; font-size:15px; }
                    .note { font-size:12px; color:#4a5568; margin-top:24px; }
                    .footer { padding:20px 40px; text-align:center; border-top:1px solid rgba(109,141,255,0.1); }
                    .footer p { color:#4a5568; font-size:12px; margin:0; }
                  </style>
                </head>
                <body>
                  <div class="wrapper">
                    <div class="header">
                      <div class="logo">🛒 NovaCart</div>
                    </div>
                    <div class="body">
                      <h2>Reset your password</h2>
                      <p>We received a request to reset the password for your NovaCart account
                         associated with <strong style="color:#f0f4ff;">%s</strong>.</p>
                      <p>Click the button below to set a new password. This link expires in
                         <strong style="color:#f0f4ff;">15 minutes</strong>.</p>
                      <a href="%s" class="btn">Reset Password</a>
                      <p class="note">If you didn't request a password reset, you can safely ignore
                         this email. Your password will not be changed.</p>
                    </div>
                    <div class="footer">
                      <p>© 2026 NovaCart. All rights reserved.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(toEmail, resetLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Reset your NovaCart password");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}