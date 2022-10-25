package net.feng.blog.service.impl;

import com.google.gson.Gson;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import net.feng.blog.dao.RefreshTokenDao;
import net.feng.blog.dao.SettingDao;
import net.feng.blog.dao.UserDao;
import net.feng.blog.pojo.RefreshToken;
import net.feng.blog.pojo.Setting;
import net.feng.blog.pojo.User;
import net.feng.blog.response.ResponseResult;
import net.feng.blog.service.IUserService;
import net.feng.blog.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional
public class UserServiceImpl implements IUserService {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private SnowFlakeIdWorker idWorker;
    @Autowired
    private UserDao userDao;
    @Autowired
    private SettingDao settingsDao;
    @Autowired
    private Gson gson;

    /*
    *初始化管理员账号
    * */
    @Override
    public ResponseResult initAdminAccount(User user, HttpServletRequest request) {
        //检查数据
        if (TextUtils.isEmpty(user.getUserName())) {
            return ResponseResult.FAILED("用户名不能为空");
        }
        if (TextUtils.isEmpty(user.getPassword())) {
            return ResponseResult.FAILED("密码不能为空");
        }
        if (TextUtils.isEmpty(user.getEmail())) {
            return ResponseResult.FAILED("邮箱不能为空");
        }

        //检查是否有初始化
        Setting adminAccount = settingsDao.findOneByKey(Constants.Settings.ADMIN_ACCOUNT_INIT_STATE);
        if (adminAccount != null) {
            return ResponseResult.FAILED("管理员账号已经初始化了");
        }

        //补充数据
        user.setId(String.valueOf(idWorker.nextId()));
        user.setRoles(Constants.User.ROLE_ADMIN);
        user.setAvatar(Constants.User.DEFAULT_AVATAR);
        user.setState(Constants.User.DEFAULT_STATE);
        String remoteAddr = request.getRemoteAddr();
        String localAddr = request.getLocalAddr();
        log.info("remoteAddr == > " + remoteAddr);
        log.info("localAddr == > " + localAddr);
        user.setLoginIp(remoteAddr);
        user.setRegIp(remoteAddr);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        //对密码进行加密
        //源密码
        String password = user.getPassword();
        //加密密码
        String encode = bCryptPasswordEncoder.encode(password);
        user.setPassword(encode);

        //保存到数据库里
        userDao.save(user);
        //更新已经添加的标记
        Setting setting = new Setting();
        setting.setId(idWorker.nextId() + "");
        setting.setKey(Constants.Settings.ADMIN_ACCOUNT_INIT_STATE);
        setting.setCreateTime(new Date());
        setting.setUpdateTime(new Date());
        setting.setValue("1");
        settingsDao.save(setting);
        return ResponseResult.SUCCESS("初始化成功");
    }

    public static final int[] captcha_font_types = {Captcha.FONT_1
            ,Captcha.FONT_2
            ,Captcha.FONT_3
            ,Captcha.FONT_4
            ,Captcha.FONT_5
            ,Captcha.FONT_6
            ,Captcha.FONT_7
            ,Captcha.FONT_8
            ,Captcha.FONT_9
            ,Captcha.FONT_10};

    @Autowired
    private Random random;

    @Autowired
    private RedisUtil redisUtil;

    /*
     *获取图灵验证码captcha
     * */
    @Override
    public void createCaptcha(HttpServletResponse response, String captchaKey) throws Exception {
        if(TextUtils.isEmpty(captchaKey)||captchaKey.length()<13){
            return;
        }
        long key;
        try {
            key=Long.parseLong(captchaKey);
        }catch (Exception e){
            return;
        }

        // 设置请求头为输出图片类型
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        // 设置类型，纯数字、纯字母、字母数字混合
        int captchaType=random.nextInt(3);
        Captcha targetCaptcha;
        if (captchaType==0){
            // 三个参数分别为宽、高、位数
            targetCaptcha = new SpecCaptcha(130, 48, 5);
        }else if(captchaType==1){
            //gif类型
            targetCaptcha=new GifCaptcha(130,48);
        }else {
            //算式类型
            targetCaptcha=new ArithmeticCaptcha(130,48);
            targetCaptcha.setLen(2);//几位数运算，默认是2位
        }
        targetCaptcha.setCharType(captchaType);

        // 设置字体
        int index = random.nextInt(captcha_font_types.length);
        log.info("captcha type index==> "+index);
        targetCaptcha.setFont(captcha_font_types[index]);

        //获取数据
        String content = targetCaptcha.text().toLowerCase();
        log.info("captcha content == > " + content);

        //保存到redis里,10分钟有效
        redisUtil.set(Constants.User.KEY_CAPTCHA_CONTENT+key,content,60*10);

        // 输出图片流
        targetCaptcha.out(response.getOutputStream());
    }

