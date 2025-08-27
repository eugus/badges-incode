package br.com.incode.nexus_bagde.repository;

import br.com.incode.nexus_bagde.entitys.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    List<Badge> findByIsActiveTrue();

    List<Badge> findByCategory(String category);

    List<Badge> findByNameContainingIgnoreCase(String name);

    List<Badge> findByCategoryAndIsActiveTrue(String category);
}
