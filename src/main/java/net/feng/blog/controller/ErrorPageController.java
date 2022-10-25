package net.feng.blog.controller;

import net.feng.blog.response.ResponseResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/*
* 错误码转统一返回结果
* */
@RestController
public class ErrorPageController {

    @RequestMapping("/403")
    public ResponseResult page403(){
        return ResponseResult.ERROR_403();
    }
    @RequestMapping("/404")
    public ResponseResult page404(){
        return ResponseResult.ERROR_404();
    }
    @RequestMapping("/504")
    public ResponseResult page504(){
        return ResponseResult.ERROR_504();
    }
    @RequestMapping("/505")
    public ResponseResult page505(){
        return ResponseResult.ERROR_505();
    }
}
