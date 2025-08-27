package br.com.incode.nexus_bagde.controller;

import br.com.incode.nexus_bagde.dto.BadgeDownloadInfo;
import br.com.incode.nexus_bagde.dto.TokenDownloadRequest;
import br.com.incode.nexus_bagde.dto.TokenValidationRequest;
import br.com.incode.nexus_bagde.dto.TokenValidationResponse;
import br.com.incode.nexus_bagde.entitys.BadgeAssignment;
import br.com.incode.nexus_bagde.service.BadgeAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/badges")

public class BadgeDownloadController {

    @Autowired
    private BadgeAssignmentService badgeAssignmentService;

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody TokenValidationRequest request) {
        try {
            BadgeAssignment assignment = badgeAssignmentService.findByDownloadToken(request.getToken())
                    .orElse(null);

            if (assignment == null) {
                return ResponseEntity.badRequest()
                        .body(new TokenValidationResponse(false, "Código inválido", null));
            }

            if (!assignment.isTokenValid()) {
                return ResponseEntity.badRequest()
                        .body(new TokenValidationResponse(false, "Código expirado", null));
            }

            // Verificar se o badge tem imagem
            String imagePath = assignment.getBadge().getImagePath();
            if (imagePath == null || imagePath.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new TokenValidationResponse(false, "Badge não possui imagem", null));
            }

            // Logs para verificar os dados
            System.out.println("🏆 Badge name: " + assignment.getBadge().getName());
            System.out.println("🏆 Badge imagePath: " + assignment.getBadge().getImagePath());
            System.out.println("🏢 Badge issuer: " + assignment.getBadge().getIssuer());
            System.out.println("🏢 Badge issuerImagePath: " + assignment.getBadge().getIssuerImagePath());

            // Retornar informações do badge
            BadgeDownloadInfo badgeInfo = new BadgeDownloadInfo(
                    assignment.getBadge().getName(),
                    assignment.getBadge().getDescription(),
                    assignment.getBadge().getCategory(),
                    assignment.getBadge().getImagePath(), // ADICIONADO: imagePath do badge
                    assignment.getBadge().getIssuer(),
                    assignment.getBadge().getIssuerImagePath(),
                    assignment.getStudent().getName(),
                    assignment.getAchievementReason(),
                    assignment.getAssignedAt(),
                    assignment.getDownloadCount(),
                    assignment.getTokenExpiresAt(),
                    assignment.getId()
            );

            System.out.println("📤 Retornando BadgeInfo com:");
            System.out.println("   - badgeImagePath: " + badgeInfo.getBadgeImagePath());
            System.out.println("   - issuerImagePath: " + badgeInfo.getIssuerImagePath());

            return ResponseEntity.ok(new TokenValidationResponse(true, "Código válido", badgeInfo));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new TokenValidationResponse(false, "Erro interno do servidor", null));
        }
    }

    @PostMapping("/download-by-token")
    public ResponseEntity<?> downloadByToken(@RequestBody TokenDownloadRequest request) {
        try {
            // ⚡ Alterado para buscar pelo nome correto da coluna
            BadgeAssignment assignment = badgeAssignmentService
                    .findByDownloadToken(request.getToken())
                    .orElse(null);

            if (assignment == null) {
                return ResponseEntity.badRequest().body("Código inválido");
            }

            if (!assignment.isTokenValid()) {
                return ResponseEntity.badRequest().body("Código expirado");
            }

            String imagePath = assignment.getBadge().getImagePath();
            if (imagePath == null || imagePath.isEmpty()) {
                return ResponseEntity.badRequest().body("Badge não possui imagem");
            }

            // Monta caminho absoluto
            Path filePath = Paths.get("uploads/badges").resolve(imagePath).toAbsolutePath();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.badRequest().body("Arquivo de imagem não encontrado");
            }

            // Incrementa contador
            assignment.incrementDownloadCount();
            badgeAssignmentService.updateAssignment(assignment);

            // Detecta content type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";

            // Cria nome do arquivo
            String studentName = assignment.getStudent().getName()
                    .replaceAll("[^a-zA-Z0-9\\s]", "")
                    .replaceAll("\\s+", "_");
            String badgeName = assignment.getBadge().getName()
                    .replaceAll("[^a-zA-Z0-9\\s]", "")
                    .replaceAll("\\s+", "_");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            String extension = "";
            int lastDot = imagePath.lastIndexOf('.');
            if (lastDot > 0) extension = imagePath.substring(lastDot);

            String filename = String.format("badge_%s_%s_%s%s",
                    badgeName, studentName, timestamp, extension);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erro ao processar download: " + e.getMessage());
        }
    }
}
