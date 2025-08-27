package br.com.incode.nexus_bagde.controller;

import br.com.incode.nexus_bagde.dto.BadgeValidationRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/badges")
public class BadgeValidationController {


    private final ObjectMapper objectMapper;

    public BadgeValidationController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @PostMapping("/validate")
    public ResponseEntity<?> validateBadge(@RequestBody BadgeValidationRequest request) {
        try {
            JsonNode badgeNode = request.getBadgeJson(); // <-- usar openBadgeJson

            // Validar destinatário
            String recipient = badgeNode.path("recipient").path("identity").asText(null);
            if (recipient == null || !recipient.equals(request.getRecipient())) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "errors", List.of("Destinatário inválido ou não confere")
                ));
            }

            // Validar se badge está ativo
            boolean isActive = badgeNode.path("isActive").asBoolean(true);
            if (!isActive) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "errors", List.of("Badge inativo")
                ));
            }

            // Validar data de emissão
            String issuedOn = badgeNode.path("issuedOn").asText(null);
            if (issuedOn == null) {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "errors", List.of("Badge sem data de emissão")
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "errors", List.of()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "errors", List.of("JSON inválido ou erro interno: " + e.getMessage())
            ));
        }
    }
}
