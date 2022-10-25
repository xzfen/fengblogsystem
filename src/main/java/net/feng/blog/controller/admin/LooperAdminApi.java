package net.feng.blog.controller.admin;

import net.feng.blog.pojo.Looper;
import net.feng.blog.response.ResponseResult;
import net.feng.blog.service.ILoopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/*
 * 管理中心-图片的API
 * */
@PreAuthorize("@permission.admin()")
@RestController
@RequestMapping("/admin/looper")
public class LooperAdminApi {
    @Autowired
    private ILoopService loopService;
    /*
     * 添加轮播图
     * */
    @PostMapping
    public ResponseResult addLooper(@RequestBody Looper looper){
        return loopService.addLoop(looper);
    }
    /*
     * 删除轮播图
     * */
    @DeleteMapping("/{looperId}")
    public ResponseResult deleteLooper(@PathVariable("looperId") String looperId){
        return loopService.deleteLoop(looperId);
    }
    /*
     * 修改轮播图
     * */
    @PutMapping("/{looperId}")
    public ResponseResult updateLooper(@PathVariable("looperId") String looperId, @RequestBody Looper looper) {
        return loopService.updateLoop(looperId,looper);
    }
    /*
     * 获取轮播图
     * */
    @GetMapping("/{looperId}")
    public ResponseResult getLooper(@PathVariable("looperId") String looperId){
        return loopService.getLoop(looperId);
    }
    /*
     * 获取轮播图列表
     * */
    @GetMapping("/list")
    public ResponseResult getLooperList(){
        return loopService.listLoops();
    }
}

