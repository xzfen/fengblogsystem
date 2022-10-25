package net.feng.blog.controller.user;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import net.feng.blog.pojo.User;
import net.feng.blog.response.ResponseResult;
import net.feng.blog.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Slf4j
@RestController
@RequestMapping("/user")
public class UserApi {

    @Autowired
    private IUserService userService;

    /*
    * 初始化管理员账号init-admin
    * */
    @PostMapping("/admin_account")
    public ResponseResult initAdminAccount(@RequestBody User user, HttpServletRequest request){
        log.info("user_name==>"+user.getUserName());
        log.info("password==>"+user.getPassword());
        log.info("email==>"+user.getEmail());
        return userService.initAdminAccount(user,request);
    }
    /*
    * 注册join-in
    * */
    @PostMapping("/join_in")
    public ResponseResult register(@RequestBody User user,
                                   @RequestParam("email_code") String emailCode,
                                   @RequestParam("captcha_code")String captchaCode,
                                   @RequestParam("captcha_key")String captchaKey,
                                   HttpServletRequest request){
        return userService.register(user,emailCode,captchaCode,captchaKey,request);
    }
    /*
    * 登录sign-up
    * 需要提交的信息
    * 1、用户账号-可以昵称、邮箱--->需要唯一处理
    * 2、密码
    * 3、图灵验证码
    * 4、图灵验证码key
    * */
    @PostMapping("/login/{captcha}/{captcha_key}")
    public ResponseResult login(@PathVariable("captcha") String captcha,
                                @PathVariable("captcha_key") String captchaKey,
                                @RequestParam(value = "from", required = false) String from,
                                @RequestBody User user){
        return userService.doLogin(captcha,captchaKey,from,user);
    }

