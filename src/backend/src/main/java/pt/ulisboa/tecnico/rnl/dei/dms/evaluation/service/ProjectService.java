package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.Project;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.ProjectGroup;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.ProjectMember;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.ProjectSubmission;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.EvaluationGrade;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto.ProjectDto;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto.ProjectGroupDto;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto.ProjectSubmissionDto;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.repository.ProjectRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.repository.ProjectGroupRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.repository.ProjectSubmissionRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.repository.EvaluationGradeRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.domain.CurricularUnit;
import pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.repository.CurricularUnitRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.studentEnrollment.domain.StudentEnrollment;
import pt.ulisboa.tecnico.rnl.dei.dms.studentEnrollment.repository.StudentEnrollmentRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.dms.person.repository.PersonRepository;

/**
 * Service class for managing Project entities and related operations
 */
@Service
@Transactional
public class ProjectService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private ProjectGroupRepository projectGroupRepository;
    
    @Autowired
    private ProjectSubmissionRepository projectSubmissionRepository;

    @Autowired
    private CurricularUnitRepository curricularUnitRepository;
    
    @Autowired
    private StudentEnrollmentRepository studentEnrollmentRepository;
    
    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private EvaluationGradeRepository evaluationGradeRepository;

    @Autowired
    private EvaluationGradeService evaluationGradeService;

    // Helper methods
    private Project fetchProjectOrThrow(long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_TEST, "Project " + Long.toString(id)));
    }

    private CurricularUnit fetchCurricularUnitOrThrow(long id) {
        return curricularUnitRepository.findById(id)
                .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_CURRICULAR_UNIT, Long.toString(id)));
    }
    
    private Person fetchPersonOrThrow(long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_PERSON, Long.toString(id)));
    }

    // Project CRUD operations
    @Transactional
    public List<ProjectDto> getProjectsByCurricularUnit(long curricularUnitId) {
        return projectRepository.findByCurricularUnitIdOrderByDateAsc(curricularUnitId).stream()
                .map(ProjectDto::new)
                .toList();
    }

    @Transactional
    public ProjectDto getProject(long id) {
        return new ProjectDto(fetchProjectOrThrow(id));
    }

    @Transactional
    public ProjectDto createProject(long curricularUnitId, String title, 
                                  Double weight, LocalDateTime submissionDeadline, String description,
                                  Integer maxGroupSize) {
        CurricularUnit curricularUnit = fetchCurricularUnitOrThrow(curricularUnitId);
        
        // Validate weight
        if (weight < 0.0 || weight > 1.0) {
            throw new DEIException(ErrorMessage.TEST_WEIGHT_NOT_VALID, Double.toString(weight));
        }
        
        // Validate submission deadline
        if (submissionDeadline.isBefore(LocalDateTime.now())) {
            throw new DEIException(ErrorMessage.TEST_DATE_NOT_VALID, "Submission deadline cannot be in the past");
        }

        Project project;
        if (maxGroupSize != null && maxGroupSize > 1) {
            // Group project
            project = new Project(title, weight, curricularUnit, submissionDeadline, 
                                description, maxGroupSize);
        } else {
            // Individual project
            project = new Project(title, weight, curricularUnit, submissionDeadline, description);
        }

        project = projectRepository.save(project);
        
        // If it's a group project, create groups automatically
        if (project.isGroupProject()) {
            createGroupsForProject(project);
        }

        return new ProjectDto(project);
    }

    @Transactional
    public ProjectDto updateProject(long id, String title, Double weight,
                                  LocalDateTime submissionDeadline, String description, String allowedExtensions,
                                  Long maxFileSize) {
        Project project = fetchProjectOrThrow(id);
        
        // Validate weight
        if (weight < 0.0 || weight > 1.0) {
            throw new DEIException(ErrorMessage.INVALID_WEIGHT, Double.toString(weight));
        }

        project.setTitle(title);
        project.setDate(submissionDeadline); // Set evaluation date = submission deadline
        project.setWeight(weight);
        project.setSubmissionDeadline(submissionDeadline);
        project.setDescription(description);
        project.setAllowedExtensions(allowedExtensions);
        project.setMaxFileSize(maxFileSize);

        project = projectRepository.save(project);
        return new ProjectDto(project);
    }

    @Transactional
    public void deleteProject(long id) {
        Project project = fetchProjectOrThrow(id);
        
        // Since Project extends Evaluation with JOINED inheritance strategy,
        // we need to delete from both tables. The easiest way is to use
        // EntityManager to merge and remove the entity, ensuring both
        // the child (Project) and parent (Evaluation) records are deleted.
        project = entityManager.merge(project);
        entityManager.remove(project);
    }

    // Group management
    @Transactional
    public void createGroupsForProject(Project project) {
        if (!project.isGroupProject()) {
            return; // No groups needed for individual projects
        }
        
        // Get all enrolled students for this curricular unit
        List<StudentEnrollment> enrollments = studentEnrollmentRepository
                .findByCurricularUnitId(project.getCurricularUnit().getId());
        
        List<Person> students = enrollments.stream()
                .filter(enrollment -> enrollment.getStatus() == StudentEnrollment.EnrollmentStatus.ENROLLED)
                .map(StudentEnrollment::getStudent)
                .toList();
        
        if (students.isEmpty()) {
            return; // No students to assign to groups
        }
        
        // Shuffle students for random assignment
        List<Person> shuffledStudents = new ArrayList<>(students);
        Collections.shuffle(shuffledStudents);
        
        int maxGroupSize = project.getMaxGroupSize();
        int totalStudents = shuffledStudents.size();
        int groupNumber = 1;
        int studentIndex = 0;
        
        // Create groups and distribute all students
        while (studentIndex < totalStudents) {
            ProjectGroup group = new ProjectGroup(project, "Grupo " + groupNumber);
            group = projectGroupRepository.save(group);
            
            // Calculate how many students to add to this group
            int studentsLeftToAssign = totalStudents - studentIndex;
            int studentsForThisGroup = Math.min(maxGroupSize, studentsLeftToAssign);
            
            // Add students to this group
            for (int i = 0; i < studentsForThisGroup; i++) {
                Person student = shuffledStudents.get(studentIndex++);
                ProjectMember member = new ProjectMember(group, student);
                entityManager.persist(member);
                group.getMembers().add(member);
            }
            
            // Save group with all members
            projectGroupRepository.save(group);
            groupNumber++;
        }
        
        // Force flush to ensure all entities are persisted
        entityManager.flush();
    }

    @Transactional
    public List<ProjectGroupDto> getProjectGroups(long projectId) {
        return projectGroupRepository.findByProjectIdOrderByGroupNameAsc(projectId).stream()
                .map(ProjectGroupDto::new)
                .toList();
    }

    @Transactional
    public void integrateNewStudentIntoGroups(long curricularUnitId, long studentId) {
        Person student = fetchPersonOrThrow(studentId);
        
        // Find all group projects for this curricular unit
        List<Project> groupProjects = projectRepository.findByCurricularUnitIdOrderByDateAsc(curricularUnitId).stream()
                .filter(Project::isGroupProject)
                .filter(project -> !project.isCompleted()) // Only add to ongoing/future projects
                .toList();
        
        for (Project project : groupProjects) {
            // Check if student is already in a group for this project
            ProjectGroup existingGroup = projectGroupRepository.findByProjectIdAndStudentId(project.getId(), studentId);
            if (existingGroup != null) {
                continue; // Student already in a group for this project
            }
            
            // Find the smallest group to maintain balance
            List<ProjectGroup> groups = projectGroupRepository.findByProjectIdOrderByGroupNameAsc(project.getId());
            if (groups.isEmpty()) {
                // No groups exist yet, create one
                ProjectGroup newGroup = new ProjectGroup(project, "Grupo 1");
                newGroup = projectGroupRepository.save(newGroup);
                ProjectMember member = new ProjectMember(newGroup, student);
                entityManager.persist(member);
                newGroup.getMembers().add(member);
                projectGroupRepository.save(newGroup);
            } else {
                // Find the group with the fewest members
                ProjectGroup smallestGroup = groups.stream()
                        .min((g1, g2) -> Integer.compare(g1.getMembers().size(), g2.getMembers().size()))
                        .orElse(groups.get(0));
                
                // Only add if the group isn't at max capacity
                if (smallestGroup.getMembers().size() < project.getMaxGroupSize()) {
                    ProjectMember member = new ProjectMember(smallestGroup, student);
                    entityManager.persist(member);
                    smallestGroup.getMembers().add(member);
                    projectGroupRepository.save(smallestGroup);
                } else {
                    // All groups are full, create a new one
                    int nextGroupNumber = groups.size() + 1;
                    ProjectGroup newGroup = new ProjectGroup(project, "Grupo " + nextGroupNumber);
                    newGroup = projectGroupRepository.save(newGroup);
                    ProjectMember member = new ProjectMember(newGroup, student);
                    entityManager.persist(member);
                    newGroup.getMembers().add(member);
                    projectGroupRepository.save(newGroup);
                }
            }
        }
    }

    @Transactional
    public ProjectGroupDto getStudentGroup(long projectId, long studentId) {
        ProjectGroup group = projectGroupRepository.findByProjectIdAndStudentId(projectId, studentId);
        return group != null ? new ProjectGroupDto(group) : null;
    }

    // Submission management
    @Transactional
    public ProjectSubmissionDto submitProject(long projectId, long studentId, String originalFilename, 
                                            String storedFilename, long fileSize, String mimeType) {
        Project project = fetchProjectOrThrow(projectId);
        Person student = fetchPersonOrThrow(studentId);
        
        // Check if submission is still allowed
        if (LocalDateTime.now().isAfter(project.getSubmissionDeadline())) {
            throw new DEIException(ErrorMessage.SUBMISSION_DEADLINE_EXCEEDED);
        }
        
        // Check file size if limit is set
        if (project.getMaxFileSize() != null && fileSize > project.getMaxFileSize()) {
            throw new DEIException(ErrorMessage.FILE_SIZE_EXCEEDED, String.valueOf(project.getMaxFileSize()));
        }
        
        // Check file extension if restrictions exist
        String extension = getFileExtension(originalFilename);
        if (project.getAllowedExtensions() != null && !project.getAllowedExtensions().isEmpty()) {
            if (!project.getAllowedExtensions().toLowerCase().contains(extension.toLowerCase())) {
                throw new DEIException(ErrorMessage.INVALID_FILE_EXTENSION, extension);
            }
        }

        ProjectSubmission submission;
        if (project.isGroupProject()) {
            // For group projects, find the student's group
            ProjectGroup group = projectGroupRepository.findByProjectIdAndStudentId(projectId, studentId);
            if (group == null) {
                throw new DEIException(ErrorMessage.STUDENT_NOT_IN_GROUP);
            }
            
            // Mark any existing group submission as not latest
            ProjectSubmission existingSubmission = projectSubmissionRepository
                    .findLatestByProjectIdAndGroupId(projectId, group.getId());
            
            if (existingSubmission != null) {
                existingSubmission.setIsLatest(false);
                projectSubmissionRepository.save(existingSubmission);
            }
            
            // Create new group submission
            submission = new ProjectSubmission(project, group, student, originalFilename, 
                                             storedFilename, fileSize, mimeType, extension);
            submission = projectSubmissionRepository.save(submission);
        } else {
            // Individual project submission
            ProjectSubmission existingSubmission = projectSubmissionRepository
                    .findLatestByProjectIdAndStudentId(projectId, studentId);
            
            if (existingSubmission != null) {
                existingSubmission.setIsLatest(false);
                projectSubmissionRepository.save(existingSubmission);
            }
            
            // Create new individual submission
            submission = new ProjectSubmission(project, student, originalFilename, 
                                             storedFilename, fileSize, mimeType, extension);
            submission = projectSubmissionRepository.save(submission);
        }
        
        return new ProjectSubmissionDto(submission);
    }

    @Transactional
    public List<ProjectSubmissionDto> getProjectSubmissions(long projectId) {
        return projectSubmissionRepository.findByProjectIdOrderBySubmissionDateDesc(projectId).stream()
                .map(ProjectSubmissionDto::new)
                .toList();
    }

    @Transactional
    public List<ProjectSubmissionDto> getGroupSubmissions(long projectId, long groupId) {
        return projectSubmissionRepository.findByProjectIdAndGroupIdOrderBySubmissionDateDesc(projectId, groupId).stream()
                .map(ProjectSubmissionDto::new)
                .toList();
    }

    @Transactional
    public ProjectSubmissionDto getLatestGroupSubmission(long projectId, long groupId) {
        ProjectSubmission submission = projectSubmissionRepository.findLatestByProjectIdAndGroupId(projectId, groupId);
        return submission != null ? new ProjectSubmissionDto(submission) : null;
    }

    @Transactional
    public ProjectSubmissionDto submitProjectForGroup(long projectId, long groupId, long studentId, 
                                                    String originalFilename, String storedFilename, 
                                                    long fileSize, String mimeType) {
        Project project = fetchProjectOrThrow(projectId);
        Person student = fetchPersonOrThrow(studentId);
        
        // Verify that the group exists and belongs to this project
        ProjectGroup group = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_TEST, "Group " + groupId));
        
        if (!group.getProject().getId().equals(projectId)) {
            throw new DEIException(ErrorMessage.NO_SUCH_TEST, "Group does not belong to project " + projectId);
        }
        
        // Verify that the student is a member of this group
        boolean isMember = group.getMembers().stream()
                .anyMatch(member -> member.getStudent().getId().equals(studentId));
        
        if (!isMember) {
            throw new DEIException(ErrorMessage.STUDENT_NOT_IN_GROUP);
        }
        
        // Check if submission is still allowed
        if (LocalDateTime.now().isAfter(project.getSubmissionDeadline())) {
            throw new DEIException(ErrorMessage.SUBMISSION_DEADLINE_EXCEEDED);
        }
        
        // Check file size and extension (same validation as before)
        if (project.getMaxFileSize() != null && fileSize > project.getMaxFileSize()) {
            throw new DEIException(ErrorMessage.FILE_SIZE_EXCEEDED, String.valueOf(project.getMaxFileSize()));
        }
        
        String extension = getFileExtension(originalFilename);
        if (project.getAllowedExtensions() != null && !project.getAllowedExtensions().isEmpty()) {
            if (!project.getAllowedExtensions().toLowerCase().contains(extension.toLowerCase())) {
                throw new DEIException(ErrorMessage.INVALID_FILE_EXTENSION, extension);
            }
        }
        
        // Mark any existing group submission as not latest
        ProjectSubmission existingSubmission = projectSubmissionRepository
                .findLatestByProjectIdAndGroupId(projectId, groupId);
        
        if (existingSubmission != null) {
            existingSubmission.setIsLatest(false);
            projectSubmissionRepository.save(existingSubmission);
        }
        
        // Create new group submission
        ProjectSubmission submission = new ProjectSubmission(project, group, student, originalFilename, 
                                                           storedFilename, fileSize, mimeType, extension);
        submission = projectSubmissionRepository.save(submission);
        
        return new ProjectSubmissionDto(submission);
    }

    @Transactional
    public ProjectSubmissionDto getStudentSubmission(long projectId, long studentId) {
        Project project = fetchProjectOrThrow(projectId);
        
        ProjectSubmission submission;
        if (project.isGroupProject()) {
            ProjectGroup group = projectGroupRepository.findByProjectIdAndStudentId(projectId, studentId);
            if (group == null) {
                return null;
            }
            submission = projectSubmissionRepository.findLatestByProjectIdAndGroupId(projectId, group.getId());
        } else {
            submission = projectSubmissionRepository.findLatestByProjectIdAndStudentId(projectId, studentId);
        }
        
        return submission != null ? new ProjectSubmissionDto(submission) : null;
    }

    @Transactional
    public ProjectSubmissionDto gradeSubmission(long submissionId, double grade, String feedback, long graderId) {
        ProjectSubmission submission = projectSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new DEIException(ErrorMessage.SUBMISSION_NOT_FOUND));
        
        // Validate that grader exists
        fetchPersonOrThrow(graderId);
        
        // Validate grade (assuming 0-20 scale)
        if (grade < 0.0 || grade > 20.0) {
            throw new DEIException(ErrorMessage.INVALID_GRADE, Double.toString(grade));
        }
        
        // Since the ProjectSubmission doesn't have manual grading fields in the current implementation,
        // we'll use the automatic grading fields for now
        submission.setAutomaticGrade(grade);
        submission.setAutomaticFeedback(feedback);
        
        submission = projectSubmissionRepository.save(submission);
        return new ProjectSubmissionDto(submission);
    }

    @Transactional
    public void deleteSubmission(long submissionId) {
        ProjectSubmission submission = projectSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new DEIException(ErrorMessage.SUBMISSION_NOT_FOUND));
        
        projectSubmissionRepository.delete(submission);
    }

    // Automatic grading placeholder (can be extended with actual grading logic)
    @Transactional
    public void performAutomaticGrading(long projectId) {
        
        List<ProjectSubmission> submissions = projectSubmissionRepository.findByProjectId(projectId);
        
        for (ProjectSubmission submission : submissions) {
            if (submission.getAutomaticGrade() == null && submission.getIsLatest()) { 
                // Only grade latest ungraded submissions
                double automaticGrade = performAutomaticGradingLogic(submission);
                
                submission.setAutomaticGrade(automaticGrade);
                submission.setAutomaticFeedback("Automatically graded");
                projectSubmissionRepository.save(submission);
            }
        }
    }

    // Helper methods
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
    
    private double performAutomaticGradingLogic(ProjectSubmission submission) {
        // Placeholder implementation - return a default grade
        // In a real implementation, this would:
        // 1. Extract and analyze the submitted file
        // 2. Run automated tests or checks
        // 3. Calculate grade based on results
        return 15.0; // Default grade of 15/20
    }

    // Group grading methods
    @Transactional
    public void gradeGroup(long projectId, long groupId, double grade) {
        Project project = fetchProjectOrThrow(projectId);
        ProjectGroup group = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_PERSON, "Group " + Long.toString(groupId)));
        
        // Validate that the group belongs to the project
        if (!group.getProject().getId().equals(projectId)) {
            throw new DEIException(ErrorMessage.RESOURCE_NOT_FOUND, "Group does not belong to project");
        }
        
        // Validate grade
        if (grade < 0.0 || grade > 20.0) {
            throw new DEIException(ErrorMessage.INVALID_GRADE, Double.toString(grade));
        }
        
        // Save the grade to the group
        group.setFinalGrade(grade);
        projectGroupRepository.save(group);
        
        // Also save individual grades for each group member for future average calculations
        saveIndividualGradesForGroupMembers(project, group, grade);
    }
    
    private void saveIndividualGradesForGroupMembers(Project project, ProjectGroup group, double grade) {
        // Get the evaluation grade service to save individual grades
        // This method creates EvaluationGrade entries for each group member
        
        for (ProjectMember member : group.getMembers()) {
            // Find the student enrollment for this member in the project's curricular unit
            StudentEnrollment enrollment = studentEnrollmentRepository
                    .findByStudentIdAndCurricularUnitId(member.getStudent().getId(), 
                                                       project.getCurricularUnit().getId())
                    .orElse(null);
            
            if (enrollment != null) {
                // Check if an evaluation grade already exists for this student and evaluation
                EvaluationGrade existingGrade = evaluationGradeRepository
                        .findByEvaluationIdAndStudentEnrollmentId(project.getId(), enrollment.getId())
                        .orElse(null);
                
                if (existingGrade != null) {
                    // Update existing grade
                    existingGrade.setGrade(grade);
                    existingGrade.setGradedAt(LocalDateTime.now());
                    evaluationGradeRepository.save(existingGrade);
                } else {
                    // Create new evaluation grade
                    EvaluationGrade newGrade = new EvaluationGrade(project, enrollment, grade);
                    evaluationGradeRepository.save(newGrade);
                }
                
                // Check if student has completed all evaluations and update enrollment if necessary
                evaluationGradeService.checkAndUpdateStudentCompletion(enrollment);
            }
        }
    }
}
