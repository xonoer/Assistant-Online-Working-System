package com.jzy.service;

import com.jzy.model.dto.ClassDetailedDto;
import com.jzy.model.entity.Class;

import java.util.List;

/**
 * @author JinZhiyun
 * @version 1.0
 * @IntefaceName ClassService
 * @description 班级业务
 * @date 2019/11/14 23:30
 **/
public interface ClassService {
    /**
     * 根据班级id查询班级
     *
     * @param id 班级id
     * @return
     */
    Class getClassById(Long id);

    /**
     * 根据班级编码查询班级,，注意这里classId不是主键id
     *
     * @param classId 班级编码
     * @return
     */
    Class getClassByClassId(String classId);

    /**
     * 修改班级信息由班级编码修改，注意这里classId不是主键id
     *
     * @param classDetailedDto 修改后的班级信息
     * @return
     */
    String updateClassByClassId(ClassDetailedDto classDetailedDto);

    /**
     * 添加班级
     *
     * @param classDetailedDto 新添加班级的信息
     * @return
     */
    String insertClass(ClassDetailedDto classDetailedDto);

    /**
     * 根据从excel中读取到的classDetailedDtos信息，更新插入多个。根据班号判断：
     *      if 当前班号不存在
     *          执行插入
     *      else
     *          根据班号更新
     * @param classDetailedDtos
     * @return
     */
    String insertAndUpdateClassesFromExcel(List<ClassDetailedDto> classDetailedDtos) throws Exception;

    /**
     * 根据从excel中读取到的classDetailedDtos信息，更新插入一个。根据班号判断：
     *      if 当前班号不存在
     *          执行插入
     *      else
     *          根据班号更新
     * @param classDetailedDto
     * @return
     */
    String insertAndUpdateOneClassFromExcel(ClassDetailedDto classDetailedDto) throws Exception;
}