    @Autowired
    private TaskService taskService;
    /*
    * 发送邮箱验证码
    * <p>
    * 注册（register）：如果已经注册过了就提示说，该邮箱已经注册
    * 找回密码（forget）：如果没有注册过，提示该邮箱没有注册
    * 修改邮箱（update）：（新的邮箱）：如果已经注册了，提示该邮箱已经注册
    * */
    @Override
    public ResponseResult sendEmail(String type, HttpServletRequest request, String emailAddress) {
        if(emailAddress == null){
            return ResponseResult.FAILED("邮箱地址不可以为空");
        }

        if("register".equals(type) || "update".equals(type)){
            User userByEmail = userDao.findOneByEmail(emailAddress);
            if(userByEmail != null){
                return ResponseResult.FAILED("该邮箱已经注册");
            }
        }

        if("forget".equals(type)){
            User userByEmail = userDao.findOneByEmail(emailAddress);
            if(userByEmail == null){
                return ResponseResult.FAILED("该邮箱未注册");
            }
        }

        //1.防止暴力发送，同一个邮箱，间隔要超过30秒，同一个IP最多只能发10次（如果是短信，最多发5次）
        String remoteAddr = request.getRemoteAddr();
        log.info(("remoteAddr==> "+remoteAddr));
        if(remoteAddr!=null){
            remoteAddr=remoteAddr.replace(":","_");
        }
        Integer ipSendTime = (Integer) redisUtil.get(Constants.User.KEY_EMAIL_SEND_IP + remoteAddr);
        if(ipSendTime!=null&&ipSendTime > 10) {
            return ResponseResult.FAILED("发送太频繁！");
        }
        Object hasEmailSend = redisUtil.get(Constants.User.KEY_EMAIL_SEND_ADDRESS + remoteAddr);
        if(hasEmailSend!=null) {
            return ResponseResult.FAILED("发送太频繁！");
        }
        //2.检查邮箱地址是否正确
        boolean isEmail = TextUtils.isEmail(emailAddress);
        if (!isEmail) {
            return ResponseResult.FAILED("邮箱地址格式不正确");
        }
        //生成随机验证码
        int code=random.nextInt(999999);
        if(code<100000){
            code+=100000;
        }
        log.info("sendEmail code==> "+code);
        //3.发送验证码，6位数：100000~999999
        try {
            taskService.sendEmailVerifyCode(String.valueOf(code),emailAddress);
        } catch (Exception e) {
            return ResponseResult.FAILED("验证码发送失败，请重新发送");
        }
        //4.做记录
        //发送记录：code
        if(ipSendTime==null){
            ipSendTime=0;
        }
        ipSendTime++;
        //1个小时内有效
        redisUtil.set(Constants.User.KEY_EMAIL_SEND_IP+remoteAddr,ipSendTime,60*60);
        redisUtil.set(Constants.User.KEY_EMAIL_SEND_ADDRESS+remoteAddr,"true",30);
        //保存code,10分钟内有效
        redisUtil.set(Constants.User.KEY_EMAIL_CODE_CONTENT+emailAddress,String.valueOf(code),60*10);

        return ResponseResult.SUCCESS("验证码发送成功");
    }

