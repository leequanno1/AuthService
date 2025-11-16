package com.project.q_authent.services.notificaton_service;

import com.project.q_authent.models.sqls.PoolMailConfig;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sourceEmail;

    /**
     * Handle send email from sourceEmail to toEmail
     * @param to destination email {@link String}
     * @param subject email's subject {@link String}
     * @param content email content, content can be html document
     * @param isHtml if content is html document so this value is true, otherwise false
     * @throws MessagingException ex
     */
    @Async
    public void sendEmail(String to, String subject, String content, Boolean isHtml) throws MessagingException {

        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(sourceEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, isHtml);

        mailSender.send(mimeMessage);
    }

    @Async
    public void sendValidationCode(String to, PoolMailConfig mailConfig, String action, String code) throws MessagingException {
        String emailSubject = mailConfig.getSiteName()+ " verification email";
        String emailContent = buildEmailContent(mailConfig, action, code);
        sendEmail(to, emailSubject, emailContent, true);
    }

    @Async
    public void sendValidationCode(String to, String action, String code) throws MessagingException {
        String emailSubject = "Authify verification email";
        PoolMailConfig mailConfig = PoolMailConfig.builder()
                .siteName("Authify")
                .siteUrl("https://authify.com")
                .build();
        String emailContent = buildEmailContent(mailConfig, action, code);
        sendEmail(to, emailSubject, emailContent, true);
    }

    public String buildEmailContent(
            PoolMailConfig mailConfig,
            String action,
            String code
    ) {
        // Default values
        String siteName = (mailConfig.getSiteName() == null || mailConfig.getSiteName().isEmpty())
                ? "Your Website"
                : mailConfig.getSiteName();

        String siteUrl = (mailConfig.getSiteUrl() == null || mailConfig.getSiteUrl().isEmpty())
                ? ""
                : mailConfig.getSiteUrl();

        String supportEmail = (mailConfig.getSupportEmail() == null || mailConfig.getSupportEmail().isEmpty())
                ? ""
                : mailConfig.getSupportEmail();

        // Conditional sections
        String siteLinkHeader = siteUrl.isEmpty()
                ? siteName
                : String.format("<a href=\"%s\" style=\"color:#fff;text-decoration:none;font-weight:700;font-size:20px;\">%s</a>", siteUrl, siteName);

        String ctaButton = siteUrl.isEmpty()
                ? ""
                : String.format("<p style=\"margin:18px 0 0 0;\"><a href=\"%s\" style=\"display:inline-block;padding:10px 16px;border-radius:6px;text-decoration:none;font-weight:600;border:1px solid #06b6d4;background:#06b6d4;color:#fff;\">Back to %s</a></p>", siteUrl, siteName);

        String footerSupport = supportEmail.isEmpty()
                ? ""
                : String.format("<p style=\"margin:0 0 8px 0;\">Need help? Contact us at <a href=\"mailto:%s\" style=\"color:#0ea5a4;text-decoration:none;\">%s</a></p>", supportEmail, supportEmail);

        // Build HTML content
        return String.format("""
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>Verification Code — %s</title>
</head>
<body style="margin:0;padding:0;background-color:#f3f4f6;font-family:Arial,Helvetica,sans-serif;color:#111;">
  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f3f4f6;padding:20px 0;">
    <tr>
      <td align="center">
        <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="max-width:600px;width:100%%;background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 6px rgba(0,0,0,0.08);">
          
          <tr>
            <td style="padding:20px 24px;background:linear-gradient(90deg,#0ea5a4,#06b6d4);color:#fff;">
              <table role="presentation" width="100%%">
                <tr>
                  <td style="vertical-align:middle;">
                    %s
                  </td>
                  <td style="text-align:right;vertical-align:middle;font-size:13px;opacity:0.95;">
                    Verification Code
                  </td>
                </tr>
              </table>
            </td>
          </tr>

          <tr>
            <td style="padding:28px 24px;">
              <h1 style="margin:0 0 12px 0;font-size:20px;color:#0f172a;">Verification for %s</h1>
              <p style="margin:0 0 18px 0;line-height:1.45;color:#334155;">
                Hello, <br>
                You’ve requested a verification code for the action <strong>“%s”</strong> on <strong>%s</strong>.<br>
                Please use the code below to complete your request.
              </p>

              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="margin:18px 0;">
                <tr>
                  <td align="center">
                    <div style="display:inline-block;background:#f8fafc;border:1px dashed #cbd5e1;padding:18px 28px;border-radius:8px;">
                      <p style="margin:0;font-size:28px;letter-spacing:4px;font-weight:700;color:#0f172a;">
                        %s
                      </p>
                      <p style="margin:6px 0 0 0;font-size:13px;color:#475569;">Expires in: <strong>15 minutes</strong></p>
                    </div>
                  </td>
                </tr>
              </table>

              <p style="margin:18px 0 0 0;line-height:1.45;color:#475569;font-size:14px;">
                Please note:
              </p>
              <ul style="margin:8px 0 0 20px;color:#475569;line-height:1.5;">
                <li>The code can be used only once and expires in 15 minutes.</li>
                <li>Do not share this code with anyone.</li>
                <li>If you didn’t request this action, please ignore this email or contact our support team.</li>
              </ul>

              %s
            </td>
          </tr>

          <tr>
            <td style="padding:18px 24px;background:#f8fafc;color:#64748b;font-size:13px;">
              %s
              <p style="margin:0;">© <span style="font-weight:600;">%s</span>%s</p>
            </td>
          </tr>

          <tr>
            <td style="padding:12px 20px;font-size:11px;color:#9ca3af;text-align:center;">
              You’re receiving this email because of your account at %s.<br>
              If you did not initiate the “%s” request, please ignore this email.
            </td>
          </tr>

        </table>
      </td>
    </tr>
  </table>
</body>
</html>
""",
                siteName,
                siteLinkHeader,
                action,
                action,
                siteName,
                code,
                ctaButton,
                footerSupport,
                siteName,
                siteUrl.isEmpty() ? "" : " — <a href=\"" + siteUrl + "\" style=\"color:#0ea5a4;text-decoration:none;\">" + siteUrl + "</a>",
                siteName,
                action
        );
    }
}