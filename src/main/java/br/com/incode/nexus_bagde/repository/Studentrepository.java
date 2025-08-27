package br.com.incode.nexus_bagde.repository;

import br.com.incode.nexus_bagde.entitys.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface Studentrepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);

    Optional<Student> findByRegistration(String registration);

    List<Student> findByNameContainingIgnoreCase(String name);

    List<Student> findByCourseIgnoreCase(String course);

    @Query("SELECT s FROM Student s WHERE s.name LIKE %:search% OR s.email LIKE %:search% OR s.registration LIKE %:search%")
    List<Student> findBySearchTerm(@Param("search") String search);

    boolean existsByEmail(String email);

    boolean existsByRegistration(String registration);
}
