package net.feng.blog.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.feng.blog.pojo.User;
import net.feng.blog.service.IUserService;
import net.feng.blog.utils.Constants;
import net.feng.blog.utils.CookieUtils;
import net.feng.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
@Slf4j
@Service("permission")
public class PermissionService {

    @Autowired
    private IUserService userService;

    public boolean admin(){
        //拿到request和response
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        String tokenKey = CookieUtils.getCookie(request, Constants.User.COOKIE_TOKEN_KEY);
        //没有令牌的key，没有登录，不用往下执行了
        if (TextUtils.isEmpty(tokenKey)) {
            return false;
        }

        User user = userService.checkLoginUser();
        if (user == null) {
            return false;
        }
        log.info("PermissionService username===> " + user.getUserName());
        if (Constants.User.ROLE_ADMIN.equals(user.getRoles())) {
            //管理员
            return true;
        }
        return false;
    }
}
