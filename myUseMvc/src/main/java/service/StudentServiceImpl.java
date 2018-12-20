package service;

import com.currentbp.annotation.MyBean;
import entity.Student;

/**
 * @author baopan
 * @createTime 20181213
 */
@MyBean("studentService")
public class StudentServiceImpl implements StudentService {

    @Override
    public void queryById(Integer id) {
        System.out.println("++++++++++++++++++++++++++" + id);
    }

    @Override
    public int createStudent(Student student) {
        System.out.println(student.toString());
        return 12;
    }
}
