package net.feng.blog.controller.admin;

import net.feng.blog.response.ResponseResult;
import net.feng.blog.service.IImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
* 管理中心-图片的API
* */
@PreAuthorize("@permission.admin()")
@RestController
@RequestMapping("/admin/images")
public class ImageAdminApi {
    @Autowired
    private IImageService imageService;
    /**
     * 关于图片（文件）上传
     * 一般来说，现在比较常用的是对象存储--->很简单，看文档就可以学会了
     * 使用 Nginx + fastDFS == > fastDFS -- > 处理文件上传， Nginx -- > 负责处理文件访问
     *
     * @param file
     * @return
     */
    @PostMapping
    public ResponseResult uploadImage(@RequestParam("file") MultipartFile file) {
        return imageService.uploadImage(file);
    }
    /**
    * 删除图片
    * */
    @DeleteMapping("/{imageId}")
    public ResponseResult deleteImage(@PathVariable("imageId") String imageId) {
        return imageService.deleteById(imageId);
    }

    /**
     * 获取图片
     * */
    @GetMapping("/{imageId}")
    public void getImage(HttpServletResponse response, @PathVariable("imageId") String imageId){
        try {
            imageService.viewImage(response, imageId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
    * 获取图片列表
    * */
    @GetMapping("/list/{page}/{size}")
    public ResponseResult listImages(@PathVariable("page") int page, @PathVariable("size") int size){
        return imageService.listImages(page, size);
    }
}