    /*
    * 完成用户注册
    * */
    @Override
    public ResponseResult register(User user, String emailCode, String captchaCode, String captchaKey, HttpServletRequest request) {
        //第一步：检查当前用户名是否已经注册
        String userName = user.getUserName();
        if (TextUtils.isEmpty(userName)) {
            return ResponseResult.FAILED("用户名不可以为空.");
        }
        User userByName = userDao.findOneByUserName(userName);
        if (userByName != null) {
            return ResponseResult.FAILED("该用户名已注册.");
        }
        //第二步：检查邮箱格式是否正确
        String email = user.getEmail();
        if (TextUtils.isEmpty(email)) {
            return ResponseResult.FAILED("邮箱地址不可以为空.");
        }
        if (!TextUtils.isEmail(email)) {
            return ResponseResult.FAILED("邮箱地址格式不正确");
        }
        //第三步：检查该邮箱是否已经注册
        User userByEmail = userDao.findOneByEmail(email);
        if (userByEmail != null) {
            return ResponseResult.FAILED("该邮箱地址已经注册");
        }
        //第四步：检查邮箱验证码是否正确
        String emailVerifyCode = (String) redisUtil.get(Constants.User.KEY_EMAIL_CODE_CONTENT + email);
        if (TextUtils.isEmpty(emailVerifyCode)) {
            return ResponseResult.FAILED("邮箱验证码已过期");
        }
        if (!emailVerifyCode.equals(emailCode)) {
            return ResponseResult.FAILED("邮箱验证码不正确");
        } else {
            //正确，干掉redis里的内容
            redisUtil.del(Constants.User.KEY_EMAIL_CODE_CONTENT + email);
        }
        //第五步：检查图灵验证码是否正确
        String captchaVerifyCode = (String) redisUtil.get(Constants.User.KEY_CAPTCHA_CONTENT + captchaKey);
        if (TextUtils.isEmpty(captchaVerifyCode)) {
            return ResponseResult.FAILED("人类验证码已过期");
        }
        if (!captchaVerifyCode.equals(captchaCode)) {
            return ResponseResult.FAILED("人类验证码不正确");
        } else {
            redisUtil.del(Constants.User.KEY_CAPTCHA_CONTENT + captchaKey);
        }
        //达到可以注册的条件
        //第六步：对密码进行加密
        String password = user.getPassword();
        if (TextUtils.isEmpty(password)) {
            return ResponseResult.FAILED("密码不可以为空");
        }
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        //第七步：补全数据
        //包括：注册IP,登录IP,角色,头像,创建时间,更新时间
        String ipAddress = request.getRemoteAddr();
        user.setRegIp(ipAddress);
        user.setLoginIp(ipAddress);
        user.setUpdateTime(new Date());
        user.setCreateTime(new Date());
        user.setAvatar(Constants.User.DEFAULT_AVATAR);
        user.setRoles(Constants.User.ROLE_NORMAL);
        user.setState("1");
        user.setId(idWorker.nextId() + "");
        //第八步：保存到数据库中
        userDao.save(user);
        //第九步：返回结果
        return ResponseResult.JOIN_IN_SUCCESS();
    }

    @Override
    public ResponseResult doLogin(String captcha,
                                  String captchaKey,
                                  String from,
                                  User user) {
        //from可能没有值，如果没有值，就给它一个值
        if (TextUtils.isEmpty(from) ||
                (!Constants.FROM_MOBILE.equals(from) && !Constants.FROM_PC.equals(from))) {
            from = Constants.FROM_MOBILE;
        }
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
        String captchaValue = (String) redisUtil.get(Constants.User.KEY_CAPTCHA_CONTENT + captchaKey);
        if (!captcha.equals(captchaValue)) {
            return ResponseResult.FAILED("人类验证码不正确");
        }
        //验证成功，删除redis里的验证码
        redisUtil.del(Constants.User.KEY_CAPTCHA_CONTENT + captchaKey);
        //有可能是邮箱，也有可能是用户名
        String userName = user.getUserName();
        if (TextUtils.isEmpty(userName)) {
            return ResponseResult.FAILED("账号不可以为空.");
        }

        String password = user.getPassword();
        if (TextUtils.isEmpty(password)) {
            return ResponseResult.FAILED("密码不可以为空.");
        }

        User userFromDb = userDao.findOneByUserName(userName);
        if (userFromDb == null) {
            userFromDb = userDao.findOneByEmail(userName);
        }

        if (userFromDb == null) {
            return ResponseResult.FAILED("用户名或密码不正确");
        }
        //用户存在
        //对比密码
        boolean matches = bCryptPasswordEncoder.matches(password, userFromDb.getPassword());
        if (!matches) {
            return ResponseResult.FAILED("用户名或密码不正确");
        }
        //密码是正确
        //判断用户状态，如果是非正常的状态，则返回结果
        if (!Constants.User.COLUMN_USER_STATE_NORMAL.equals(userFromDb.getState())) {
            return ResponseResult.PERMISSION_DENIED();
        }
        //修改更新时间和登录IP
        userFromDb.setLoginIp(request.getRemoteAddr());
        userFromDb.setUpdateTime(new Date());
        //生成Token
        createToken(response, userFromDb, from);
        return ResponseResult.SUCCESS("登录成功");
    }

