package net.feng.blog;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.feng.blog.utils.CountDownLatchManager;
import net.feng.blog.utils.RedisUtil;
import net.feng.blog.utils.SnowFlakeIdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Random;

@Slf4j
@SpringBootApplication
public class BlogApplication {
    public static void main(String[] args) {
        log.info("SpringApplication.run...");
        SpringApplication.run(BlogApplication.class,args);
    }

    @Bean
    public SnowFlakeIdWorker createIdWorker(){
        return new SnowFlakeIdWorker(0,0);
    }

    @Bean
    public BCryptPasswordEncoder createPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RedisUtil createRedisUtil(){
        return new RedisUtil();
    }

    @Bean
    public Random createRandom(){
        return new Random();
    }

    @Bean
    public Gson createGson(){
        return new Gson();
    }

    @Bean
    public CountDownLatchManager createCountDownLatchManager() {
        return new CountDownLatchManager();
    }
}
