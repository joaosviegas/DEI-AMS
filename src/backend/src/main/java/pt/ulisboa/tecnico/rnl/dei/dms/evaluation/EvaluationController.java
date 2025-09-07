package pt.ulisboa.tecnico.rnl.dei.dms.evaluation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto.EvaluationCalendarDto;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto.ConflictNotificationDto;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.service.EvaluationCalendarService;

/**
 * REST controller for unified Evaluation calendar operations
 */
@RestController
@RequestMapping("/evaluations")
public class EvaluationController {

    @Autowired
    private EvaluationCalendarService evaluationCalendarService;

    /**
     * Get all evaluations (tests and projects) for calendar view
     */
    @GetMapping("/calendar")
    public List<EvaluationCalendarDto> getAllEvaluations() {
        return evaluationCalendarService.getAllEvaluationsForCalendar();
    }

    /**
     * Notify regent teachers about evaluation conflicts
     */
    @PostMapping("/notify-conflicts")
    public void notifyConflicts(@RequestBody List<ConflictNotificationDto> conflicts) {
        evaluationCalendarService.notifyConflicts(conflicts);
    }
}
