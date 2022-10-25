package net.feng.blog.service;

import net.feng.blog.response.ResponseResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface IImageService {
    ResponseResult uploadImage(MultipartFile file);

    void viewImage(HttpServletResponse response,String imageId) throws IOException;

    ResponseResult listImages(int page, int size);

    ResponseResult deleteById(String imageId);

    void createQrCode(String code, HttpServletResponse response);
}
