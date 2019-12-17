package com.jzy.web.controller;

import com.alibaba.fastjson.JSON;
import com.jzy.manager.constant.Constants;
import com.jzy.manager.constant.ModelConstants;
import com.jzy.manager.util.MyStringUtils;
import com.jzy.manager.util.SendEmailUtils;
import com.jzy.model.vo.ProblemCollection;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author JinZhiyun
 * @version 1.0
 * @ClassName HomeController
 * @description 主页的一些页面的跳转
 * @date 2019/11/19 12:57
 **/
@Controller
public class HomeController extends AbstractController {
    private final static Logger logger = LogManager.getLogger(HomeController.class);

    /**
     * 跳转控制台
     *
     * @param model
     * @return
     */
    @RequestMapping("/console")
    public String console(Model model) {
        List<String> belongsTo = usefulInformationService.listAllBelongTo();
        model.addAttribute(ModelConstants.USEFUL_INFORMATION_BELONG_TO_MODEL_KEY, JSON.toJSONString(belongsTo));

        return "home/console";
    }


//    /**
//     * 问题收集的提交问题
//     *
//     * @return
//     */
//    @RequestMapping("/getSystemLoad")
//    @ResponseBody
//    public Map<String, Object> getSystemLoad() {
//        Map<String, Object> map = new HashMap<>(1);
//
//        //系统监控
//        double cpuUsage = 0;
//        double memUsage = 0;
//        double diskUsage = 0;
//        try {
//            //当前系统的cpu使用率
//           cpuUsage = SystemUtils.getCpuUsage();
//            //当前系统的内存使用率
//            memUsage = SystemUtils.getMemUsage();
//            //当前系统的硬盘使用率
//            diskUsage = SystemUtils.getDiskUsage();
//
//            System.out.println(diskUsage+"///////");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        map.put("data", new SystemInformation(cpuUsage, memUsage, diskUsage));
//        return map;
//    }

    /**
     * 跳转问题收集页面
     *
     * @return
     */
    @RequestMapping("/problemCollection")
    public String problemCollection() {
        return "home/problemCollection";
    }


    /**
     * 问题收集的提交问题
     *
     * @param problemCollection 前台表单参数封装
     * @return
     */
    @RequestMapping("/sendProblem")
    @ResponseBody
    public Map<String, Object> sendProblem(ProblemCollection problemCollection) {
        Map<String, Object> map = new HashMap<>(1);

        if (Constants.ON.equals(problemCollection.getHide())) {
            problemCollection.setRealName("匿名");
        }


        if (!StringUtils.isEmpty(problemCollection.getContent())) {
            String msgToSend = problemCollection.getContent() + "\n\n姓名: " + problemCollection.getRealName() + "\n邮箱: " + problemCollection.getEmail();

            SendEmailUtils.sendConcurrentEncryptedEmail(SendEmailUtils.FROM, problemCollection.getTitle(), msgToSend);

            //回访邮件
            if (MyStringUtils.isEmail(problemCollection.getEmail())) {
                String msgToSendBack = "感谢您的问题反馈，以及长期以来对AWOS-优能助教在线工作平台的支持!酷乐会尽快处理您的问题的~";
                SendEmailUtils.sendConcurrentEncryptedEmail(problemCollection.getEmail(), "AOWS-回访", msgToSendBack);
            }
        } else {
            String msg = "sendProblem方法错误入参";
            logger.error(msg);
            map.put("data", Constants.FAILURE);
            return map;
        }

        map.put("data", Constants.SUCCESS);
        return map;
    }
}
