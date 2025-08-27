package br.com.incode.nexus_bagde.entitys;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "badge_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BadgeAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @Column(name = "email_sent", nullable = false)
    private Boolean emailSent = false;

    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    @Column(name = "achievement_reason", length = 500)
    private String achievementReason;

    @Column(name = "download_token", unique = true, length = 36)
    private String downloadToken;

    @Column(name = "download_count", nullable = false)
    private Integer downloadCount = 0;

    @Column(name = "last_downloaded_at")
    private LocalDateTime lastDownloadedAt;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    public void generateDownloadToken() {
        this.downloadToken = UUID.randomUUID().toString();
        // Token válido por 30 dias
        this.tokenExpiresAt = LocalDateTime.now().plusDays(30);
    }

    public boolean isTokenValid() {
        return this.tokenExpiresAt != null && LocalDateTime.now().isBefore(this.tokenExpiresAt);
    }

    public void incrementDownloadCount() {
        this.downloadCount++;
        this.lastDownloadedAt = LocalDateTime.now();
    }

    public BadgeAssignment(Student student, Badge badge, String achievementReason) {
        this.student = student;
        this.badge = badge;
        this.achievementReason = achievementReason;
    }
}
