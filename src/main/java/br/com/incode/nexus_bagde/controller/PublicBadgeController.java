package br.com.incode.nexus_bagde.controller;

import br.com.incode.nexus_bagde.dto.AssertionDTO;
import br.com.incode.nexus_bagde.entitys.Badge;
import br.com.incode.nexus_bagde.repository.BadgeRepository;
import br.com.incode.nexus_bagde.service.BadgeAssignmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@RestController
@RequestMapping("/api/public/assertions")
public class PublicBadgeController {

    private final BadgeAssignmentService badgeAssignmentService;

    private final BadgeRepository badgeRepository;


    @Value("${uploads.badges}")
    private String badgesDir;

    @Value("${uploads.issuers}")
    private String issuersDir;

    public PublicBadgeController(BadgeAssignmentService badgeAssignmentService, BadgeRepository badgeRepository) {
        this.badgeAssignmentService = badgeAssignmentService;
        this.badgeRepository = badgeRepository;

    }

    @GetMapping("/{assignmentId}/open-badge")
    public ResponseEntity<?> getOpenBadge(@PathVariable Long assignmentId) {
        try {
            AssertionDTO dto = badgeAssignmentService.getAssignmentById(assignmentId);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    //em uso
    @GetMapping("/badges/{id}/image")
    public  ResponseEntity<Resource> getBadgeImageById2(@PathVariable Long id) {
        try {
            Badge badge = badgeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Badge não encontrado"));

            String filename = badge.getImagePath();
            if (filename == null || filename.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Path path = Paths.get(badgesDir).resolve(filename).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                // Logue o caminho absoluto para depurar no server
                System.err.println("Imagem não encontrada em: " + path.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new RuntimeException("Não foi possível ler a imagem", e);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao identificar content-type da imagem", e);
        }
    }




    @GetMapping("/issuers/{id}/image")
    public  ResponseEntity<Resource> getIssuerImageById2(@PathVariable Long id) {
        try {
            Badge badge = badgeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Badge não encontrado"));

            String filename = badge.getIssuerImagePath();
            if (filename == null || filename.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Path path = Paths.get(issuersDir).resolve(filename).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                // Logue o caminho absoluto para depurar no server
                System.err.println("Imagem não encontrada em: " + path.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new RuntimeException("Não foi possível ler a imagem", e);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao identificar content-type da imagem", e);
        }
    }



    //em uso
//    @GetMapping("/issuers/{id}/image")
//    public ResponseEntity<Resource> getIssuerImageById(@PathVariable Long id) {
//        try {
//            Badge badge = badgeRepository.findById(id)
//                    .orElseThrow(() -> new RuntimeException("Badge não encontrado"));
//
//            String filename = badge.getIssuerImagePath();
//            if (filename == null || filename.isEmpty()) {
//                throw new RuntimeException("Emissor não possui imagem");
//            }
//
//            Path path = Paths.get(issuersDir).resolve(filename);
//            Resource resource = new UrlResource(path.toUri());
//
//            if (!resource.exists()) {
//                throw new RuntimeException("Imagem não encontrada");
//            }
//
//            return ResponseEntity.ok()
//                    .contentType(MediaType.IMAGE_PNG)
//                    .body(resource);
//
//        } catch (MalformedURLException e) {
//            throw new RuntimeException("Não foi possível ler a imagem", e);
//        }
//    }

//    @GetMapping("/issuers/{id}/image")
//    public ResponseEntity<Resource> getIssuerImage(@PathVariable Long id) {
//        try {
//            Badge badge = badgeRepository.findById(id)
//                    .orElseThrow(() -> new RuntimeException("Badge não encontrado"));
//
//            // Pega o filename salvo no banco
//            String filename = badge.getIssuerImagePath();
//            if (filename == null) {
//                throw new RuntimeException("Badge não possui imagem");
//            }
//
//            // Monta o path completo
//            Path path = Paths.get("src/main/resources/static/issuers/").resolve(filename);
//            Resource resource = new UrlResource(path.toUri());
//
//            if (!resource.exists()) {
//                throw new RuntimeException("Imagem não encontrada");
//            }
//
//            return ResponseEntity.ok()
//                    .contentType(MediaType.IMAGE_PNG)
//                    .body(resource);
//
//        } catch (MalformedURLException e) {
//            throw new RuntimeException("Não foi possível ler a imagem", e);
//        }
//    }

//    @GetMapping("/badges/{id}/image")
//    public ResponseEntity<Resource> getBadgeImageById(@PathVariable Long id) {
//        try {
//            Badge badge = badgeRepository.findById(id)
//                    .orElseThrow(() -> new RuntimeException("Badge não encontrado"));
//
//            // Pega o filename salvo no banco
//            String filename = badge.getImagePath();
//            if (filename == null) {
//                throw new RuntimeException("Badge não possui imagem");
//            }
//
//            // Monta o path completo
//            Path path = Paths.get("src/main/resources/static/badges/").resolve(filename);
//            Resource resource = new UrlResource(path.toUri());
//
//            if (!resource.exists()) {
//                throw new RuntimeException("Imagem não encontrada");
//            }
//
//            return ResponseEntity.ok()
//                    .contentType(MediaType.IMAGE_PNG)
//                    .body(resource);
//
//        } catch (MalformedURLException e) {
//            throw new RuntimeException("Não foi possível ler a imagem", e);
//        }
//    }


}
