package br.com.incode.nexus_bagde.repository;

import br.com.incode.nexus_bagde.entitys.BadgeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface BadgeAssignmentRepository extends JpaRepository<BadgeAssignment, Long> {


    List<BadgeAssignment> findByStudentId(Long studentId);

    List<BadgeAssignment> findByBadgeId(Long badgeId);

    List<BadgeAssignment> findByEmailSentFalse();

    Optional<BadgeAssignment> findByDownloadToken(String downloadToken);

    @Query("SELECT ba FROM BadgeAssignment ba WHERE ba.student.id = :studentId AND ba.badge.id = :badgeId")
    List<BadgeAssignment> findByStudentIdAndBadgeId(@Param("studentId") Long studentId, @Param("badgeId") Long badgeId);

    boolean existsByStudentIdAndBadgeId(Long studentId, Long badgeId);


}
