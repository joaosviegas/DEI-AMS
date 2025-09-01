package pt.ulisboa.tecnico.rnl.dei.dms.course.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.dms.course.domain.Course;

// Repository interface for managing Course entities
@Repository
@Transactional
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCode(String code);
    Optional<Course> findByName(String name);
    boolean existsByCode(String code);
    boolean existsByName(String name);
}