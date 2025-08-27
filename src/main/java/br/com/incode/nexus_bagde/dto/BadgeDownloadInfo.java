package br.com.incode.nexus_bagde.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BadgeDownloadInfo {



    private String badgeName;
    private String badgeDescription;
    private String badgeCategory;
    private String badgeImagePath; // ADICIONADO
    private String issuer;
    private String issuerImagePath;
    private String studentName;
    private String achievementReason;
    private LocalDateTime assignedAt;
    private Integer downloadCount;
    private LocalDateTime tokenExpiresAt;
    private Long assignmentId;




}
