package net.feng.blog.utils;

import io.jsonwebtoken.Claims;
import net.feng.blog.pojo.User;

import java.util.HashMap;
import java.util.Map;

public class ClaimsUtils {

    public static Map<String,Object> user2Claims(User user, String from){
        Map<String,Object> claims = new HashMap<>();
        claims.put("id",user.getId());
        claims.put("user_name",user.getUserName());
        claims.put("roles",user.getRoles());
        claims.put("avatar",user.getAvatar());
        claims.put("email",user.getEmail());
        claims.put("sign",user.getSign());
        claims.put("from",from);
        return claims;
    }

    public static User claims2User(Claims claims){
        User user = new User();
        user.setId((String) claims.get("id"));
        user.setUserName((String) claims.get("user_name"));
        user.setRoles((String) claims.get("roles"));
        user.setAvatar((String) claims.get("avatar"));
        user.setEmail((String) claims.get("email"));
        user.setSign((String) claims.get("sign"));

        return user;
    }

    public static String getFrom(Claims claims) {
        return (String) claims.get("from");
    }
}
