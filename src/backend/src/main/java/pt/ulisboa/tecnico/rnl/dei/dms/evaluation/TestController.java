package pt.ulisboa.tecnico.rnl.dei.dms.evaluation;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto.TestDto;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.service.TestService;

// REST controller for Test operations
@RestController
@RequestMapping("/tests")
public class TestController {

    @Autowired
    private TestService testService;

    @GetMapping
    public List<TestDto> getUpcomingTests() {
        return testService.getUpcomingTests();
    }

    @GetMapping("/{id}")
    public TestDto getTest(@PathVariable long id) {
        return testService.getTest(id);
    }

    @GetMapping("/curricular-unit/{curricularUnitId}")
    public List<TestDto> getTestsByCurricularUnit(@PathVariable long curricularUnitId) {
        return testService.getTestsByCurricularUnit(curricularUnitId);
    }

    @PostMapping("/curricular-unit/{curricularUnitId}")
    public TestDto createTest(
            @PathVariable long curricularUnitId,
            @RequestParam String title,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam Double weight,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime revisionDeadline) {
        return testService.createTest(curricularUnitId, title, date, weight, revisionDeadline);
    }

    @PutMapping("/{id}")
    public TestDto updateTest(
            @PathVariable long id,
            @RequestParam String title,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam Double weight,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime revisionDeadline) {
        return testService.updateTest(id, title, date, weight, revisionDeadline);
    }

    @DeleteMapping("/{id}")
    public void deleteTest(@PathVariable long id) {
        testService.deleteTest(id);
    }
}
