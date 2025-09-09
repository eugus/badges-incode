package br.com.incode.nexus_bagde.controller;


import br.com.incode.nexus_bagde.dto.BadgeDTO;
import br.com.incode.nexus_bagde.service.BadgeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/badges")

public class BadgeController {


    @Autowired
    private BadgeService badgeService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<List<BadgeDTO>> getAllBadges() {
        List<BadgeDTO> badges = badgeService.getAllBadges();
        return ResponseEntity.ok(badges);
    }

    @GetMapping("/active")
    public ResponseEntity<List<BadgeDTO>> getActiveBadges() {
        List<BadgeDTO> badges = badgeService.getActiveBadges();
        return ResponseEntity.ok(badges);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BadgeDTO> getBadgeById(@PathVariable Long id) {
        return badgeService.getBadgeById(id)
                .map(badge -> ResponseEntity.ok(badge))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<BadgeDTO>> getBadgesByCategory(@PathVariable String category) {
        List<BadgeDTO> badges = badgeService.getBadgesByCategory(category);
        return ResponseEntity.ok(badges);
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createBadge(
            @RequestPart("badge") String badgeJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestPart(value = "issuerImage", required = false) MultipartFile issuerImageFile) {
        try {
            // Converter JSON string para BadgeDTO
            BadgeDTO badgeDTO = objectMapper.readValue(badgeJson, BadgeDTO.class);

            // Validar manualmente se necessário
            if (badgeDTO.getName() == null || badgeDTO.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Nome do badge é obrigatório");
            }

            if (badgeDTO.getIssuer() == null || badgeDTO.getIssuer().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Emissor do badge é obrigatório");
            }

            BadgeDTO createdBadge = badgeService.createBadge(badgeDTO, imageFile, issuerImageFile);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBadge);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar dados: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao criar badge: " + e.getMessage());
        }
    }

//    @GetMapping("/public/assertions/{id}")
//    public ResponseEntity<?> getBadge(@PathVariable Long id) {
//        return badgeService.getBadgeById(id)
//                .map(badgeDTO -> ResponseEntity.ok(badgeDTO))
//                .orElseGet(() -> ResponseEntity.notFound().build());
//    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateBadge(
            @PathVariable Long id,
            @RequestPart("badge") String badgeJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestPart(value = "issuerImage", required = false) MultipartFile issuerImageFile) {
        try {
            // Converter JSON string para BadgeDTO
            BadgeDTO badgeDTO = objectMapper.readValue(badgeJson, BadgeDTO.class);

            // Validar manualmente se necessário
            if (badgeDTO.getName() == null || badgeDTO.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Nome do badge é obrigatório");
            }

            if (badgeDTO.getIssuer() == null || badgeDTO.getIssuer().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Emissor do badge é obrigatório");
            }

            BadgeDTO updatedBadge = badgeService.updateBadge(id, badgeDTO, imageFile, issuerImageFile);
            return ResponseEntity.ok(updatedBadge);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar dados : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao atualizar badge: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBadge(@PathVariable Long id) {
        try {
            badgeService.deleteBadge(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
