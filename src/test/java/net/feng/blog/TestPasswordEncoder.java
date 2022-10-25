package net.feng.blog;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPasswordEncoder {
    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder=new BCryptPasswordEncoder();
        //String encode = passwordEncoder.encode("123456");
        //数据库密文是这些里面的其中一个
        //$2a$10$nAheeM9QnFpINjVYlxg5suOC6olvO82rnpwceXvHH10.gDCVuhO9K
        //$2a$10$GLeDPgsR3PbLYDB0a5cVhew4.5oYYmOi4XFlpSIlqWq56AuNBpsYq
        //System.out.println("encode==> "+encode);
        //验证登录流程
        //1.用户提交密码：123456
        //2.跟数据库中的密文进行比较，如何判断提交的密码是否正确
        String originalPassword="123456";
        boolean matches = passwordEncoder.matches(originalPassword, "$2a$10$GLeDPgsR3PbLYDB0a5cVhew4.5oYYmOi4XFlpSIlqWq56AuNBpsYq");
        System.out.println("密码是否正确==> "+matches);
    }
}
