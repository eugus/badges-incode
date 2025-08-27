package br.com.incode.nexus_bagde.controller;

import br.com.incode.nexus_bagde.dto.BadgeAssignmentDTO;
import br.com.incode.nexus_bagde.service.BadgeAssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/public/assertions")
public class PublicBadgeController {

    private final BadgeAssignmentService badgeAssignmentService;

    public PublicBadgeController(BadgeAssignmentService badgeAssignmentService) {
        this.badgeAssignmentService = badgeAssignmentService;
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
}
