package br.com.incode.nexus_bagde.service;


import br.com.incode.nexus_bagde.entitys.Badge;
import br.com.incode.nexus_bagde.entitys.BadgeAssignment;
import br.com.incode.nexus_bagde.entitys.Student;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.frontend-url:https://badge-gerador.vercel.app}")
    private String frontendUrl;

    public void sendBadgeNotification(BadgeAssignment assignment) throws MessagingException {
        Student student = assignment.getStudent();
        Badge badge = assignment.getBadge();

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(student.getEmail());
        helper.setSubject("🏆 Parabéns! Você conquistou um novo badgee: " + badge.getName());

        String htmlContent = buildEmailContent(student, badge, assignment, frontendUrl);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String buildEmailContent(Student student, Badge badge, BadgeAssignment assignment, String systemUrl) {
        StringBuilder content = new StringBuilder();

        content.append("<!DOCTYPE html>");
        content.append("<html>");
        content.append("<head>");
        content.append("<meta charset='UTF-8'>");
        content.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        content.append("<style>");
        content.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #0C0C0D; background-color: #EDEDED; margin: 0; padding: 0; }");
        content.append(".container { max-width: 620px; margin: 20px auto; background-color: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 3px 10px rgba(0,0,0,0.1); }");
        content.append(".header { background: #0D1525; color: white; padding: 50px 30px; text-align: center; }");
        content.append(".header h1 { margin: 0; font-size: 30px; line-height: 1.2; font-weight: bold; }");
        content.append(".header p { margin: 12px 0 0; font-size: 16px; opacity: 0.85; }");
        content.append(".content { padding: 30px; background-color: #fff; }");
        content.append(".card { background: #EDEDED; padding: 22px; border-radius: 10px; margin-bottom: 20px; }");
        content.append(".card h2, .card h3, .card h4 { margin-top: 0; color: #0D1525; }");
        content.append(".card p { font-size: 15px; margin: 8px 0; }");
        content.append(".token-code { font-family: 'Courier New', monospace; background: #fff; padding: 14px; border-radius: 8px; font-size: 18px; font-weight: bold; letter-spacing: 2px; margin: 15px 0; border: 2px dashed #0D1525; text-align: center; color: #0C0C0D; }");
        content.append(".access-button { display: inline-block; padding: 15px 30px; background: #0D1525; color: white; font-size: 16px; font-weight: bold; text-decoration: none; border-radius: 6px; transition: background 0.3s, transform 0.2s; box-shadow: 0 3px 6px rgba(0,0,0,0.2); }");
        content.append(".access-button:hover { background: #0C0C0D; transform: translateY(-2px); }");
        content.append(".footer { background-color: #EDEDED; text-align: center; padding: 25px; font-size: 13px; color: #0C0C0D; border-top: 1px solid #d0d0d0; }");
        content.append(".steps { margin-top: 15px; }");
        content.append(".step { margin: 8px 0; padding: 10px; background: #fff; border-radius: 6px; border-left: 4px solid #0D1525; font-size: 14px; color: #0C0C0D; }");
        content.append("</style>");
        content.append("</head>");
        content.append("<body>");

        content.append("<div class='container'>");

        // Header
        content.append("<div class='header'>");
        content.append("<h1>🏆 Parabéns, ").append(student.getName()).append("!</h1>");
        content.append("<p>Você conquistou um novo badge de reconhecimento!</p>");
        content.append("</div>");

        // Content
        content.append("<div class='content'>");

        // Badge Info
        content.append("<div class='card'>");
        content.append("<h2>").append(badge.getName()).append("</h2>");
        if (badge.getDescription() != null && !badge.getDescription().isEmpty()) {
            content.append("<p><strong>📋 Descrição:</strong> ").append(badge.getDescription()).append("</p>");
        }
        if (badge.getCategory() != null && !badge.getCategory().isEmpty()) {
            content.append("<p><strong>🏷️ Categoria:</strong> ").append(badge.getCategory()).append("</p>");
        }
        if (assignment.getAchievementReason() != null && !assignment.getAchievementReason().isEmpty()) {
            content.append("<p><strong>⭐ Motivo da conquista:</strong> ").append(assignment.getAchievementReason()).append("</p>");
        }
        content.append("<p><strong>📅 Data da conquista:</strong> ").append(
                assignment.getAssignedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"))
        ).append("</p>");
        content.append("</div>");

        // Issuer Info
        if (badge.getIssuer() != null && !badge.getIssuer().isEmpty()) {
            content.append("<div class='card'>");
            content.append("<p><strong>🏢 Emitido por:</strong> ").append(badge.getIssuer()).append("</p>");
            content.append("<p style='font-size: 13px; margin-top: 6px; opacity: 0.8;'>Este badge é oficialmente reconhecido e emitido por esta instituição.</p>");
            content.append("</div>");
        }

        // Token Section
        content.append("<div class='card'>");
        content.append("<h3>🔑 Seu Código de Download</h3>");
        content.append("<p>Guarde este código para baixar seu badge:</p>");
        content.append("<div class='token-code'>").append(assignment.getDownloadToken()).append("</div>");
        content.append("<p><strong>Válido até:</strong> ").append(
                assignment.getTokenExpiresAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"))
        ).append("</p>");
        content.append("</div>");

        // Link direto para página de download
        content.append("<div class='card' style='text-align: center;'>");
        content.append("<h3>📥 Baixar seu Badge</h3>");
        content.append("<p>Clique no botão abaixo para acessar a página de download:</p>");
        content.append("<a href='").append(systemUrl).append("/download' class='access-button'>📥 Ir para Download</a>");
        content.append("</div>");

        // Instructions
        content.append("<div class='card'>");
        content.append("<h4>📋 Como Baixar seu Badge:</h4>");
        content.append("<div class='steps'>");
        content.append("<div class='step'><strong>1.</strong> Copie o código de download acima</div>");
        content.append("<div class='step'><strong>2.</strong> Clique em \"Ir para Download\"</div>");
        content.append("<div class='step'><strong>3.</strong> Cole o código no campo da página</div>");
        content.append("<div class='step'><strong>4.</strong> Clique em \"Baixar Badge\"</div>");
        content.append("</div>");
        content.append("<p><strong>💡 Dica:</strong> A página de download já está pronta para receber seu código.</p>");
        content.append("</div>");

        content.append("</div>"); // fecha content

        // Footer
        content.append("<div class='footer'>");
        if (badge.getIssuer() != null && !badge.getIssuer().isEmpty()) {
            content.append("<p><strong>").append(badge.getIssuer()).append("</strong></p>");
        }
        content.append("<p>Este email foi gerado automaticamente. Parabéns pela sua conquista!</p>");
        content.append("<p>📧 Email enviado em ").append(
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"))
        ).append("</p>");
        content.append("<p>🌐 Página de download: ").append(systemUrl).append("/download</p>");
        content.append("</div>");

        content.append("</div>");
        content.append("</body>");
        content.append("</html>");

        return content.toString();
    }


}