    @Override
    public ResponseResult getUserInfo(String userId) {
        //从数据库获取
        User user = userDao.findOneById(userId);
        //判断结果
        if (user==null) {
            //如果不存在，返回
            return ResponseResult.FAILED("用户不存在");
        }
        //如果存在，就复制对象
        //清空密码、Email、登录ID、注册IP
        String userJson = gson.toJson(user);
        User newUser = gson.fromJson(userJson, User.class);
        newUser.setPassword("");
        newUser.setEmail("");
        newUser.setRegIp("");
        newUser.setLoginIp("");
        //返回结果
        return ResponseResult.SUCCESS("获取成功").setData(newUser);
    }

    /*
    * 检查该Email是否已经注册
    * */
    @Override
    public ResponseResult checkEmail(String email) {
        User user = userDao.findOneByEmail(email);
        return user == null?ResponseResult.FAILED("该邮箱未注册"):ResponseResult.SUCCESS("该邮箱已注册");
    }

    /*
     *检查用户名是否已经注册
     * */
    @Override
    public ResponseResult checkUserName(String userName) {
        User user = userDao.findOneByUserName(userName);
        return user == null?ResponseResult.FAILED("该用户名未注册"):ResponseResult.SUCCESS("该用户名已注册");
    }

    /*
     * 修改用户信息user-info
     * */
    @Override
    public ResponseResult updateUserInfo(String userId, User user) {
        //从token里解析出来的user，为了校验权限
        //只有用户才可以修改自己的信息
        User userFromTokenKey= checkLoginUser();
        if (userFromTokenKey==null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }

        User userFromDb = userDao.findOneById(userFromTokenKey.getId());

        //可以判断用户的Id是否一致，如果一致才可以修改
        if(!userFromDb.getId().equals(userId)){
            return ResponseResult.FAILED("无权限修改...");
        }
        //可以进行修改
        //可以修改的项
        //用户名
        String userName = user.getUserName();
        if (!TextUtils.isEmpty(userName)) {
            User userByUserName = userDao.findOneByUserName(userName);
            if (userByUserName != null) {
                return ResponseResult.FAILED("该用户已注册...");
            }
            userFromDb.setUserName(userName);
        }
        //头像
        if (!TextUtils.isEmpty(user.getAvatar())) {
            userFromDb.setAvatar(user.getAvatar());
        }
        userFromDb.setUpdateTime(new Date());
        //签名,可以为空
        userFromDb.setSign(user.getSign());
        userDao.save(userFromDb);

        //干掉redis里的token，下一次请求，需要解析token的，就会根据refreshToken重新创建一个
        String tokenKey = CookieUtils.getCookie(getRequest(), Constants.User.COOKIE_TOKEN_KEY);
        redisUtil.del(Constants.User.KEY_TOKEN + tokenKey);
        return ResponseResult.SUCCESS("用户信息更新成功...");
    }

