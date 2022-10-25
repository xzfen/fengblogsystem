package net.feng.blog.interceptor;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.feng.blog.response.ResponseResult;
import net.feng.blog.utils.Constants;
import net.feng.blog.utils.CookieUtils;
import net.feng.blog.utils.RedisUtil;
import net.feng.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.reflect.Method;

@Slf4j
@Component
public class ApiInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private Gson gson;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            String name = method.getName();
            log.info("method ==>" + name);

            CheckTooFrequentCommit methodAnnotation = handlerMethod.getMethodAnnotation(CheckTooFrequentCommit.class);
            if(methodAnnotation != null) {
                //所有提交内容的方法，必须用户登录的，所以使用token作为key来记录请求频率
                String tokenKey = CookieUtils.getCookie(request, Constants.User.COOKIE_TOKEN_KEY);
                log.info("tokenKey ==> " + tokenKey);
                if (!TextUtils.isEmpty(tokenKey)) {
                    String hasCommit = (String) redisUtil.get(Constants.User.KEY_COMMIT_TOKEN_RECORD + tokenKey);
                    if (!TextUtils.isEmpty(hasCommit)) {
                        //从redis丽获取，判断是否存在，如果存在，则返回提交频繁
                        response.setCharacterEncoding("UTF-8");
                        response.setContentType("application/json");
                        ResponseResult failed = ResponseResult.FAILED("提交过于频繁，请稍后重试...");
                        PrintWriter writer = response.getWriter();
                        writer.write(gson.toJson(failed));
                        writer.flush();
                        return false;
                    } else {
                        //如果不存在，说明可以提交，并且记录此次提交，有效期为15秒
                        redisUtil.set(Constants.User.KEY_COMMIT_TOKEN_RECORD + tokenKey, "true", Constants.TimeValueInSecond.SEC_15);
                    }
                }
                //去判断是否真提交太频繁了
                log.info("check commit too frequent...");
            }
        }
        //true表示放行
        //false表示拦截
        return true;
    }
}
