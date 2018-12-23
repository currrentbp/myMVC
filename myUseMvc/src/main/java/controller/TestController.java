package controller;


import com.springmvc.currentbp.annotation.*;
import entity.Student;
import service.StudentService;

/**
 * @author baopan
 * @createTime 20181210
 */
@MyController(value = "testController")
@MyRequestMapping("/t")
public class TestController {
    @MyAutoWire
    private StudentService studentService;

    @MyRequestMapping("/m")
    public void queryStudent(@MyRequestBody String s1, @MyRequestBody String s2) {
        System.out.println("s1:" + s1 + " s2:" + s2);
        studentService.queryById(10);
    }

    @MyRequestMapping("m1")
    public void queryStudent1() {
        studentService.queryById(10);
    }

    @MyRequestMapping("m2")
    @MyResponseBody
    public Student create(@MyRequestBody Student student) {
        int id = studentService.createStudent(student);
        student.setId(id);
        return student;
    }
}
