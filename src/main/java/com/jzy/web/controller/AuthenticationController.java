package com.jzy.web.controller;

import com.jzy.manager.constant.Constants;
import com.jzy.manager.util.*;
import com.jzy.manager.constant.SessionConstants;
import com.jzy.model.dto.*;
import com.jzy.model.entity.User;
import com.jzy.model.vo.EmailVerifyCodeSession;
import com.jzy.model.vo.UserLoginInput;
import com.jzy.model.vo.UserLoginResult;
import com.jzy.web.interceptor.Token;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author JinZhiyun
 * @version 1.0
 * @ClassName AuthenticationController
 * @description 用户登录登出，验证码，错误页面导向等。
 * 这些请求应该在shiro中配置为匿名访问
 * @date 2019/11/14 22:04
 **/
@Controller
public class AuthenticationController extends AbstractController {
    private final static Logger logger = Logger.getLogger(AbstractController.class);

    @RequestMapping("/400")
    public String error400() {
        return "tips/HTTP-400";
    }

    @RequestMapping("/404")
    public String error404() {
        return "tips/HTTP-404";
    }

    @RequestMapping("/500")
    public String error500() {
        return "tips/HTTP-500";
    }

    @RequestMapping("/formRepeatSubmit")
    public String formRepeatSubmit() {
        return "tips/formRepeatSubmit";
    }

    @RequestMapping("/noPermissions")
    public String noPermissions() {
        return "tips/noPermissions";
    }

    @RequestMapping("/")
    public String login0() {
        return "login";
    }

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/logout")
    public String logout() {
        return "login";
    }

    @RequestMapping("/forget")
    @Token(save = true)
    public String forget() {
        return "forget";
    }

    @RequestMapping("/loginByEmailCode")
    public String loginByEmailCode() {
        return "loginByEmailCode";
    }

    /**
     * 跳转用问题登录页面，后台产生随机数（问题id），将id存在当前session，问题内容通过model返回前台
     *
     * @param model
     * @return
     */
    @RequestMapping("/guestLogin")
    public String guestLogin(Model model) {
        Integer qId=CodeUtils.oneRandomCode(1,QuestionUtils.QUESTION_COUNT);
        //把问题id设到session
        ShiroUtils.getSession().setAttribute(SessionConstants.LOGIN_QUESTION_ID_SESSION_KEY, qId);
        model.addAttribute("question", QuestionUtils.getQuestionById(qId));
        return "loginByQuestion";
    }


    @RequestMapping("/index")
    public String index(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("++++"+ShiroUtils.getSession().getAttribute(SessionConstants.USER_INFO_SESSION_KEY));
        if (SecurityUtils.getSubject().isAuthenticated() || SecurityUtils.getSubject().isRemembered()){
            //登录成功后设置CsrfToken
            CookieUtils.setCSRFTokenCookieAndSession(request, response);
        }
        return "index";
    }

    /**
     * 登录交互
     *
     * @param input 登录请求的入参封装--用户名&密码&图形验证码&是否记住密码
     * @return
     */
    @RequestMapping("/loginTest")
    @ResponseBody
    public Map<String, Object> loginTest(UserLoginInput input) {
        Map<String, Object> map = new HashMap<>(1);
        UserLoginResult result = new UserLoginResult();

        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        //图形验证码判断
        String trueImgCode = (String) session.getAttribute(SessionConstants.KAPTCHA_SESSION_KEY);
        if (!CodeUtils.equals(input.getImgCode(), trueImgCode)) {
            result.setImgCodeWrong(true);
            map.put("data", result);
            return map;
        }

        //redis用户登录错误次数缓存的键
        String key = UserLoginResult.getUserLoginFailKey(input.getUserName());

        UsernamePasswordToken token = new UsernamePasswordToken(input.getUserName(), input.getUserPassword());
        token.setRememberMe(input.getRememberMe() != null);
        try {
            subject.login(token);
            User userSessionInfo = (User) subject.getPrincipal();
            //登录成功，设置用户信息到session
            session.setAttribute(SessionConstants.USER_INFO_SESSION_KEY, userSessionInfo);

            //rememberMe功能交给shrio

            //返回json中设置标志success为true
            result.setSuccess(true);
            result.setUser(userSessionInfo);
            if (redisTemplate.hasKey(key)) { //如果有登录失败记录
                redisTemplate.expire(key,0,TimeUnit.MINUTES); //登录成功，让当前登录失败次数的缓存过期
            }
        } catch (UnknownAccountException e) {
            //用户名不存在
            result.setUnknownAccount(true);
            map.put("data", result);
        } catch (IncorrectCredentialsException e) {
            //账号存在，但密码错误

            //密码输错次数的redis缓存设置与查询
            result.setSuccess(false); //success标志默认false，可以不写
            if (!redisTemplate.hasKey(key)) {
                valueOps.set(key, "1"); //如果当前用户名没有登录失败次数的缓存，设为第一次登录失败
            } else {
                /*
                 * 登录失败次数加一，这里没有使用increment方法
                 * 因为redisTemplate配置中value序列化使用了GenericJackson2JsonRedisSerializer，这会导致该方法报String转换错误
                 */
                int wrongTimes = Integer.parseInt((String) valueOps.get(key));
                valueOps.set(key, wrongTimes + 1 + "");
            }
            int wrongTimes = Integer.parseInt((String) valueOps.get(key));
            result.setWrongTimes(wrongTimes);
//            if (result.getWrongTimes() == UserLoginResult.DEFAULT_WRONG_TIMES) { //如果当前用户名连续五次输错密码

            redisTemplate.expire(key, UserLoginResult.DEFAULT_BASE_DELAY_TIME, TimeUnit.MINUTES);//设置当前用户登录错误的缓存有效期15分钟

            result.setPasswordWrong(true);
            map.put("data", result);
        } catch (LockedAccountException | ExcessiveAttemptsException e) {
            //账户被锁定
            result.setSuccess(false); //success标志默认false，可以不写
            result.setLocked(true);
            result.setWrongTimes(UserLoginResult.DEFAULT_WRONG_TIMES);
            result.setTimeRemaining(redisTemplate.getExpire(key, TimeUnit.MINUTES) + 1); //getExpire下取整，这里所以取+1
            map.put("data", result);
        } catch (AuthenticationException e) {
            //其他异常
            logger.error("未知的登录错误!");
            map.put("data", result);
        }

        map.put("data", result);
        return map;
    }

