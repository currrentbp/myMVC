package controller;


import com.currentbp.annotation.MyAutoWire;
import com.currentbp.annotation.MyController;
import com.currentbp.annotation.MyRequestBody;
import com.currentbp.annotation.MyRequestMapping;
import service.StudentService;

/**
 * @author baopan
 * @createTime 20181210
 */
@MyController(value="testController")
@MyRequestMapping("/t")
public class TestController {
    @MyAutoWire
    private StudentService studentService;

    @MyRequestMapping("/m")
    public void queryStudent(@MyRequestBody String s1,@MyRequestBody String s2){
        studentService.queryById(10);
    }

    @MyRequestMapping("m1")
    public void queryStudent1(){
        studentService.queryById(10);
    }
}
