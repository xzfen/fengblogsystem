package net.feng.blog.controller.portal;

import net.feng.blog.service.IImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/portal/images")
public class ImagePortalApi {
    @Autowired
    private IImageService imageService;

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
     * 获取二维码图片
     * */
    @GetMapping("/qr-code/{code}")
    public void getQrCodeImage(@PathVariable("code") String code, HttpServletResponse response) {
        imageService.createQrCode(code,response);
    }
}
