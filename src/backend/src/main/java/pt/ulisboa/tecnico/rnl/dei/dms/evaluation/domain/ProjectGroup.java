package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entity representing a project group.
 * Groups are automatically created and students are assigned to them.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "project_groups")
public class ProjectGroup {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * The project this group belongs to
     */
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * Group name/identifier (e.g., "Group 1", "Group A")
     */
    @Column(name = "group_name", nullable = false)
    private String groupName;

    /**
     * Creation timestamp
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Students in this group
     */
    @OneToMany(mappedBy = "projectGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectMember> members = new HashSet<>();

    /**
     * Submissions made by this group
     */
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectSubmission> submissions = new HashSet<>();

    /**
     * Final grade for this group (all members get the same grade)
     */
    @Column(name = "final_grade")
    private Double finalGrade;

    /**
     * Constructor for creating a new project group
     */
    public ProjectGroup(Project project, String groupName) {
        this.project = project;
        this.groupName = groupName;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Adds a student to this group
     */
    public void addMember(Person student) {
        if (canAddMoreMembers()) {
            ProjectMember member = new ProjectMember(this, student);
            members.add(member);
        } else {
            throw new IllegalStateException("Group is already full");
        }
    }

    /**
     * Removes a student from this group
     */
    public void removeMember(Person student) {
        members.removeIf(member -> member.getStudent().equals(student));
    }

    /**
     * Checks if the group can accept more members
     */
    public boolean canAddMoreMembers() {
        return project.getMaxGroupSize() == null || members.size() < project.getMaxGroupSize();
    }

    /**
     * Checks if the group is full
     */
    public boolean isFull() {
        return !canAddMoreMembers();
    }

    /**
     * Checks if the group is empty
     */
    public boolean isEmpty() {
        return members.isEmpty();
    }

    /**
     * Gets the number of members in this group
     */
    public int getMemberCount() {
        return members.size();
    }

    /**
     * Gets the latest submission for this group
     */
    public ProjectSubmission getLatestSubmission() {
        return submissions.stream()
                .max((s1, s2) -> s1.getSubmissionDate().compareTo(s2.getSubmissionDate()))
                .orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectGroup that = (ProjectGroup) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