    /*
    * 获取图灵验证码captcha
    * */
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletResponse response,@RequestParam("captcha_key") String captchaKey) {
        try {
            userService.createCaptcha(response,captchaKey);
        } catch (Exception e) {
            log.error(e.toString());
        }
    }
    /*
    * 发送邮件email
    * <p>
    * 使用场景：注册、找回密码、修改邮箱
    * <p>
    * 注册（register）：如果已经注册过了就提示说，该邮箱已经注册
    * 找回密码（forget）：如果没有注册过，提示该邮箱没有注册
    * 修改邮箱（update）：（新的邮箱）：如果已经注册了，提示该邮箱已经注册
    * */
    @GetMapping("/verify_code")
    public ResponseResult sendVerifyCode(@RequestParam("type") String type, HttpServletRequest request,
                                         @RequestParam("email") String emailAddress){
        log.info("emailAddress==> "+emailAddress);
        return userService.sendEmail(type,request,emailAddress);
    }
    /*
    * 修改密码password
    * 修改密码
    * 普通做法：通过旧密码对比来更新密码
    * <p>
    * 即可以找回密码，也可以修改密码
    * 发送验证码到邮箱/手机---> 判断验证码是否正确来判断
    * 对应邮箱/手机所注册的账号是否属于你
    * <p>
    * 步骤：
    * 1、用户填写邮箱
    * 2、用户获取验证码type=forget
    * 3、填写验证码
    * 4、填写新的密码
    * 5、提交数据
    * <p>
    * 数据包括：
    * 1、邮箱和新密码
    * 2、验证码
    * 如果验证码正确--->所用邮箱注册的账号就是你的，可以修改密码
    * */
    @PutMapping("/password/{verifyCode}")
    public ResponseResult updatePassword(@PathVariable("verifyCode") String verifyCode,@RequestBody User user){
        return userService.updateUserPassword(verifyCode,user);
    }
    /*
    * 获取用户信息user-info
    * */
    @GetMapping("/user_info/{userId}")
    public ResponseResult getUserInfo(@PathVariable("userId") String userId){
        return userService.getUserInfo(userId);
    }
    /*
    * 修改用户信息user-info
    * <p>
    * 允许用户修改的内容
    * 1. 头像
    * 2. 用户名（唯一）
    * 3. 签名
    * 4. 密码（单独修改）
    * 5. Email（唯一，单独修改）
    * */
    @PutMapping("/user_info/{userId}")
    public ResponseResult updateUserInfo(@PathVariable("userId")String userId, @RequestBody User user){
        return userService.updateUserInfo(userId,user);
    }

    /*
    * 获取用户列表
    * 需要管理员权限
    * */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/list")
    public ResponseResult listUsers(@RequestParam("page") int page,
                                    @RequestParam("size") int size){
        return userService.listUsers(page,size);
    }
    /*
    * 删除用户
    * 需要管理员权限
    * */
    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/{userId}")
    public ResponseResult deleteUser(@PathVariable("userId") String userId){
        //判断当前操作的用户是谁
        //根据用户角色判断是否可以删除
        //TODO:通过注解的方式来控制权限
        return userService.deleteUserById(userId);
    }

    /*
    * 检查该Email是否已经注册
    * @param email 邮箱地址
    * @return SUCCESS-->已经注册了，FAILED-->没有注册
    * */
    @ApiResponses({
            @ApiResponse(code = 20000,message = "表示当前邮箱已经注册"),
            @ApiResponse(code = 40000,message = "表示当前邮箱未注册")
    })
    @GetMapping("/email")
    public ResponseResult checkEmail(@RequestParam("email")String email){
        return userService.checkEmail(email);
    }

    /*
    *检查用户名是否已经注册
     * @param userName 邮箱地址
     * @return SUCCESS-->已经注册了，FAILED-->没有注册
     * */
    @ApiResponses({
            @ApiResponse(code = 20000,message = "表示该用户已经注册"),
            @ApiResponse(code = 40000,message = "表示该用户未注册")
    })
    @GetMapping("/user_name")
    public ResponseResult checkUserName(@RequestParam("userName") String userName){
        return userService.checkUserName(userName);
    }

    /**
     * 更新邮箱地址
     * 1、必须已经登录了
     * 2、新的邮箱没有注册过
     * <p>
     * 用户的步骤：
     * 1、已经登录
     * 2、输入新的邮箱地址
     * 3、获取验证码 type=update
     * 4、输入验证码
     * 5、提交数据
     * <p>
     * 需要提交的数据
     * 1、新的邮箱地址
     * 2、验证码
     * 3、其他信息我们可以token里获取
     *
     * @return
     */
    @PutMapping("/email")
    public ResponseResult updateEmail(@RequestParam("email") String email,
                                      @RequestParam("verify_code") String verifyCode) {
        return userService.updateEmail(email, verifyCode);
    }

    /**
     * 退出登录
     * <p>
     * 拿到token_key
     * -> 删除redis里对应的token
     * -> 删除mysql里对应的refreshToken
     * -> 删除cookie里的token_key
     *
     * @return
     */
    @GetMapping("/logout")
    public ResponseResult logout() {
        return userService.doLogout();
    }

    /***
     * 获取二维码：
     * 二维码的图片路径
     * 二维码的内容字符串
     * 要防止太频繁的请求
     * @return
     */
    @GetMapping("/pc-login-qr-code")
    public ResponseResult getPcLoginQrCode() {
        return userService.getPcLoginQrCodeInfo();
    }

    @PutMapping("/qr-code-state/{loginId}")
    public ResponseResult updateQrCodeLoginState(@PathVariable("loginId") String loginId) {
        return userService.updateQrCodeLoginState(loginId);
    }

    /**
     * 检查二维码的登录状态
     *
     * @return
     */
    @GetMapping("/qr-code-state/{loginId}")
    public ResponseResult checkQrCodeLoginState(@PathVariable("loginId") String loginId) {
        return userService.checkQrCodeLoginState(loginId);
    }

    @GetMapping("/check_token")
    public ResponseResult parseToken() {
        return userService.parseToken();
    }

    @PreAuthorize("@permission.admin()")
    @PutMapping("/reset_password/{userId}")
    public ResponseResult resetPassword(@PathVariable("userId") String userId, @RequestParam("password") String password) {
        return userService.resetPassword(userId, password);
    }

    @PreAuthorize("@permission.admin()")
    @GetMapping("/register_count")
    public ResponseResult getRegisterCount() {
        return userService.getRegisterCount();
    }

    @GetMapping("/check_email_code")
    public ResponseResult checkEmailCode(@RequestParam("email") String email,
                                         @RequestParam("emailCode") String emailCode,
                                         @RequestParam("captchaCode") String captchaCode) {
        return userService.checkEmailCode(email, emailCode, captchaCode);
    }
}
