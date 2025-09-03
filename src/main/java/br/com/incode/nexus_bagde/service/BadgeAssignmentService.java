package br.com.incode.nexus_bagde.service;


import br.com.incode.nexus_bagde.dto.BadgeAssignmentDTO;
import br.com.incode.nexus_bagde.entitys.Badge;
import br.com.incode.nexus_bagde.entitys.BadgeAssignment;
import br.com.incode.nexus_bagde.entitys.Student;
import br.com.incode.nexus_bagde.repository.BadgeAssignmentRepository;
import br.com.incode.nexus_bagde.repository.BadgeRepository;
import br.com.incode.nexus_bagde.repository.Studentrepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.codec.digest.DigestUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.http.HttpHeaders;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
        // Verificar se estudante existe
        Student student = studentRepository.findById(assignmentDTO.getStudentId())
                .orElseThrow(() -> new RuntimeException("Estudante não encontrado"));

        // Verificar se badge existe
        Badge badge = badgeRepository.findById(assignmentDTO.getBadgeId())
                .orElseThrow(() -> new RuntimeException("Badge não encontrado"));

        // Verificar se já foi atribuído
        if (badgeAssignmentRepository.existsByStudentIdAndBadgeId(
                assignmentDTO.getStudentId(), assignmentDTO.getBadgeId())) {
            throw new RuntimeException("Este badge já foi atribuído a este estudante");
        }

        // Criar atribuição
        BadgeAssignment assignment = new BadgeAssignment(student, badge, assignmentDTO.getAchievementReason());
        BadgeAssignment savedAssignment = badgeAssignmentRepository.save(assignment);

        // Enviar email se solicitado
        if (assignmentDTO.getEmailSent() != null && assignmentDTO.getEmailSent()) {
            try {
                emailService.sendBadgeNotification(savedAssignment);
                savedAssignment.setEmailSent(true);
                savedAssignment.setEmailSentAt(LocalDateTime.now());
                savedAssignment = badgeAssignmentRepository.save(savedAssignment);
                System.out.println("Email enviado com sucesso para: " + student.getEmail());
                System.out.println("Token de download: " + savedAssignment.getDownloadToken());
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
            // Gerar novo token se o atual expirou
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


    public BadgeAssignmentDTO getAssignmentById(Long assignmentId) {
        return badgeAssignmentRepository.findById(assignmentId)
                .map(assignment -> {
                    BadgeAssignmentDTO dto = modelMapper.map(assignment, BadgeAssignmentDTO.class);
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode openBadgeJson = mapper.createObjectNode();

                    // Contexto e tipo
                    openBadgeJson.put("@context", "https://w3id.org/openbadges/v2");
                    openBadgeJson.put("type", "Assertion");
                    openBadgeJson.put("id", "http://badges-incode-production.up.railway.app/api/public/assertions/"
                            + assignment.getId() + "/open-badge");

                    // Recipient
                    ObjectNode recipientNode = openBadgeJson.putObject("recipient");
                    recipientNode.put("type", "email");

                    // Hash opcional do email
                    String salt = UUID.randomUUID().toString().substring(0, 16);
                    recipientNode.put("salt", salt);
                    recipientNode.put("hashed", true);
                    recipientNode.put("identity", "sha256$" + DigestUtils.sha256Hex(assignment.getStudent().getEmail() + salt));

                    // Badge
                    openBadgeJson.put("badge", "http://badges-incode-production.up.railway.app/api/badges/"
                            + assignment.getBadge().getId());


                    // Badge image URL
                    if (assignment.getBadge().getImagePath() != null) {
                        ObjectNode imageNode = openBadgeJson.putObject("image");
                        imageNode.put("id", "http://badges-incode-production.up.railway.app/api/public/assertions/badges/"
                                + assignment.getBadge().getId() + "/image");
                    }

                    // Evidence / narrative
                    if (assignment.getAchievementReason() != null) {
                        ObjectNode evidenceNode = mapper.createObjectNode();
                        evidenceNode.put("narrative", assignment.getAchievementReason());
                        openBadgeJson.set("evidence", mapper.createArrayNode().add(evidenceNode));
                        openBadgeJson.put("narrative", assignment.getAchievementReason());
                        openBadgeJson.put("revoked", false);
                    }

                    ObjectNode criteriaNode = openBadgeJson.putObject("criteria");
                    criteriaNode.put("id", "http://badges-incode-production.up.railway.app/api/badges/"
                            + assignment.getBadge().getId() + "/criteria");
                    criteriaNode.put("narrative", "O estudante completou as atividades necessárias.");

                    // IssuedOn
                    openBadgeJson.put("issuedOn", assignment.getAssignedAt().atZone(ZoneOffset.UTC).toInstant().toString());

                    // Verification
                    ObjectNode verificationNode = openBadgeJson.putObject("verification");
                    verificationNode.put("type", "HostedBadge");

                    // Issuer
                    ObjectNode issuerNode = openBadgeJson.putObject("issuer");
                    issuerNode.put("id", assignment.getBadge().getIssuer());
                    issuerNode.put("type", "Issuer");
                    issuerNode.put("name", assignment.getBadge().getIssuer());

                    // Issuer image URL
                    if (assignment.getBadge().getIssuerImagePath() != null) {
                        issuerNode.put("image", "http://badges-incode-production.up.railway.app/api/public/assertions/issuers/"
                                + assignment.getBadge().getId() + "/image");
                    }

                    // Extensions: RecipientProfile
                    ObjectNode extensionNode = openBadgeJson.putObject("extensions:recipientProfile");
                    extensionNode.put("name", assignment.getStudent().getName());
                    extensionNode.put("@context", "https://openbadgespec.org/extensions/recipientProfile/context.json");
                    extensionNode.putArray("type").add("Extension").add("extensions:RecipientProfile");

                    dto.setOpenBadgeJson(openBadgeJson);
                    return dto;
                })
                .orElseThrow(() -> new RuntimeException("Assignment não encontrado"));
    }



}