    /**
     * 发送验证码的ajax交互
     *
     * @param user
     * @return
     */
    @RequestMapping("/sendVerifyCodeToEmail")
    @ResponseBody
    public Map<String, Object> sendVerifyCodeToEmail(User user) {
        System.out.println("++++++++++++"+user);
        Map<String, Object> map = new HashMap(1);
        ShiroUtils.getSession().setAttribute(SessionConstants.USER_EMAIL_SESSION_KEY, new EmailVerifyCodeSession(user.getUserEmail(),false));
        try {
            userService.sendVerifyCodeToEmail(user.getUserEmail());
            map.put("msg", Constants.SUCCESS);
        } catch (Exception e) {
            logger.error("邮箱验证码发送失败!");
            map.put("msg", Constants.FAILURE);
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 检测验证码是否正确的ajax交互
     *
     * @param emailVerifyCode 输入的验证码
     * @param user 用户的邮箱信息，用user对象封装
     * @return
     */
    @RequestMapping("/emailVerifyCodeTest")
    @ResponseBody
    public Map<String, Object> emailVerifyCodeTest(@RequestParam(value = "emailVerifyCode",required = false) String emailVerifyCode, User user) {
        Map<String, Object> map = new HashMap(1);
        if (userService.getUserByEmail(user.getUserEmail()) == null) {
            map.put("data", "emailUnregistered");
        } else if (!userService.ifValidEmailVerifyCode(new EmailVerifyCode(user.getUserEmail(), emailVerifyCode))) {
            map.put("data", "verifyCodeWrong");
        } else {
            //auth=true，即已经经过了服务端验证
            ShiroUtils.getSession().setAttribute(SessionConstants.USER_EMAIL_SESSION_KEY, new EmailVerifyCodeSession(user.getUserEmail(),true));
            map.put("data", "verifyCodeCorrect");
        }
        return map;
    }

    /**
     * 重置密码的ajax交互
     *
     * @param user 入参用户信息
     * @return
     */
    @RequestMapping("/resetPassword")
    @Token(remove = true)
    @ResponseBody
    public Map<String, Object> resetPassword(User user) {
        Map<String, Object> map = new HashMap(1);
        if (!MyStringUtils.isPassword(user.getUserPassword())){
            logger.error("错误的用户密码入参!");
            throw new InvalidParameterException("错误的用户密码入参");
        }
        EmailVerifyCodeSession emailVerifyCodeSession= (EmailVerifyCodeSession) ShiroUtils.getSession().getAttribute(SessionConstants.USER_EMAIL_SESSION_KEY);
        userService.updatePasswordByEmail(emailVerifyCodeSession.getUserEmail(), user.getUserPassword());
        map.put("data", Constants.SUCCESS);
        return map;
    }


    /**
     * 检测验证码是否正确的ajax交互
     *
     * @param emailVerifyCode 输入的验证码
     * @param user 用户的邮箱信息，用user对象封装
     * @return
     */
    @RequestMapping("/loginTestByEmailCode")
    @ResponseBody
    public Map<String, Object> loginTestByEmailCode(@RequestParam(value = "emailVerifyCode",required = false) String emailVerifyCode, User user) {
        Map<String, Object> map = new HashMap(1);
        User userGetByEmail=userService.getUserByEmail(user.getUserEmail());
        if (userGetByEmail == null) {
            map.put("data", "emailUnregistered");
        } else if (!userService.ifValidEmailVerifyCode(new EmailVerifyCode(user.getUserEmail(), emailVerifyCode))) {
            map.put("data", "verifyCodeWrong");
        } else {
            //auth=true，即已经经过了服务端验证
            ShiroUtils.getSession().setAttribute(SessionConstants.USER_EMAIL_SESSION_KEY, new EmailVerifyCodeSession(user.getUserEmail(),true));

            //通过验证，用固定的明文密文组实现免密登录
            Subject subject = SecurityUtils.getSubject();
            Session session=subject.getSession();
            session.setAttribute(SessionConstants.LOGIN_WITHOUT_PASSWORD_SESSION_KEY, "true");
            UsernamePasswordToken token = new UsernamePasswordToken(null, ShiroUtils.FINAL_PASSWORD_PLAINTEXT);
            try {
                subject.login(token);
                //登录成功，设置用户信息到session
                session.setAttribute(SessionConstants.USER_INFO_SESSION_KEY, userGetByEmail);
                map.put("data", "verifyCodeCorrect");
            } catch (AuthenticationException e) {
                //其他异常
                map.put("data", Constants.UNKNOWN_ERROR);
            }
            //移除免密登录成功标志
            session.removeAttribute(SessionConstants.LOGIN_WITHOUT_PASSWORD_SESSION_KEY);
        }
        return map;
    }

    /**
     * 通过问题登录页面换一个问题的ajax请求
     *
     * @return
     */
    @RequestMapping("/resetLoginQuestion")
    @ResponseBody
    public Map<String, Object> resetLoginQuestion() {
        Map<String, Object> map = new HashMap(1);
        Integer originQuestionId= (Integer) ShiroUtils.getSession().getAttribute(SessionConstants.LOGIN_QUESTION_ID_SESSION_KEY);
        Integer qId;
        do {
            //循环直到找到与当前问题不同的问题id
            qId=CodeUtils.oneRandomCode(1,QuestionUtils.QUESTION_COUNT);
        } while (originQuestionId.equals(qId));

        //把问题id设到session
        ShiroUtils.getSession().setAttribute(SessionConstants.LOGIN_QUESTION_ID_SESSION_KEY, qId);

        map.put("question", QuestionUtils.getQuestionById(qId));
        return map;
    }

    @RequestMapping("/loginTestByQuestion")
    @ResponseBody
    public Map<String, Object> loginTestByQuestion(@RequestParam(value = "answer",required = false) String answer) {
        Map<String, Object> map = new HashMap(1);

        Integer qId= (Integer) ShiroUtils.getSession().getAttribute(SessionConstants.LOGIN_QUESTION_ID_SESSION_KEY);

        if (!QuestionUtils.isValidAnswer(qId, answer)) {
            //问题回答错误
            map.put("data", "answerWrong");
        } else {
            //通过验证，用固定的明文密文组实现免密登录
            Subject subject = SecurityUtils.getSubject();
            Session session=subject.getSession();
            session.setAttribute(SessionConstants.LOGIN_WITHOUT_PASSWORD_SESSION_KEY, "true");
            UsernamePasswordToken token = new UsernamePasswordToken(null, ShiroUtils.FINAL_PASSWORD_PLAINTEXT);
            try {
                subject.login(token);
                //登录成功，设置用户信息到session。注意这里的用户应该是游客！
                User guest=new User();
                guest.setUserWorkId(UUID.randomUUID().toString());
                guest.setUserName(UUID.randomUUID().toString());
                guest.setUserRealName(CodeUtils.sixRandomCodes());
                guest.setUserRole(UserUtils.USER_ROLES.get(5));
                guest.setUserIcon(UserUtils.USER_ICON_DEFAULT);
                session.setAttribute(SessionConstants.USER_INFO_SESSION_KEY, guest);
                System.out.println("++++"+ session.getAttribute(SessionConstants.USER_INFO_SESSION_KEY));
                map.put("data", "answerCorrect");
            } catch (AuthenticationException e) {
                //其他异常
                map.put("data", Constants.UNKNOWN_ERROR);
            }
            //移除免密登录成功标志
            session.removeAttribute(SessionConstants.LOGIN_WITHOUT_PASSWORD_SESSION_KEY);
            //移除问题id
            session.removeAttribute(SessionConstants.LOGIN_QUESTION_ID_SESSION_KEY);
        }
        return map;
    }

}