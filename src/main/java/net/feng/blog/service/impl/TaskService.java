package net.feng.blog.service.impl;

import net.feng.blog.utils.EmailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    @Async
    public void sendEmailVerifyCode(String verifyCode, String emailAddress) throws Exception {
        EmailSender.sendRegisterVerifyCode(verifyCode, emailAddress);
    }
}
