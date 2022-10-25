package net.feng.blog.utils;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class TextUtils {
    public static boolean isEmpty(String text){
        return text==null || text.length()==0;
    }

    public static boolean isEmail(String email){
        if (null==email || "".equals(email)) return false;
        //Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配
        Pattern p =  Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配
        Matcher m = p.matcher(email);
        return m.matches();
    }

    public static String getDomain(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String servletPath = request.getServletPath();
        String originalDomain = requestURL.toString().replace(servletPath, "");
        log.info("TextUtils.getDomain requestURL== > " + requestURL);
        log.info("TextUtils.getDomain servletPath== > " + servletPath);
        log.info("TextUtils.getDomain originalDomain== > " + originalDomain);
        return originalDomain;
    }
}
