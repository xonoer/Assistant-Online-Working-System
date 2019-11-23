package com.jzy.service.impl;

import com.jzy.dao.ClassMapper;
import com.jzy.manager.constant.Constants;
import com.jzy.model.dto.ClassDetailedDto;
import com.jzy.model.entity.Class;
import com.jzy.service.ClassService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * @author JinZhiyun
 * @version 1.0
 * @ClassName ClassServiceImpl
 * @description 班级业务实现
 * @date 2019/11/14 23:30
 **/
@Service
public class ClassServiceImpl extends AbstractServiceImpl implements ClassService {
    private final static Logger logger = Logger.getLogger(ClassServiceImpl.class);

    @Autowired
    private ClassMapper classMapper;

    @Override
    public Class getClassById(Long id) {
        return id == null ? null :classMapper.getClassById(id);
    }

    @Override
    public Class getClassByClassId(String classId) {
        return StringUtils.isEmpty(classId) ? null :classMapper.getClassByClassId(classId);
    }

    @Override
    public String updateClassByClassId(ClassDetailedDto classDetailedDto) {
        classMapper.updateClassByClassId(classDetailedDto);
        return Constants.SUCCESS;
    }

    @Override
    public String insertClass(ClassDetailedDto classDetailedDto) {
        //新班号不为空
        if (getClassByClassId(classDetailedDto.getClassId()) != null) {
            //添加的班号已存在
            return "classIdRepeat";
        }

        classMapper.insertClass(classDetailedDto);
        return Constants.SUCCESS;
    }

    @Override
    public String insertAndUpdateClassesFromExcel(List<ClassDetailedDto> classDetailedDtos) throws Exception {
        for (ClassDetailedDto classDetailedDto : classDetailedDtos) {
            insertAndUpdateOneClassFromExcel(classDetailedDto);
        }
        return Constants.SUCCESS;
    }

    @Override
    public String insertAndUpdateOneClassFromExcel(ClassDetailedDto classDetailedDto) throws Exception {
        if (classDetailedDto == null) {
            String msg = "insertAndUpdateOneClassFromExcel方法输入classDetailedDto为null!";
            logger.error(msg);
            throw new InvalidParameterException(msg);
        }

        Class originalClasses=getClassByClassId(classDetailedDto.getClassId());
        if (originalClasses != null) {
            //班号已存在，更新
            updateClassByClassId(classDetailedDto);
        } else {
            //插入
            insertClass(classDetailedDto);
        }

        return Constants.SUCCESS;
    }
}