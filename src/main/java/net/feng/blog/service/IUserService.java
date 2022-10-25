package net.feng.blog.service;

import net.feng.blog.pojo.User;
import net.feng.blog.response.ResponseResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IUserService {
    ResponseResult initAdminAccount(User user, HttpServletRequest request);

    void createCaptcha(HttpServletResponse response, String captchaKey) throws Exception;

    ResponseResult sendEmail(String type, HttpServletRequest request, String emailAddress);

    ResponseResult register(User user, String emailCode, String captchaCode, String captchaKey, HttpServletRequest request);

    ResponseResult doLogin(String captcha, String captchaKey, String from, User user);

    ResponseResult getUserInfo(String userId);

    ResponseResult checkEmail(String email);

    ResponseResult checkUserName(String userName);

    ResponseResult updateUserInfo(String userId, User user);

    User checkLoginUser();

    ResponseResult deleteUserById(String userId);

    ResponseResult listUsers(int page, int size);

    ResponseResult updateUserPassword(String verifyCode, User user);

    ResponseResult updateEmail(String email, String verifyCode);

    ResponseResult doLogout();

    ResponseResult getPcLoginQrCodeInfo();

    ResponseResult updateQrCodeLoginState(String loginId);

    ResponseResult checkQrCodeLoginState(String loginId);

    ResponseResult parseToken();

    ResponseResult resetPassword(String userId, String password);

    ResponseResult getRegisterCount();

    ResponseResult checkEmailCode(String email, String emailCode, String captchaCode);
}

