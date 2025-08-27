package br.com.incode.nexus_bagde.controller;

import br.com.incode.nexus_bagde.dto.BadgeAssignmentDTO;
import br.com.incode.nexus_bagde.service.BadgeAssignmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")

public class BadgeAssignmentController {

    @Autowired
    private BadgeAssignmentService badgeAssignmentService;

    @GetMapping
    public ResponseEntity<List<BadgeAssignmentDTO>> getAllAssignments() {
        List<BadgeAssignmentDTO> assignments = badgeAssignmentService.getAllAssignments();
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<BadgeAssignmentDTO>> getAssignmentsByStudent(@PathVariable Long studentId) {
        List<BadgeAssignmentDTO> assignments = badgeAssignmentService.getAssignmentsByStudent(studentId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/badge/{badgeId}")
    public ResponseEntity<List<BadgeAssignmentDTO>> getAssignmentsByBadge(@PathVariable Long badgeId) {
        List<BadgeAssignmentDTO> assignments = badgeAssignmentService.getAssignmentsByBadge(badgeId);
        return ResponseEntity.ok(assignments);
    }

    @PostMapping
    public ResponseEntity<BadgeAssignmentDTO> assignBadge(@Valid @RequestBody BadgeAssignmentDTO assignmentDTO) {
        try {
            BadgeAssignmentDTO createdAssignment = badgeAssignmentService.assignBadge(assignmentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAssignment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/resend-email")
    public ResponseEntity<?> resendBadgeEmail(@PathVariable Long id) {
        try {
            badgeAssignmentService.resendBadgeEmail(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body("sd:" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        try {
            badgeAssignmentService.deleteAssignment(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
