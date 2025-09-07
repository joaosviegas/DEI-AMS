package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.service;

import java.util.List;
import java.time.LocalDateTime;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.Test;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto.TestDto;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.repository.TestRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.domain.CurricularUnit;
import pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.repository.CurricularUnitRepository;

// Service class for managing Test entities
@Service
@Transactional
public class TestService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private CurricularUnitRepository curricularUnitRepository;

    private Test fetchTestOrThrow(long id) {
        return testRepository.findById(id)
                .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_TEST, Long.toString(id)));
    }

    private CurricularUnit fetchCurricularUnitOrThrow(long id) {
        return curricularUnitRepository.findById(id)
                .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_CURRICULAR_UNIT, Long.toString(id)));
    }

    @Transactional
    public List<TestDto> getTestsByCurricularUnit(long curricularUnitId) {
        return testRepository.findByCurricularUnitIdOrderByDateAsc(curricularUnitId).stream()
                .map(TestDto::new)
                .toList();
    }

    @Transactional
    public TestDto getTest(long id) {
        return new TestDto(fetchTestOrThrow(id));
    }

    @Transactional
    public TestDto createTest(long curricularUnitId, String title, LocalDateTime date, Double weight, LocalDateTime revisionDeadline) {
        CurricularUnit curricularUnit = fetchCurricularUnitOrThrow(curricularUnitId);

        // Validate input
        if (title == null || title.isBlank()) {
            throw new DEIException(ErrorMessage.TEST_TITLE_NOT_VALID);
        }

        if (date == null || date.isBefore(LocalDateTime.now())) {
            throw new DEIException(ErrorMessage.TEST_DATE_NOT_VALID);
        }

        if (weight == null || weight < 0.0 || weight > 1.0) {
            throw new DEIException(ErrorMessage.TEST_WEIGHT_NOT_VALID);
        }
        
        if (revisionDeadline == null || revisionDeadline.isBefore(date)) {
            throw new DEIException(ErrorMessage.TEST_DATE_NOT_VALID, "Revision deadline must be after evaluation date");
        }

        // Check total weight of evaluations
        double currentTotalWeight = curricularUnit.getEvaluations().stream()
                .mapToDouble(pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.Evaluation::getWeight)
                .sum();

        if (currentTotalWeight + weight > 1.0) {
            throw new DEIException(ErrorMessage.EVALUATION_WEIGHT_EXCEEDS_LIMIT);
        }

        Test test = new Test(title, date, weight, curricularUnit, revisionDeadline);
        return new TestDto(testRepository.save(test));

        // Note: Consider checking if the total weight of evaluations exceeds 1.0 before adding this test
    }

    @Transactional
    public TestDto updateTest(long id, String title, LocalDateTime date, Double weight, LocalDateTime revisionDeadline) {
        Test test = fetchTestOrThrow(id);

        // Validate input
        if (title == null || title.isBlank()) {
            throw new DEIException(ErrorMessage.TEST_TITLE_NOT_VALID);
        }

        if (date == null || date.isBefore(LocalDateTime.now())) {
            throw new DEIException(ErrorMessage.TEST_DATE_NOT_VALID);
        }

        if (weight == null || weight < 0.0 || weight > 1.0) {
            throw new DEIException(ErrorMessage.TEST_WEIGHT_NOT_VALID);
        }
        
        if (revisionDeadline == null || revisionDeadline.isBefore(date)) {
            throw new DEIException(ErrorMessage.TEST_DATE_NOT_VALID, "Revision deadline must be after evaluation date");
        }

        // Check total weight of evaluations
        double otherEvaluationsWeight = test.getCurricularUnit().getEvaluations().stream()
                .filter(e -> !e.getId().equals(id))
                .mapToDouble(pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.Evaluation::getWeight)
                .sum();

        if (otherEvaluationsWeight + weight > 1.0) {
            throw new DEIException(ErrorMessage.EVALUATION_WEIGHT_EXCEEDS_LIMIT);
        }

        test.setTitle(title);
        test.setDate(date);
        test.setWeight(weight);
        test.setRevisionDeadline(revisionDeadline);

        return new TestDto(testRepository.save(test));
    }

    @Transactional
    public void deleteTest(long id) {
        Test test = fetchTestOrThrow(id);
        
        // Since Test extends Evaluation with JOINED inheritance strategy,
        // we need to delete from both tables. The easiest way is to use
        // EntityManager to merge and remove the entity, ensuring both
        // the child (Test) and parent (Evaluation) records are deleted.
        test = entityManager.merge(test);
        entityManager.remove(test);
    }

    @Transactional
    public List<TestDto> getUpcomingTests() {
        return testRepository.findUpcomingTests(LocalDateTime.now()).stream()
                .map(TestDto::new)
                .toList();
    }
}
