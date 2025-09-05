package br.com.incode.nexus_bagde.service;

import br.com.incode.nexus_bagde.dto.*;
import br.com.incode.nexus_bagde.entitys.Badge;
import br.com.incode.nexus_bagde.entitys.BadgeAssignment;
import br.com.incode.nexus_bagde.entitys.Student;
import br.com.incode.nexus_bagde.repository.BadgeAssignmentRepository;
import br.com.incode.nexus_bagde.repository.BadgeRepository;
import br.com.incode.nexus_bagde.repository.Studentrepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BadgeAssignmentService {

    @Autowired
    private BadgeAssignmentRepository badgeAssignmentRepository;

    @Autowired
    private Studentrepository studentRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ModelMapper modelMapper;

    public List<BadgeAssignmentDTO> getAllAssignments() {
        return badgeAssignmentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BadgeAssignmentDTO> getAssignmentsByStudent(Long studentId) {
        return badgeAssignmentRepository.findByStudentId(studentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BadgeAssignmentDTO> getAssignmentsByBadge(Long badgeId) {
        return badgeAssignmentRepository.findByBadgeId(badgeId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BadgeAssignmentDTO assignBadge(BadgeAssignmentDTO assignmentDTO) {
        Student student = studentRepository.findById(assignmentDTO.getStudentId())
                .orElseThrow(() -> new RuntimeException("Estudante não encontrado"));

        Badge badge = badgeRepository.findById(assignmentDTO.getBadgeId())
                .orElseThrow(() -> new RuntimeException("Badge não encontrado"));

        if (badgeAssignmentRepository.existsByStudentIdAndBadgeId(
                assignmentDTO.getStudentId(), assignmentDTO.getBadgeId())) {
            throw new RuntimeException("Este badge já foi atribuído a este estudante");
        }

        BadgeAssignment assignment = new BadgeAssignment(student, badge, assignmentDTO.getAchievementReason());
        BadgeAssignment savedAssignment = badgeAssignmentRepository.save(assignment);

        if (assignmentDTO.getEmailSent() != null && assignmentDTO.getEmailSent()) {
            try {
                emailService.sendBadgeNotification(savedAssignment);
                savedAssignment.setEmailSent(true);
                savedAssignment.setEmailSentAt(LocalDateTime.now());
                savedAssignment = badgeAssignmentRepository.save(savedAssignment);
            } catch (Exception e) {
                System.err.println("Erro ao enviar email: " + e.getMessage());
            }
        }

        return convertToDTO(savedAssignment);
    }

    public void resendBadgeEmail(Long assignmentId) {
        BadgeAssignment assignment = badgeAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Atribuição não encontrada"));

        try {
            if (!assignment.isTokenValid()) {
                assignment.generateDownloadToken();
                assignment = badgeAssignmentRepository.save(assignment);
            }

            emailService.sendBadgeNotification(assignment);

            assignment.setEmailSent(true);
            assignment.setEmailSentAt(LocalDateTime.now());
            badgeAssignmentRepository.save(assignment);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar email: " + e.getMessage());
        }
    }

    public Optional<BadgeAssignment> findByDownloadToken(String token) {
        return badgeAssignmentRepository.findByDownloadToken(token);
    }

    public BadgeAssignment updateAssignment(BadgeAssignment assignment) {
        return badgeAssignmentRepository.save(assignment);
    }

    public void deleteAssignment(Long id) {
        if (!badgeAssignmentRepository.existsById(id)) {
            throw new RuntimeException("Atribuição não encontrada");
        }
        badgeAssignmentRepository.deleteById(id);
    }

    private BadgeAssignmentDTO convertToDTO(BadgeAssignment assignment) {
        BadgeAssignmentDTO dto = modelMapper.map(assignment, BadgeAssignmentDTO.class);
        dto.setStudentId(assignment.getStudent().getId());
        dto.setBadgeId(assignment.getBadge().getId());
        dto.setStudentName(assignment.getStudent().getName());
        dto.setStudentEmail(assignment.getStudent().getEmail());
        dto.setBadgeName(assignment.getBadge().getName());
        dto.setBadgeDescription(assignment.getBadge().getDescription());
        dto.setDownloadToken(assignment.getDownloadToken());
        dto.setDownloadCount(assignment.getDownloadCount());
        dto.setTokenExpiresAt(assignment.getTokenExpiresAt());
        return dto;
    }

    public AssertionDTO getAssignmentById(Long assignmentId) {
        return badgeAssignmentRepository.findById(assignmentId)
                .map(this::convertToAssertionDTO)
                .orElseThrow(() -> new RuntimeException("Assignment não encontrado"));
    }

    private AssertionDTO convertToAssertionDTO(BadgeAssignment assignment) {
        AssertionDTO assertion = new AssertionDTO();

        // Base URLs - podem ser externalizadas para um arquivo de configuração
        String baseUrl = "https://api.badgr.io/public"; // Exemplo do JSON fornecido

        assertion.setId(baseUrl + "/assertions/" + assignment.getId()); // Usando o ID da atribuição

        // Recipient
        String salt = UUID.randomUUID().toString();
        String identity = "sha256$" + DigestUtils.sha256Hex(assignment.getStudent().getEmail() + salt);
        assertion.setRecipient(new RecipientDTO("email", salt, true, identity));

        // Badge
        assertion.setBadge(baseUrl + "/badges/" + assignment.getBadge().getId());

        // IssuedOn
        assertion.setIssuedOn(assignment.getAssignedAt().atZone(ZoneOffset.UTC).toInstant().toString());

        // Image
        if (assignment.getBadge().getImagePath() != null && !assignment.getBadge().getImagePath().isEmpty()) {
            assertion.setImage(new ImageDTO(baseUrl + "/assertions/" + assignment.getId() + "/image"));
        }

        // Evidence and Narrative
        if (assignment.getAchievementReason() != null && !assignment.getAchievementReason().isEmpty()) {
            assertion.setEvidence(Collections.singletonList(new EvidenceDTO(assignment.getAchievementReason())));
            assertion.setNarrative(assignment.getAchievementReason());
        }

        // Revoked
        assertion.setRevoked(false); // Default

        // Verification
        assertion.setVerification(new VerificationDTO()); // Usa o tipo padrão "HostedBadge"

        // Recipient Profile Extension
        assertion.setRecipientProfile(new RecipientProfileDTO(assignment.getStudent().getName()));

        return assertion;
    }
}
