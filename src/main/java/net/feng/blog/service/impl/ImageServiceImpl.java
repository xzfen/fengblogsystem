package net.feng.blog.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.feng.blog.dao.ImageDao;
import net.feng.blog.pojo.Image;
import net.feng.blog.pojo.User;
import net.feng.blog.response.ResponseResult;
import net.feng.blog.service.IImageService;
import net.feng.blog.service.IUserService;
import net.feng.blog.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@Service
@Transactional
public class ImageServiceImpl implements IImageService {

    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd");

    @Autowired
    private SnowFlakeIdWorker idWorker;
    @Autowired
    ImageDao imageDao;
    @Autowired
    private IUserService userService;

    @Value("${sob.blog.image.save-path}")
    public String imagePath;
    @Value("${sob.blog.image.max-size}")
    public long maxSize;

    /**
     * 上传的路径：可以配置，在配置文件里配置
     * 上传的内容，命名-->可以用ID,-->每天一个文件夹保存
     * 限制文件大小，通过配置文件配置
     * 保存记录到数据库里
     * ID｜存储路径｜url｜原名称｜用户ID｜状态｜创建日期｜更新日期
     *
     * @param file
     * @return
     */
    @Override
    public ResponseResult uploadImage(MultipartFile file) {
        User user = userService.checkLoginUser();
        if (user == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        //判断是否有文件
        if (file == null) {
            return ResponseResult.FAILED("图片不可以为空.");
        }
        //判断文件类型，我们只支持图片上传，比如说：png，jpg，gif
        String contentType = file.getContentType();
        log.info("contentType == > " + contentType);
        if (TextUtils.isEmpty(contentType)) {
            return ResponseResult.FAILED("图片格式错误.");
        }
        //获取相关数据，比如说文件类型，文件名称
        String originalFilename = file.getOriginalFilename();
        log.info("originalFilename == > " + originalFilename);
        String type = getType(contentType, originalFilename);
        if (type == null) {
            return ResponseResult.FAILED("不支持此图片类型");
        }
        //限制文件的大小
        long size = file.getSize();
        log.info("maxSize === > " + maxSize + "  size ==> " + size);
        if (size > maxSize) {
            return ResponseResult.FAILED("图片最大仅支持" + (maxSize / 1024 / 1024) + "Mb");
        }
        //创建图片的保存目录
        //规则：配置目录/日期/类型/ID.类型
        long currentMillions = System.currentTimeMillis();
        String currentDay = simpleDateFormat.format(currentMillions);
        log.info("current day == > " + currentDay);
        String dayPath = imagePath + File.separator + currentDay;
        File dayPathFile = new File(dayPath);
        //判断日期文件夹是否存在//2020_06_26
        if (!dayPathFile.exists()) {
            dayPathFile.mkdirs();
        }
        String targetName = String.valueOf(idWorker.nextId());
        String targetPath = dayPath +
                File.separator + type + File.separator + targetName + "." + type;
        File targetFile = new File(targetPath);
        //判断类型文件夹是否存在//gif
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        try {
            if (!targetFile.exists()) {
                targetFile.createNewFile();
            }
            log.info("targetFile == > " + targetFile);
            //保存文件
            file.transferTo(targetFile);
            //返回结果：包含这个图片的名称和访问路径
            //第一个是访问路径 -- > 得对应着解析来
            Map<String, String> result = new HashMap<>();
            String resultPath = currentMillions + "_" + targetName + "." + type;
            result.put("id", resultPath);
            //第二个是名称--->alt="图片描述",如果不写，前端可以使用名称作为这个描述
            result.put("name", originalFilename);
            Image image = new Image();
            image.setContentType(contentType);
            image.setId(targetName);
            image.setCreateTime(new Date());
            image.setUpdateTime(new Date());
            image.setPath(targetFile.getPath());
            image.setName(originalFilename);
            image.setUrl(resultPath);
            image.setState("1");

            image.setUserId(user.getId());
            //记录文件
            //保存记录到数据里
            imageDao.save(image);
            //返回结果
            return ResponseResult.SUCCESS("文件上传成功").setData(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseResult.FAILED("图片上传失败，请稍后重试");
    }

    private String getType(String contentType, String name) {
        String type = null;
        if (Constants.ImageType.TYPE_PNG_WITH_PREFIX.equals(contentType)
                && name.endsWith(Constants.ImageType.TYPE_PNG)) {
            type = Constants.ImageType.TYPE_PNG;
        }else if (Constants.ImageType.TYPE_GIF_WITH_PREFIX.equals(contentType)
                && name.endsWith(Constants.ImageType.TYPE_GIF)) {
            type = Constants.ImageType.TYPE_GIF;
        }else if (Constants.ImageType.TYPE_JPG_WITH_PREFIX.equals(contentType)
                && name.endsWith(Constants.ImageType.TYPE_JPG)) {
            type = Constants.ImageType.TYPE_JPG;
        }else if(Constants.ImageType.TYPE_JPEG_WITH_PREFIX.equals(contentType)
                && name.endsWith(Constants.ImageType.TYPE_JPEG)) {
            type = Constants.ImageType.TYPE_JPG;
        }else if(Constants.ImageType.TYPE_JPEG_WITH_PREFIX.equals(contentType)
                && name.endsWith(Constants.ImageType.TYPE_JPG)) {
            type = Constants.ImageType.TYPE_JPG;
        }else if(Constants.ImageType.TYPE_JPG_WITH_PREFIX.equals(contentType)
                && name.endsWith(Constants.ImageType.TYPE_JPEG)) {
            type = Constants.ImageType.TYPE_JPG;
        }
        return type;
    }

    @Override
    public void viewImage(HttpServletResponse response, String imageId) throws IOException {
        //配置的目录已知
        //根据尺寸来动态返回图片给前端
        //好处：减少带宽占用，传输速度快
        //缺点：消耗后台的CPU资源
        //推荐做法：上传上来的时候，把图片复制成三个尺寸：大，中，小
        //根据尺寸范围，返回结果即可
        //需要日期
        String[] paths = imageId.split("[_,.]");//使用"_"和"."分隔符将

        String dayValue = paths[0];
        String format = simpleDateFormat.format(Long.parseLong(dayValue));
        log.info("viewImage  format == > " + format);
        //ID
        String name = paths[1];
        //需要类型
        String type = paths[2];
        //使用日期的时间戳_ID.类型
        String targetPath = imagePath + File.separator + format + File.separator +
                type +
                File.separator + name +
                "." +
                type;
        log.info("get image target path === > " + targetPath);
        File file = new File(targetPath);
        OutputStream writer = null;
        FileInputStream fos = null;
        try {
            response.setContentType("image/png");
            writer = response.getOutputStream();
            //读取
            fos = new FileInputStream(file);
            byte[] buff = new byte[1024];
            int len;
            while ((len = fos.read(buff)) != -1) {
                writer.write(buff, 0, len);
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                fos.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
    }

    @Override
    public ResponseResult listImages(int page, int size) {
        //处理page,size
        if (page < Constants.Page.DEFAULT_PAGE) {
            page = Constants.Page.DEFAULT_PAGE;
        }
        if (size < Constants.Page.DEFAULT_SIZE) {
            size = Constants.Page.DEFAULT_SIZE;
        }
        User user = userService.checkLoginUser();
        if (user == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        //创建分页条件
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        //查询
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        //返回结果
        final String userId = user.getId();
        Page<Image> all = imageDao.findAll(new Specification<Image>() {
            @Override
            public Predicate toPredicate(Root<Image> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                //根据用户ID
                Predicate userIdPre = cb.equal(root.get("userId").as(String.class), userId);
                //根据状态
                Predicate statePre = cb.equal(root.get("state").as(String.class), "1");
                return cb.and(userIdPre, statePre);
            }
        }, pageable);
        return ResponseResult.SUCCESS("获取图片列表成功.").setData(all);
    }

    /**
     * 删除图片，
     * 只改变状态
     *
     * @param imageId
     * @return
     */
    @Override
    public ResponseResult deleteById(String imageId) {
        int result = imageDao.deleteImageByUpdateState(imageId);
        if (result > 0) {
            return ResponseResult.SUCCESS("删除成功.");
        }
        return ResponseResult.FAILED("图片不存在.");
    }

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void createQrCode(String code, HttpServletResponse response) {
        //检查二维码是否已经过期
        String loginState = (String) redisUtil.get(Constants.User.KEY_PC_LOGIN_ID + code);
        if (TextUtils.isEmpty(loginState)) {
            //TODO:返回一张图片显示已经过期
            return;
        }
        //生成二维码
        //二维码的内容是什么？
        //1、可以简单的是一个code，也就是传进来的这个code
        //这种情况，如果是用我们自己的App来扫描，可以识别并解析，请求对应的接口
        //如果是第三方的App，可以识别，但是没有用，只能显示这个code
        //2、我们应该做成一个App的下载地址+code，如果是我们自己的App扫到，切割后面的内容拿到code进行解析，请求对应接口
        //如果是第三方的App扫描，它是个网址，就会访问下载App的地址，去下载我们的App
        String content = Constants.APP_DOWNLOAD_PATH + "===" + code;
        log.info("qr-code content ==> " + content);
        byte[] result = QrCodeUtils.encodeQRCode(content);
        response.setContentType(QrCodeUtils.RESPONSE_CONTENT_TYPE);
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            outputStream.write(result);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
