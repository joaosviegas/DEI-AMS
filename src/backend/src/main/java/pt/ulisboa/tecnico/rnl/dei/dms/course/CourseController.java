package pt.ulisboa.tecnico.rnl.dei.dms.course;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import pt.ulisboa.tecnico.rnl.dei.dms.course.dto.CourseDto;
import pt.ulisboa.tecnico.rnl.dei.dms.course.service.CourseService;

@RestController
public class CourseController {
    @Autowired
    private CourseService courseService;

    @GetMapping("/courses")
    public List<CourseDto> getCourses() {
        return courseService.getCourses();
    }

    @PostMapping("/courses")
    public CourseDto createCourse(@RequestBody CourseDto courseDto) {
        return courseService.createCourse(courseDto);
    }

    @GetMapping("/courses/{id}")
    public CourseDto getCourse(@PathVariable long id) {
        return courseService.getCourse(id);
    }

    @PutMapping("/courses/{id}")
    public CourseDto updateCourse(@PathVariable long id, @RequestBody CourseDto courseDto) {
        return courseService.updateCourse(id, courseDto);
    }

    @DeleteMapping("/courses/{id}")
    public void deleteCourse(@PathVariable long id) {
        courseService.deleteCourse(id);
    }
}