package service;

import entity.Student;

/**
 * @author baopan
 * @createTime 20181213
 */
public interface StudentService {
    void queryById(Integer id);
    int createStudent(Student student);
}
