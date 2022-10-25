package net.feng.blog.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieUtils {

    //1年
    public static final int default_age = 60 * 60 * 24 * 365;
    //TODO: 需要根据配置修改为域名
    public static final String domain = "feng.net";

    /*
    * 设置Cookie值
    * */
    public static void setUpCookie(HttpServletResponse response,String key,String value){
        setUpCookie(response,key,value,default_age);
    }

    public static void setUpCookie(HttpServletResponse response,String key,String value,int age){
        Cookie cookie = new Cookie(key,value);
        cookie.setPath("/");
        /**
         * TODO: 需要根据配置修改为域名
         * 域名：如果是单点登录，就设置顶级域名
         * feng.net
         * https://www.feng.net/
         * https://mp.feng.net/
         */
        //cookie.setDomain(domain);
        cookie.setMaxAge(age);
        response.addCookie(cookie);
    }

    /*
    * 删除Cookie
    * */
    public static void deleteCookie(HttpServletResponse response, String key){
        setUpCookie(response,key,null,0);
    }

    /*
    * 获取Cookie值
    * */
    public static String getCookie(HttpServletRequest request, String key){
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if(key.equals(cookie.getName())){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
