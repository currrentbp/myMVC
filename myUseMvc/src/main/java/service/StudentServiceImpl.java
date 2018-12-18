package service;

import com.currentbp.annotation.MyBean;

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
}
