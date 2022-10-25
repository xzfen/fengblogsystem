package net.feng.blog.controller;


import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import lombok.extern.slf4j.Slf4j;
import net.feng.blog.dao.LabelDao;
import net.feng.blog.pojo.House;
import net.feng.blog.pojo.Label;
import net.feng.blog.pojo.TestUser;
import net.feng.blog.response.ResponseResult;
import net.feng.blog.utils.Constants;
import net.feng.blog.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/*@ComponentScan(value = "net.feng.blog",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION,
                classes = Controller.class
        )
)*/
@PreAuthorize("@permission.admin()")//测试模块只提供管理员测试用，其他用户无法使用
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    /*
    * JPA Test
    * */
    private LabelDao labelDao;

    @GetMapping("/helloworld")
    public ResponseResult helloWorld(){
        log.info("helloworld...");
        String caphchacontent = (String) redisUtil.get(Constants.User.KEY_CAPTCHA_CONTENT + "123456");
        log.info("caphchacontent==> "+caphchacontent);
        return ResponseResult.SUCCESS().setData("helloworld");
    }

    @GetMapping("/hellouser")
    public ResponseResult helloUser(){
        TestUser user=new TestUser(1,"特朗普","male");
        House house=new House("白宫","华盛顿大区");
        user.setHouse(house);
        return ResponseResult.SUCCESS().setData(user);
    }

    @PostMapping("/testlogin")
    public ResponseResult testLogin(@RequestBody TestUser testUser){
        log.info("testUser name==>"+testUser.getName());
        return ResponseResult.SUCCESS("登录成功");
    }
    /*
    * 日期类型参数传递
    * */
    @RequestMapping("/dataParam")
    public ResponseResult testDataParam(Date date,
                                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date date1,
                                        @DateTimeFormat(pattern = "yyyy MM dd HH:mm:ss")Date date2){
        log.info("参数传递 date==》 "+date);
        log.info("参数传递 date1(yyyy-MM-dd)==》 "+date1);
        log.info("参数传递 date1(yyyy MM dd HH:mm:ss)==》 "+date2);

        /*
        * JPA Test
        * */
        Label label=new Label();
        labelDao.save(label);
        //labelDao.findById(label.getId());
        //第一个参数是排序方式：升序or降序
        //第二个参数是字段名，按哪个字段排序
        //Sort sort=new Sort(Sort.Direction.DESC,"createTime");
        //第一个参数是第几页，从0开始
        //第二个参数是每页有多少个item
        //第三个参数是排序方式
        //Pageable pageable=PageRequest.of(0,10,sort);
        //条件查询：equal是完全匹配，like是部分匹配,and是将几个条件查询合并
        //第一个参数是按哪个字段查询
        //第二个参数和该字段匹配的条件
        List<Label> labelList = labelDao.findAll(new Specification<Label>() {
            @Override
            public Predicate toPredicate(Root<Label> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Predicate count = cb.equal(root.get("count").as(Integer.class), "label.count");
                Predicate name = cb.like(root.get("name").as(String.class), "%" + "label.name的一部分" + "%");
                Predicate and = cb.and(count, name);
                return and;
            }
        });


        return ResponseResult.SUCCESS("日期测试成功");
    }

    @Autowired
    private RedisUtil redisUtil;

    //http://localhost:2020/test/captcha
    @RequestMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 设置请求头为输出图片类型
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        // 三个参数分别为宽、高、位数
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 5);
        // 设置字体
        // specCaptcha.setFont(new Font("Verdana", Font.PLAIN, 32));  // 有默认字体，可以不用设置
        specCaptcha.setFont(Captcha.FONT_1);
        // 设置类型，纯数字、纯字母、字母数字混合
        //specCaptcha.setCharType(Captcha.TYPE_ONLY_NUMBER);
        specCaptcha.setCharType(Captcha.TYPE_DEFAULT);

        String content = specCaptcha.text().toLowerCase();
        log.info("captcha content == > " + content);
        // 验证码存入session
        //request.getSession().setAttribute("captcha", content);
        //保存到redis里,10分钟有效
        redisUtil.set(Constants.User.KEY_CAPTCHA_CONTENT+"123456",content,60*10);

        // 输出图片流
        specCaptcha.out(response.getOutputStream());
    }
}
