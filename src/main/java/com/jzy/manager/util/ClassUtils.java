package com.jzy.manager.util;

import com.jzy.manager.constant.Constants;
import com.jzy.model.dto.ClassDetailedDto;
import com.jzy.model.dto.StudentAndClassDetailedDto;
import com.jzy.model.entity.Class;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @ClassName ClassUtils
 * @Author JinZhiyun
 * @Description 班级的工具类 {@link com.jzy.model.entity.Class}
 * 对班级的增删改入参对象字段的校验，服务端的校验应该与前端js保持一致，且必须严格于数据库列属性要求的标准
 * @Date 2019/11/23 20:31
 * @Version 1.0
 **/
public class ClassUtils {
    private ClassUtils() {
    }

    public static final List<String> SEASONS = Constants.SEASONS;

    public static final List<String> GRADES = Constants.GRADES;

    public static final List<String> SUBJECTS = Constants.SUBJECTS;

    public static final List<String> TYPES = Constants.TYPES;


    public static boolean isValidClassId(String classId) {
        return !StringUtils.isEmpty(classId) && classId.length() <= 32;
    }

    public static boolean isValidClassName(String className) {
        return StringUtils.isEmpty(className) || className.length() <= 50;
    }

    public static boolean isValidClassCampus(String classCampus) {
        return StringUtils.isEmpty(classCampus) || classCampus.length() <= 50;
    }

    public static boolean isValidClassGrade(String classGrade) {
        return StringUtils.isEmpty(classGrade) || classGrade.length() <= 50;
    }

    public static boolean isValidClassSubject(String classSubject) {
        return StringUtils.isEmpty(classSubject) || classSubject.length() <= 50;
    }

    public static boolean isValidClassType(String classType) {
        return StringUtils.isEmpty(classType) || classType.length() <= 20;
    }

    public static boolean isValidClassYear(String classYear) {
        return StringUtils.isEmpty(classYear) || classYear.length() == 4;
    }

    //    public static boolean isValidClassYear(Date classYear) {
//        return true;
//    }
    public static boolean isValidClassSeason(String classSeason) {
        return StringUtils.isEmpty(classSeason) || classSeason.length() <= 50;
    }

    public static boolean isValidClassTime(String classTime) {
        return StringUtils.isEmpty(classTime) || classTime.length() <= 50;
    }

    public static boolean isValidClassSimplifiedTime(String classSimplifiedTime) {
        return StringUtils.isEmpty(classSimplifiedTime) || classSimplifiedTime.length() <= 50;
    }

    public static boolean isValidClassTimes(Integer classTimes) {
        return true;
    }

    public static boolean isValidClassTeacherId(Long classTeacherId) {
        return true;
    }

    public static boolean isValidClassAssistantId(Long classAssistantId) {
        return true;
    }

    public static boolean isValidClassroom(String classroom) {
        return StringUtils.isEmpty(classroom) || classroom.length() <= 10;
    }

    public static boolean isValidClassTeacherRequirement(String classTeacherRequirement) {
        return StringUtils.isEmpty(classTeacherRequirement) || classTeacherRequirement.length() <= 200;
    }

    public static boolean isValidClassRemark(String classRemark) {
        return StringUtils.isEmpty(classRemark) || classRemark.length() <= 500;
    }

    /**
     * 输入的clazz是否合法
     *
     * @param clazz 输入的Class对象
     * @return
     */
    public static boolean isValidClassInfo(Class clazz) {
        return clazz != null && isValidClassId(clazz.getClassId()) && isValidClassName(clazz.getClassName())
                && isValidClassCampus(clazz.getClassCampus()) && isValidClassGrade(clazz.getClassGrade())
                && isValidClassSubject(clazz.getClassSubject()) && isValidClassType(clazz.getClassType())
                && isValidClassYear(clazz.getClassYear()) && isValidClassSeason(clazz.getClassSeason())
                && isValidClassTime(clazz.getClassTime()) && isValidClassSimplifiedTime(clazz.getClassSimplifiedTime())
                && isValidClassTimes(clazz.getClassTimes()) && isValidClassTeacherId(clazz.getClassTeacherId())
                && isValidClassAssistantId(clazz.getClassAssistantId()) && isValidClassroom(clazz.getClassroom())
                && isValidClassTeacherRequirement(clazz.getClassTeacherRequirement()) && isValidClassRemark(clazz.getClassRemark());
    }

    /**
     * 输入的classDetailedDto是否合法
     *
     * @param classDetailedDto 输入的ClassDetailedDto对象
     * @return
     */
    public static boolean isValidClassDetailedDtoInfo(ClassDetailedDto classDetailedDto) {
        return isValidClassInfo(classDetailedDto)
                && (StringUtils.isEmpty(classDetailedDto.getTeacherName()) || TeacherUtils.isValidTeacherName(classDetailedDto.getTeacherName()))
                && (StringUtils.isEmpty(classDetailedDto.getAssistantName()) || AssistantUtils.isValidAssistantName(classDetailedDto.getAssistantName()));
    }

    /**
     * 输入的classDetailedDto是否合法
     *
     * @param classDetailedDto 输入的ClassDetailedDto对象
     * @return
     */
    public static boolean isValidClassUpdateInfo(ClassDetailedDto classDetailedDto) {
        return isValidClassDetailedDtoInfo(classDetailedDto);
    }

    /**
     * 根据当前月份日期智能判断当前季度
     *
     * @return
     */
    public static String getCurrentSeason() {
        int month = MyTimeUtils.getCurrentMonth();
        int day = MyTimeUtils.getCurrentDay();
        //6月20日~8月31日，设定为暑假
        if ((month == 6 && day >= 20) || month == 7 || month == 8) {
            return SEASONS.get(0);
        }

        //9月1号~10月31号，设为秋上
        if (month >= 9 && month <= 10) {
            return SEASONS.get(1);
        }

        //11月1号~12月31号，设为秋下
        if (month >= 11 && month <= 12) {
            return SEASONS.get(2);
        }

        //1月1号~2月10号，设为寒假
        if (month == 1 || (month == 2 && day <= 10)) {
            return SEASONS.get(3);
        }

        //2月11号~6月20号，设为春季
        if ((month == 2 && day > 10) || (month >= 3 && month <= 5) || (month == 6 && day < 20)) {
            return SEASONS.get(4);
        }

        return "";
    }

    /**
     * 获取当前年份
     *
     * @return
     */
    public static String getCurrentYear() {
       return MyTimeUtils.getCurrentYear()+"";
    }

    /**
     * 将输入对象的年份字段由yyyy-mm-dd转成yyyy
     *
     * @param input
     * @return
     */
    public static ClassDetailedDto parseClassYear(ClassDetailedDto input){
        String year=input.getClassYear();
        String parsedYear=year.substring(0, year.indexOf('-'));
        input.setClassYear(parsedYear);
        return input;
    }
}