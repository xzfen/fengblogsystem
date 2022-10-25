package net.feng.blog;

import net.feng.blog.utils.EmailSender;

import javax.mail.MessagingException;

public class TestEmailSender {
    public static void main(String[] args) throws MessagingException {
        EmailSender.subject("测试邮件发送")
                .from("Feng的博客系统")
                .text("这是发送的内容：ab12rf")
                .to("123456@qq.com")
                .send();
    }
}
