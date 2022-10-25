package net.feng.blog;

import net.feng.blog.utils.JwtUtil;

import java.util.HashMap;
import java.util.Map;
/*
* 生成Token
* */
public class TestCreateToken {
    public static void main(String[] args) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", "722250648279580673");
        claims.put("userName", "测试用户");
        claims.put("role", "role_normal");
        claims.put("avatar", "");
        claims.put("email", "123456@qq.com");
        String token = JwtUtil.createToken(claims,60*1000);//有效期为1分钟
        System.out.println(token);
    }
}
