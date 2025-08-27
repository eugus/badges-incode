package br.com.incode.nexus_bagde.dto;


import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data

public class BadgeAssignmentDTO {

    private Long id;

    @NotNull(message = "ID do estudante é obrigatório")
    private Long studentId;

    @NotNull(message = "ID do badge é obrigatório")
    private Long badgeId;

    @Size(max = 500, message = "Motivo da conquista deve ter no máximo 500 caracteres")
    private String achievementReason;

    private Boolean emailSent;

    private String studentName;
    private String studentEmail;
    private String badgeName;
    private String badgeDescription;

    private String downloadToken;

    private Integer downloadCount;

    private LocalDateTime tokenExpiresAt;

    private ObjectNode openBadgeJson;

}