    /*
    * 通过携带的token_key检查用户是否有登录，如果登录了，就返回用户信息
    * */
    @Override
    public User checkLoginUser() {
        //拿到token_key
        String tokenKey = CookieUtils.getCookie(getRequest(), Constants.User.COOKIE_TOKEN_KEY);
        log.info("checkLoginUser tokenKey ==> "+tokenKey);
        if (TextUtils.isEmpty(tokenKey)) {
            return null;
        }
        //从tokenKey中要解析出此请求是什么端的
        String from = tokenKey.startsWith(Constants.FROM_PC) ? Constants.FROM_PC : Constants.FROM_MOBILE;
        //从tokenKey中解析出登录用户
        User user = parseByTokenKey(tokenKey);
        if (user == null) {
            //说明解析出错或者过期了
            //1、去mysql查询refreshToken
            //如果是从pc，我们就以pc的tokenKey来查
            //如果是从mobile，我们就以mobile的tokenKey来查
            RefreshToken refreshToken;
            if(Constants.FROM_PC.equals(from)) {
                refreshToken = refreshTokenDao.findOneByTokenKey(tokenKey);
            } else {
                refreshToken = refreshTokenDao.findOneByMobileTokenKey(tokenKey);
            }
            //2、如果不存在，就是当前访问没登录，提示用户登录
            if (refreshToken == null){
                log.info("refreshToken is null... ");
                return null;
            }
            //3、如果存在就解析refreshToken
            try{
                //这个解析有可能出错，就过期了
                JwtUtil.parseJWT(refreshToken.getRefreshToken());
                //5、如果refreshToken有效，创建新的token和新的refreshToken
                String userId = refreshToken.getUserId();
                User userfromDb = userDao.findOneById(userId);
                //千万别这么干，事务还没有提交，如果这样设置，数据库里的密码就没有了
                //userfromDb.setPassword("");
                //删掉refreshToken的记录

                String newTokenKey = createToken(getResponse(), userfromDb, from);
                //返回token
                log.info("created new token and refresh token... ");
                return parseByTokenKey(newTokenKey);
            } catch (Exception e){
                log.info("refreshToken is 过期了... ");
                //4、如果refreshToken过期了，就当前访问没有登录，提示用户登录
                return null;
            }
        }
        return user;
    }

    private HttpServletRequest getRequest(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes.getRequest();
    }

    private HttpServletResponse getResponse(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes.getResponse();
    }

