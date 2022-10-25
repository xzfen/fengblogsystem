package net.feng.blog;

import net.feng.blog.pojo.User;
import net.feng.blog.utils.Constants;
import net.feng.blog.utils.SnowFlakeIdWorker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;

public class TestAddUser {
    public static void main(String[] args) {
        User user=new User();
        SnowFlakeIdWorker idWorker=new SnowFlakeIdWorker(0,0);
        BCryptPasswordEncoder passwordEncoder=new BCryptPasswordEncoder();
        
        user.setId(String.valueOf(idWorker.nextId()));
        user.setUserName("zhangsan");
        user.setRoles(Constants.User.ROLE_NORMAL);
        user.setAvatar(Constants.User.DEFAULT_AVATAR);
        user.setState(Constants.User.DEFAULT_STATE);
        user.setEmail("123456@qq.com");
        user.setSign("第一个放牛娃");
        String remoteAddr = "0:0:0:0:0:0:0:1";
        String localAddr = "0:0:0:0:0:0:0:1";
        user.setLoginIp(remoteAddr);
        user.setRegIp(remoteAddr);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        //对密码进行加密
        //源密码
        String password = "123456";
        //加密密码
        String encode = passwordEncoder.encode(password);
        user.setPassword(encode);

        //保存到数据库里
        //userDao.save(user);
    }
}
