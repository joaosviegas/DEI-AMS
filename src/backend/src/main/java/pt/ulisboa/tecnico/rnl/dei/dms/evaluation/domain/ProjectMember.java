package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a member of a project group.
 * Similar to JuryMember but for project groups.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "project_members")
public class ProjectMember {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * The project group this member belongs to
     */
    @ManyToOne
    @JoinColumn(name = "project_group_id", nullable = false)
    private ProjectGroup projectGroup;

    /**
     * The student who is a member of this project group
     */
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Person student;

    /**
     * When this member was added to the group
     */
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    /**
     * Constructor for creating a new project member
     */
    public ProjectMember(ProjectGroup projectGroup, Person student) {
        this.projectGroup = projectGroup;
        this.student = student;
        this.joinedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectMember that = (ProjectMember) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