    /*
    * 删除用户，并不是真的删除，而是修改状态
    * */
    @Override
    public ResponseResult deleteUserById(String userId) {
        //检查当前操作的用户是谁
        User currentUser = checkLoginUser();
        log.info("currentUser==> "+currentUser.getUserName());
        if(currentUser == null){
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        //判断角色
        if (!Constants.User.ROLE_ADMIN.equals(currentUser.getRoles())) {
            return ResponseResult.PERMISSION_DENIED();
        }
        //可以删除用户了
        //从数据库获取
        User userFromDb = userDao.findOneById(userId);
        //0表示删除，1表示正常
        userFromDb.setState(Constants.User.COLUMN_USER_STATE_DELETE);
        userDao.save(userFromDb);
        return ResponseResult.SUCCESS("删除成功");
    }

    /*
    * 获取用户列表
    * 需要管理员权限
    * */
    @Override
    public ResponseResult listUsers(int page, int size) {
        //检查当前操作的用户是谁
        User currentUser = checkLoginUser();
        log.info("currentUser==> "+currentUser.getUserName());
        if(currentUser == null){
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        //判断角色
        if (!Constants.User.ROLE_ADMIN.equals(currentUser.getRoles())) {
            return ResponseResult.PERMISSION_DENIED();
        }
        //可以获取用户列表
        //分页查询
        if(page < Constants.Page.DEFAULT_PAGE){
            page = Constants.Page.DEFAULT_PAGE;
        }
        //size也限制一下，每一页不少于10个
        if(size < Constants.Page.DEFAULT_SIZE){
            size = Constants.Page.DEFAULT_SIZE;
        }
        //根据注册日期排序
        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");
        //获取分页信息
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        //执行查询
        Page<User> all = userDao.findAll(pageable);

        return ResponseResult.SUCCESS("获取用户列表成功").setData(all);
    }

    /*
    * 更新密码
    * */
    @Override
    public ResponseResult updateUserPassword(String verifyCode, User user) {
        //检查邮箱是否有填写
        String email = user.getEmail();
        if (TextUtils.isEmpty(email)) {
            return ResponseResult.FAILED("邮箱不可以为空.");
        }
        //根据邮箱去redis里拿验证
        //进行对比
        String redisVerifyCode = (String) redisUtil.get(Constants.User.KEY_EMAIL_CODE_CONTENT + email);
        if (redisVerifyCode == null || !redisVerifyCode.equals(verifyCode)) {
            return ResponseResult.FAILED("验证码错误.");
        }
        redisUtil.del(Constants.User.KEY_EMAIL_CODE_CONTENT + email);
        int result = userDao.updatePasswordByEmail(bCryptPasswordEncoder.encode(user.getPassword()), email);
        //修改密码
        return result > 0 ? ResponseResult.SUCCESS("密码修改成功") : ResponseResult.FAILED("密码修改失败");
    }

    /**
     * 更新邮箱地址
     *
     * @param email
     * @param verifyCode
     * @return
     */
    @Override
    public ResponseResult updateEmail(String email, String verifyCode) {
        //1、确保用户已经登录了
        User user = this.checkLoginUser();
        //没有登录
        if (user == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        //2、对比验证码，确保新的邮箱地址是属于当前用户的
        String redisVerifyCode = (String) redisUtil.get(Constants.User.KEY_EMAIL_CODE_CONTENT + email);
        if (TextUtils.isEmpty(redisVerifyCode) || !redisVerifyCode.equals(verifyCode)) {
            return ResponseResult.FAILED("验证码错误");
        }
        //验证码正确，删除验证码
        redisUtil.del(Constants.User.KEY_EMAIL_CODE_CONTENT + email);

        //可以修改邮箱
        int result = userDao.updateEmailById(email, user.getId());
        return result > 0 ? ResponseResult.SUCCESS("邮箱修改成功") : ResponseResult.FAILED("邮箱修改失败");
    }

    @Override
    public ResponseResult doLogout() {
        //拿到token_key
        String tokenKey = CookieUtils.getCookie(getRequest(), Constants.User.COOKIE_TOKEN_KEY);
        if (TextUtils.isEmpty(tokenKey)) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        //刪除redis里的token,因为各端是独立的，可以删除
        redisUtil.del(Constants.User.KEY_TOKEN + tokenKey);
        //删除mysql里的refreshToken
        //这个不做删除，只做更新
        //refreshTokenDao.deleteAllByTokenKey(tokenKey);
        if(tokenKey.startsWith(Constants.FROM_PC)) {
            refreshTokenDao.deletePcTokenKey(tokenKey);
        } else {
            refreshTokenDao.deleteMobileTokenKey(tokenKey);
        }
        //删除cookie里的token_key
        CookieUtils.deleteCookie(getResponse(), Constants.User.COOKIE_TOKEN_KEY);
        return ResponseResult.SUCCESS("退出登录成功.");
    }

    @Override
    public ResponseResult getPcLoginQrCodeInfo() {
        //尝试取出上一次的loginId
        String lastLoginId = CookieUtils.getCookie(getRequest(), Constants.User.LAST_REQUEST_LOGIN_ID);
        if (!TextUtils.isEmpty(lastLoginId)) {
            //先把redis里的删除
            redisUtil.del(Constants.User.KEY_PC_LOGIN_ID + lastLoginId);
            //检查上次的请求时间，如果太频繁，则直接返回
            Object lastGetTime = redisUtil.get(Constants.User.LAST_REQUEST_LOGIN_ID + lastLoginId);
            if (lastGetTime != null) {
                return ResponseResult.FAILED("服务器繁忙，请稍后重试.");
            }
        }
        // 1、生成一个唯一的ID
        long code = idWorker.nextId();
        // 2、保存到redis里，值为false，时间为5分钟（二维码的有效期）
        redisUtil.set(Constants.User.KEY_PC_LOGIN_ID + code,
                Constants.User.KEY_PC_LOGIN_STATE_FALSE,
                Constants.TimeValueInSecond.MIN_5);
        Map<String, Object> result = new HashMap<>();
        String originalDomain = TextUtils.getDomain(getRequest());
        result.put("code", code);
        result.put("url", originalDomain + "/portal/images/qr-code/" + code);
        CookieUtils.setUpCookie(getResponse(), Constants.User.LAST_REQUEST_LOGIN_ID, String.valueOf(code));
        redisUtil.set(Constants.User.LAST_REQUEST_LOGIN_ID + String.valueOf(code),
                "true", Constants.TimeValueInSecond.SECOND_10);
        // 返回结果
        return ResponseResult.SUCCESS("获取成功.").setData(result);
    }

    @Autowired
    CountDownLatchManager countDownLatchManager;

    /**
     * 更新二维码的登录状态
     *
     * @param loginId
     * @return
     */
    @Override
    public ResponseResult updateQrCodeLoginState(String loginId) {
        //1、检查用户是否登录
        User user = checkLoginUser();
        if (user == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        //2、改变loginId对应的值=true
        redisUtil.set(Constants.User.KEY_PC_LOGIN_ID + loginId, user.getId());
        //2.1、通知正在等待的扫描任务
        countDownLatchManager.onPhoneDoLogin(loginId);
        //3、返回结果
        return ResponseResult.SUCCESS("登录成功.");
    }

    /**
     * 检查二维码的登录状态
     * 结果有：
     * 1、登录成功（loginId对应的值为有ID内容）
     * 2、等待扫描（loginId对应的值为false）
     * 3、二维码已经过期了 loginId对应的值为null
     * <p>
     * 是被PC端轮询调用的
     *
     * @param loginId
     * @return
     */
    @Override
    public ResponseResult checkQrCodeLoginState(String loginId) {
        //从redis里取值出来
        ResponseResult result = checkLoginIdState(loginId);
        if (result != null) return result;
        //先等待一段时间，再去检查
        //如果超出了这个时间，我就们就返回等待扫码
        Callable<ResponseResult> callable = new Callable<ResponseResult>() {
            @Override
            public ResponseResult call() throws Exception {
                try {
                    log.info("start waiting for scan...");
                    //先阻塞
                    countDownLatchManager.getLatch(loginId).await(Constants.User.QR_CODE_STATE_CHECK_WAITING_TIME,
                            TimeUnit.SECONDS);
                    //收到状态更新的通知，我们就检查loginId对应的状态
                    log.info("start check login state...");
                    ResponseResult checkResult = checkLoginIdState(loginId);
                    if (checkResult != null) return checkResult;
                    //超时则返回等待扫描
                    //完事后，删除对应的latch
                    return ResponseResult.WAiTING_FOR_SCAN();
                } finally {
                    log.info("delete latch...");
                    countDownLatchManager.deleteLatch(loginId);
                }
            }
        };
        try {
            return callable.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.WAiTING_FOR_SCAN();
    }

    @Override
    public ResponseResult parseToken() {
        User user = checkLoginUser();
        if (user == null) {
            return ResponseResult.FAILED("用户未登录");
        }
        return ResponseResult.SUCCESS("获取登录信息成功").setData(user);
    }

    @Override
    public ResponseResult resetPassword(String userId, String password) {
        // 查询用户
        User user = userDao.findOneById(userId);
        // 判断用户是否存在
        if (user == null) {
            return ResponseResult.FAILED("用户不存在");
        }
        // 对密码进行加密
        user.setPassword(bCryptPasswordEncoder.encode(password));
        // 处理结果
        userDao.save(user);
        return ResponseResult.SUCCESS("密码重置成功");
    }

    @Override
    public ResponseResult getRegisterCount() {
        long count = userDao.count();
        return ResponseResult.SUCCESS("获取用户总数成功").setData(count);
    }

    @Override
    public ResponseResult checkEmailCode(String email, String emailCode, String captchaCode) {
        // 检查人类验证码是否正确
        String captchaId = CookieUtils.getCookie(getRequest(), Constants.User.LAST_CAPTCHA_ID);
        String captcha = (String) redisUtil.get(Constants.User.KEY_CAPTCHA_CONTENT + captchaId);
        if (!captchaCode.equals(captcha)) {
            return ResponseResult.FAILED("图灵验证码不正确");
        }
        // 检查邮箱验证码
        String redisVerifyCode = (String) redisUtil.get(Constants.User.KEY_EMAIL_CODE_CONTENT + email);
        if (!emailCode.equals(redisVerifyCode)) {
            return ResponseResult.FAILED("邮箱验证码不正确");
        }
        return ResponseResult.SUCCESS("邮箱验证码正确");
    }

    private ResponseResult checkLoginIdState(String loginId) {
        String loginState = (String) redisUtil.get(Constants.User.KEY_PC_LOGIN_ID + loginId);
        if (loginState == null) {
            //二维码过期
            return ResponseResult.QR_CODE_DEPRECATE();
        }

        //不为false,且不为null，那么就是用户的ID了，也就是登录成功了
        if (!TextUtils.isEmpty(loginState) && !Constants.User.KEY_PC_LOGIN_STATE_FALSE.equals(loginState)) {
            //创建token，也就是走PC端的登录
            User userFromDb = userDao.findOneById(loginState);
            if (userFromDb == null) {
                return ResponseResult.QR_CODE_DEPRECATE();
            }
            createToken(getResponse(), userFromDb, Constants.FROM_PC);
            CookieUtils.deleteCookie(getResponse(), Constants.User.LAST_REQUEST_LOGIN_ID);
            //登录成功
            return ResponseResult.LOGIN_SUCCESS();
        }
        return null;
    }

    //解析此token是从PC端来的还是移动端来的
    private String parseFrom(String tokenKey) {
        String token = (String) redisUtil.get(Constants.User.KEY_TOKEN + tokenKey);
        if(token != null){
            try{
                Claims claims = JwtUtil.parseJWT(token);
                return ClaimsUtils.getFrom(claims);
            }catch (Exception e){
                log.info("parseByTokenKey==> "+ tokenKey +"过期了...");
            }
        }
        return null;
    }
    private User parseByTokenKey(String tokenKey) {
        String token = (String) redisUtil.get(Constants.User.KEY_TOKEN + tokenKey);
        if(token != null){
            try{
                Claims claims = JwtUtil.parseJWT(token);
                return ClaimsUtils.claims2User(claims);
            }catch (Exception e){
                log.info("parseByTokenKey==> "+ tokenKey +"过期了...");
                return null;
            }
        }
        return null;
    }

    @Autowired
    RefreshTokenDao refreshTokenDao;

    /**
     * @param response
     * @param userFromDb
     * @param from
     * @return token_key
     */
    private String createToken(HttpServletResponse response, User userFromDb, String from) {
        String oldTokenKey = CookieUtils.getCookie(getRequest(), Constants.User.COOKIE_TOKEN_KEY);
        RefreshToken oldRefreshToiken = refreshTokenDao.findOneByUserId(userFromDb.getId());
        if(Constants.FROM_MOBILE.equals(from)) {
            //确保单端登录，删除redis里的token
            if(oldRefreshToiken != null) {
                redisUtil.del(Constants.User.KEY_TOKEN + oldRefreshToiken.getMobileTokenKey());
            }
            //根据来源删除refreshToken中对应的token_key
            refreshTokenDao.deleteMobileTokenKey(oldTokenKey);
            log.info("deleteResult of mobile refresh token .. ");
        } else if(Constants.FROM_PC.equals(from)) {
            if(oldRefreshToiken != null) {
                redisUtil.del(Constants.User.KEY_TOKEN + oldRefreshToiken.getTokenKey());
            }
            refreshTokenDao.deletePcTokenKey(oldTokenKey);
            log.info("deleteResult of pc refresh token .. ");
        }
        //从User对象，生成token需要的Claims
        Map<String, Object> claims = ClaimsUtils.user2Claims(userFromDb, from);
        //生成token,token默认有效为2个小时
        String token = JwtUtil.createToken(claims);
        //返回token的md5值，token会保存到redis里
        //前端访问的时候，携带token的md5key，从redis中获取即可
        String tokenKey = from + DigestUtils.md5DigestAsHex(token.getBytes());
        //保存token到redis里，有效期为2个小时，key是tokenKey
        redisUtil.set(Constants.User.KEY_TOKEN + tokenKey, token, Constants.TimeValueInSecond.HOUR_2);
        //把tokenKey写到cookies里
        //这个要动态获取，可以从request里获取，
        CookieUtils.setUpCookie(response, Constants.User.COOKIE_TOKEN_KEY, tokenKey);
        //先判断数据库里有没有refreshToken
        //如果有的话就更新
        //如果没有的话就新创建
        RefreshToken refreshToken = refreshTokenDao.findOneByUserId(userFromDb.getId());
        if (refreshToken == null) {
            refreshToken = new RefreshToken();
            refreshToken.setId(idWorker.nextId() + "");
            refreshToken.setCreateTime(new Date());
            refreshToken.setUserId(userFromDb.getId());
        }
        //不管是过期还是新登录，都生成、更新refreshToken
        //生成refreshToken,有效期1个月
        String refreshTokenValue = JwtUtil.createRefreshToken(userFromDb.getId(), Constants.TimeValueInMillisecond.MONTH);
        //保存到数据库里
        //refreshToken，tokenKey，用户ID，创建时间，更新时间
        refreshToken.setRefreshToken(refreshTokenValue);
        //要判断来源，如果是移动端的就设置到移动端，如果是PC的，就设置到默认的
        if (Constants.FROM_PC.equals(from)) {
            refreshToken.setTokenKey(tokenKey);
        } else {
            refreshToken.setMobileTokenKey(tokenKey);
        }
        refreshToken.setUpdateTime(new Date());
        refreshTokenDao.save(refreshToken);
        return tokenKey;
    }
}
