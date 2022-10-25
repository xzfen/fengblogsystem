package net.feng.blog;

import org.springframework.util.DigestUtils;

public class TestCreateJwtMd5Value {
    public static void main(String[] args) {
        String jwkKeyMd5Str = DigestUtils.md5DigestAsHex("sob_blog_system_-=".getBytes());
        System.out.println(jwkKeyMd5Str);
    }
}
