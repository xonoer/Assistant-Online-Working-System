package com.jzy.service.impl;

import com.jzy.dao.StudentMapper;
import com.jzy.manager.constant.Constants;
import com.jzy.model.entity.Student;
import com.jzy.service.StudentService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * @author JinZhiyun
 * @version 1.0
 * @ClassName StudentServiceImpl
 * @description 学生业务
 * @date 2019/11/14 23:31
 **/
@Service
public class StudentServiceImpl extends AbstractServiceImpl implements StudentService {
    private final static Logger logger = Logger.getLogger(StudentServiceImpl.class);

    @Autowired
    private StudentMapper studentMapper;

    @Override
    public Student getStudentById(Long id) {
        return id == null ? null : studentMapper.getStudentById(id);
    }

    @Override
    public Student getStudentByStudentId(String studentId) {
        return StringUtils.isEmpty(studentId) ? null :studentMapper.getStudentByStudentId(studentId);
    }

    @Override
    public String updateStudentByStudentId(Student student) {
        if (StringUtils.isEmpty(student.getStudentSex())){
            student.setStudentSex(null);
        }
        studentMapper.updateStudentByStudentId(student);
        return Constants.SUCCESS;
    }

    @Override
    public String insertStudent(Student student) {
        if (getStudentByStudentId(student.getStudentId()) != null) {
            //添加的工号已存在
            return "studentIdRepeat";
        }

        studentMapper.insertStudent(student);
        return Constants.SUCCESS;
    }

    @Override
    public String insertAndUpdateStudentsFromExcel(List<Student> students) throws Exception {
        for (Student student:students){
            insertAndUpdateOneStudentFromExcel(student);
        }
        return Constants.SUCCESS;
    }

    @Override
    public String insertAndUpdateOneStudentFromExcel(Student student) throws Exception {
        if (student == null) {
            String msg = "insertAndUpdateOneStudentFromExcel方法输入学生student为null!";
            logger.error(msg);
            throw new InvalidParameterException(msg);
        }


        Student originalStudent=getStudentByStudentId(student.getStudentId());
        if (originalStudent != null) {
            //学员编号已存在，更新
            updateStudentByStudentId(student);
        } else {
            insertStudent(student);
        }
        return Constants.SUCCESS;
    }

}