package br.com.incode.nexus_bagde.controller;

import br.com.incode.nexus_bagde.dto.BadgeAssignmentDTO;
import br.com.incode.nexus_bagde.entitys.Badge;
import br.com.incode.nexus_bagde.repository.BadgeRepository;
import br.com.incode.nexus_bagde.service.BadgeAssignmentService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;


@RestController
@RequestMapping("/api/public/assertions")
public class PublicBadgeController {

    private final BadgeAssignmentService badgeAssignmentService;

    private final BadgeRepository badgeRepository;


//    private final String badgesDir = "static/uploads/badges/";
//    private final String issuersDir = "static/uploads/issuers/";

    public PublicBadgeController(BadgeAssignmentService badgeAssignmentService, BadgeRepository badgeRepository) {
        this.badgeAssignmentService = badgeAssignmentService;
        this.badgeRepository = badgeRepository;

    }

    @GetMapping("/{assignmentId}/open-badge")
    public ResponseEntity<?> getOpenBadge(@PathVariable Long assignmentId) {
        try {
            BadgeAssignmentDTO dto = badgeAssignmentService.getAssignmentById(assignmentId);
            return ResponseEntity.ok(dto.getOpenBadgeJson());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/badges/{filename}/image")
    public ResponseEntity<Resource> getBadgeImage(@PathVariable String filename) {
        try {
            Path path = Paths.get("src/main/resources/static/badges/").resolve(filename);
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists()) {
                throw new RuntimeException("Imagem não encontrada");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new RuntimeException("Não foi possível ler a imagem", e);
        }
    }

    @GetMapping("/issuers2/{id}/image")
    public ResponseEntity<Resource> getIssuerImage(@PathVariable Long id) {
        try {
            Badge badge = badgeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Badge não encontrado"));

            // Pega o filename salvo no banco
            String filename = badge.getIssuerImagePath();
            if (filename == null) {
                throw new RuntimeException("Badge não possui imagem");
            }

            // Monta o path completo
            Path path = Paths.get("src/main/resources/static/issuers/").resolve(filename);
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists()) {
                throw new RuntimeException("Imagem não encontrada");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new RuntimeException("Não foi possível ler a imagem", e);
        }
    }

    @GetMapping("/badges2/{id}/image")
    public ResponseEntity<Resource> getBadgeImageById(@PathVariable Long id) {
        try {
            Badge badge = badgeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Badge não encontrado"));

            // Pega o filename salvo no banco
            String filename = badge.getImagePath();
            if (filename == null) {
                throw new RuntimeException("Badge não possui imagem");
            }

            // Monta o path completo
            Path path = Paths.get("src/main/resources/static/badges/").resolve(filename);
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists()) {
                throw new RuntimeException("Imagem não encontrada");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new RuntimeException("Não foi possível ler a imagem", e);
        }
    }


}
