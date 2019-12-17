package com.jzy.web.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.jzy.manager.constant.Constants;
import com.jzy.manager.exception.InvalidParameterException;
import com.jzy.manager.util.MissLessonStudentUtils;
import com.jzy.model.dto.MissLessonStudentDetailedDto;
import com.jzy.model.dto.MissLessonStudentSearchCondition;
import com.jzy.model.dto.MyPage;
import com.jzy.model.entity.User;
import com.jzy.model.vo.ResultMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MissLessonStudentController
 * @Author JinZhiyun
 * @Description 补课学生业务控制器
 * @Date 2019/11/21 22:07
 * @Version 1.0
 **/
@Controller
@RequestMapping("/missLessonStudent/admin")
public class MissLessonStudentAdminController extends AbstractController {
    private final static Logger logger = LogManager.getLogger(MissLessonStudentAdminController.class);

    /**
     * 跳转学员上课信息管理页面
     *
     * @return
     */
    @RequestMapping("/page")
    public String page(Model model) {
        return "student/missLesson/admin/page";
    }

    /**
     * 查询补课学员信息的ajax交互
     *
     * @param myPage    分页{页号，每页数量}
     * @param condition 查询条件入参
     * @return
     */
    @RequestMapping("/getMissLessonStudentInfo")
    @ResponseBody
    public ResultMap<List<MissLessonStudentDetailedDto>> getMissLessonStudentInfo(MyPage myPage, MissLessonStudentSearchCondition condition) {
        condition.setStudentId(condition.getStudentId() == null ? null : condition.getStudentId().toUpperCase());
        condition.setOriginalClassId(condition.getOriginalClassId() == null ? null : condition.getOriginalClassId().toUpperCase());
        condition.setCurrentClassId(condition.getCurrentClassId() == null ? null : condition.getCurrentClassId().toUpperCase());
        PageInfo<MissLessonStudentDetailedDto> pageInfo = missLessonStudentService.listMissLessonStudents(myPage, condition);
        return new ResultMap<>(0, "", (int) pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 查询我班上的要补课学员的信息的ajax交互
     *
     * @param myPage    分页{页号，每页数量}
     * @param condition 查询条件入参
     * @return
     */
    @RequestMapping("/getMissLessonStudentInfoFromMyClass")
    @ResponseBody
    public ResultMap<List<MissLessonStudentDetailedDto>> getMissLessonStudentInfoFromMyClass(MyPage myPage, MissLessonStudentSearchCondition condition) {
        User user = userService.getSessionUserInfo();
        condition.setOriginalAssistantWorkId(user.getUserWorkId());
        PageInfo<MissLessonStudentDetailedDto> pageInfo = missLessonStudentService.listMissLessonStudents(myPage, condition);
        return new ResultMap<>(0, "", (int) pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 查询补课到我班上的学员的信息的ajax交互
     *
     * @param myPage    分页{页号，每页数量}
     * @param condition 查询条件入参
     * @return
     */
    @RequestMapping("/getMissLessonStudentInfoToMyClass")
    @ResponseBody
    public ResultMap<List<MissLessonStudentDetailedDto>> getMissLessonStudentInfoToMyClass(MyPage myPage, MissLessonStudentSearchCondition condition) {
        User user = userService.getSessionUserInfo();
        condition.setCurrentAssistantWorkId(user.getUserWorkId());
        PageInfo<MissLessonStudentDetailedDto> pageInfo = missLessonStudentService.listMissLessonStudents(myPage, condition);
        return new ResultMap<>(0, "", (int) pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 重定向到编辑补课学生iframe子页面并返回相应model
     *
     * @param model
     * @param missLessonStudentDetailedDto 当前要被编辑的补课学生信息
     * @return
     */
    @RequestMapping("/updateForm")
    public String updateForm(Model model, MissLessonStudentDetailedDto missLessonStudentDetailedDto) {
//        model.addAttribute(ModelConstants.MISS_LESSON_STUDENT_EDIT_MODEL_KEY, missLessonStudentDetailedDto);
        return "student/missLesson/admin/missLessonStudentFormEdit";
    }

    /**
     * 重定向到编辑补课学生iframe子页面并返回相应model
     *
     * @param model
     * @return
     */
    @RequestMapping("/insertForm")
    public String insertForm(Model model) {
        return "student/missLesson/admin/missLessonStudentFormAdd";
    }

    /**
     * 补课学生管理中的编辑补课学生信息请求，由id修改
     *
     * @param missLessonStudentDetailedDto 修改后的补课学生信息
     * @return
     */
    @RequestMapping("/updateById")
    @ResponseBody
    public Map<String, Object> updateById(MissLessonStudentDetailedDto missLessonStudentDetailedDto) throws InvalidParameterException {
        Map<String, Object> map = new HashMap<>(1);


        if (!MissLessonStudentUtils.isValidMissLessonStudentUpdateInfo(missLessonStudentDetailedDto)) {
            String msg = "updateById方法错误入参";
            logger.error(msg);
            throw new InvalidParameterException(msg);
        }

        map.put("data", missLessonStudentService.updateMissLessonStudentInfo(missLessonStudentDetailedDto));
        return map;
    }

    /**
     * 补课学生管理中的添加补课学生请求
     *
     * @param missLessonStudentDetailedDto 新添加补课学生
     * @return
     */
    @RequestMapping("/insert")
    @ResponseBody
    public Map<String, Object> insert(MissLessonStudentDetailedDto missLessonStudentDetailedDto) throws InvalidParameterException {
        Map<String, Object> map = new HashMap<>(1);

        if (!MissLessonStudentUtils.isValidMissLessonStudentUpdateInfo(missLessonStudentDetailedDto)) {
            String msg = "updateById方法错误入参";
            logger.error(msg);
            throw new InvalidParameterException(msg);
        }

        map.put("data", missLessonStudentService.insertMissLessonStudent(missLessonStudentDetailedDto));
        return map;
    }

    /**
     * 删除一个补课学生ajax交互
     *
     * @param id 被删除补课学生的id
     * @return
     */
    @RequestMapping("/deleteOne")
    @ResponseBody
    public Map<String, Object> deleteOne(@RequestParam("id") Long id) {
        Map<String, Object> map = new HashMap(1);

        missLessonStudentService.deleteOneMissLessonStudentById(id);
        map.put("data", Constants.SUCCESS);
        return map;
    }

    /**
     * 删除多个补课学生记录ajax交互
     *
     * @param missLessonStudents 多个补课学生的json串，用fastjson转换为list
     * @return
     */
    @RequestMapping("/deleteMany")
    @ResponseBody
    public Map<String, Object> deleteMany(@RequestParam("missLessonStudents") String missLessonStudents) {
        Map<String, Object> map = new HashMap(1);

        List<MissLessonStudentDetailedDto> missLessonStudentsParsed = JSON.parseArray(missLessonStudents, MissLessonStudentDetailedDto.class);
        List<Long> ids = new ArrayList<>();
        for (MissLessonStudentDetailedDto dto : missLessonStudentsParsed) {
            ids.add(dto.getId());
        }
        missLessonStudentService.deleteManyMissLessonStudentsByIds(ids);
        map.put("data", Constants.SUCCESS);
        return map;
    }

    /**
     * 条件删除多个学生上课记录ajax交互
     *
     * @param condition 输入的查询条件
     * @return
     */
    @RequestMapping("/deleteByCondition")
    @ResponseBody
    public Map<String, Object> deleteByCondition(MissLessonStudentSearchCondition condition) {
        Map<String, Object> map = new HashMap(1);
        map.put("data", missLessonStudentService.deleteMissLessonStudentsByCondition(condition));
        return map;
    }
}
