package net.feng.blog.controller.portal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/portal/app")
public class AppApi {
    /**
    * 给第三方扫描下载App用的接口
    * */
    @GetMapping("/{code}")
    public void downloadAppForThirdPartScan(@PathVariable("code") String code,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {
        //TODO:直接把最新的App写出来
    }
}
