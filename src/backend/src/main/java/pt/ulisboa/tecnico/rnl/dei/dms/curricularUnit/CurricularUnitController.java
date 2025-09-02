package pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.dto.CurricularUnitDto;
import pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.service.CurricularUnitService;

@RestController
public class CurricularUnitController {
    
    @Autowired
    private CurricularUnitService curricularUnitService;

    @GetMapping("/curricular-units")
    public List<CurricularUnitDto> getCurricularUnits() {
        return curricularUnitService.getCurricularUnits();
    }

    @PostMapping("/curricular-units")
    public CurricularUnitDto createCurricularUnit(
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam String semester,
            @RequestParam Integer ects,
            @RequestParam Long mainTeacherId) {
        return curricularUnitService.createCurricularUnit(code, name, semester, ects, mainTeacherId);
    }

    @GetMapping("/curricular-units/{id}")
    public CurricularUnitDto getCurricularUnit(@PathVariable long id) {
        return curricularUnitService.getCurricularUnit(id);
    }

    @PutMapping("/curricular-units/{id}")
    public CurricularUnitDto updateCurricularUnit(
            @PathVariable long id,
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam String semester,
            @RequestParam Integer ects,
            @RequestParam Long mainTeacherId) {
        return curricularUnitService.updateCurricularUnit(id, code, name, semester, ects, mainTeacherId);
    }

    @DeleteMapping("/curricular-units/{id}")
    public void deleteCurricularUnit(@PathVariable long id) {
        curricularUnitService.deleteCurricularUnit(id);
    }

    // Course management endpoints
    @PostMapping("/curricular-units/{id}/courses/{courseId}")
    public CurricularUnitDto addCourseToCurricularUnit(
            @PathVariable long id, 
            @PathVariable long courseId) {
        return curricularUnitService.addCourseToCurricularUnit(id, courseId);
    }

    @DeleteMapping("/curricular-units/{id}/courses/{courseId}")
    public CurricularUnitDto removeCourseFromCurricularUnit(
            @PathVariable long id, 
            @PathVariable long courseId) {
        return curricularUnitService.removeCourseFromCurricularUnit(id, courseId);
    }

    // Assistant teacher management endpoints
    @PostMapping("/curricular-units/{id}/assistant-teachers/{teacherId}")
    public CurricularUnitDto addAssistantTeacher(
            @PathVariable long id, 
            @PathVariable long teacherId) {
        return curricularUnitService.addAssistantTeacher(id, teacherId);
    }

    @DeleteMapping("/curricular-units/{id}/assistant-teachers/{teacherId}")
    public CurricularUnitDto removeAssistantTeacher(
            @PathVariable long id, 
            @PathVariable long teacherId) {
        return curricularUnitService.removeAssistantTeacher(id, teacherId);
    }

    // Student management endpoints
    @PostMapping("/curricular-units/{id}/students/{studentId}")
    public CurricularUnitDto addStudent(
            @PathVariable long id, 
            @PathVariable long studentId) {
        return curricularUnitService.addStudent(id, studentId);
    }

    @DeleteMapping("/curricular-units/{id}/students/{studentId}")
    public CurricularUnitDto removeStudent(
            @PathVariable long id, 
            @PathVariable long studentId) {
        return curricularUnitService.removeStudent(id, studentId);
    }

    // Permission checking endpoints
    @GetMapping("/curricular-units/{id}/can-manage")
    public boolean canManageCurricularUnit(
            @PathVariable long id, 
            @RequestParam long personId) {
        return curricularUnitService.canManageCurricularUnit(id, personId);
    }

    @GetMapping("/curricular-units/{id}/can-assess")
    public boolean canAssessInCurricularUnit(
            @PathVariable long id, 
            @RequestParam long personId) {
        return curricularUnitService.canAssessInCurricularUnit(id, personId);
    }
}