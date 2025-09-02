package pt.ulisboa.tecnico.rnl.dei.dms.studentEnrollment.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.domain.CurricularUnit;

import java.time.LocalDate;

// Domain class representing a student's enrollment in a curricular unit
@Data
@Entity
@Getter
@Setter
@Table(name = "student_enrollments")
public class StudentEnrollment {

	public enum EnrollmentStatus {
		ENROLLED,
		APPROVED,
		FAILED
	}

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	@JoinColumn(name = "student_id", nullable = false)
	private Person student;

	@ManyToOne
	@JoinColumn(name = "curricular_unit_id", nullable = false)
	private CurricularUnit curricularUnit;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private EnrollmentStatus status;

	@Column(name = "enrollment_date", nullable = false)
	private LocalDate enrollmentDate;

	@Column(name = "final_grade")
	private Double finalGrade; // calculated from evaluations

	protected StudentEnrollment() {
	}

	public StudentEnrollment(Person student, CurricularUnit curricularUnit, EnrollmentStatus status) {
		this.student = student;
		this.curricularUnit = curricularUnit;
		this.status = status;
		this.enrollmentDate = LocalDate.now();
	}

	public StudentEnrollment(Person student, CurricularUnit curricularUnit) {
		this(student, curricularUnit, EnrollmentStatus.ENROLLED);
	}
}
