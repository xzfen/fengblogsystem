package net.feng.blog.controller.admin;

import net.feng.blog.response.ResponseResult;
import net.feng.blog.service.IWebInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@PreAuthorize("@permission.admin()")
@RestController
@RequestMapping("/admin/webInfo")
public class WebInfoAdminApi {
    @Autowired
    IWebInfoService webInfoService;
    /*
    * 获取网站标题
    * */
    @GetMapping("/title")
    public ResponseResult getWebInfoTitle(){
        return webInfoService.getWebInfoTitle();
    }
    /*
     * 修改网站标题
     * */
    @PutMapping("/title")
    public ResponseResult putWebInfoTitle(@RequestParam("title") String title){
        return webInfoService.setWebInfoTitle(title);
    }
    /*
     * 获取网站信息
     * */
    @GetMapping("/seo")
    public ResponseResult getWebInfo(){
        return webInfoService.getWebInfo();
    }
    /*
     * 修改网站信息
     * */
    @PutMapping("/seo")
    public ResponseResult putWebInfo(@RequestParam("keywords") String keywords,
                                        @RequestParam("description")String description){
        return webInfoService.setWebInfo(keywords,description);
    }
    /*
     * 获取网站统计信息
     * */
    @GetMapping("/statistics")
    public ResponseResult getStatisticsInfo(){
        return webInfoService.getStatisticsInfo();
    }
}
