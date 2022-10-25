package net.feng.blog.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.feng.blog.dao.CategoryDao;
import net.feng.blog.pojo.Category;
import net.feng.blog.pojo.User;
import net.feng.blog.response.ResponseResult;
import net.feng.blog.service.ICategoryService;
import net.feng.blog.service.IUserService;
import net.feng.blog.utils.Constants;
import net.feng.blog.utils.SnowFlakeIdWorker;
import net.feng.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Transactional
@Service
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    private SnowFlakeIdWorker idWorker;
    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    IUserService userService;

    @Override
    public ResponseResult addCategory(Category category) {
        //先检查数据
        // 必须的数据有：
        //分类名称、分类的pinyin、顺序、描述
        if (TextUtils.isEmpty(category.getName())) {
            return ResponseResult.FAILED("分类名称不可以为空.");
        }
        //检查分类是否存在
        Category categoryFromDb = categoryDao.findOneByName(category.getName());
        if (categoryFromDb != null) {
            return ResponseResult.FAILED("相同分类已经存在！");
        }
        if (TextUtils.isEmpty(category.getPinyin())) {
            return ResponseResult.FAILED("分类拼音不可以为空.");
        }
        if (TextUtils.isEmpty(category.getDescription())) {
            return ResponseResult.FAILED("分类描述不可以为空.");
        }
        //补全数据
        category.setId(idWorker.nextId() + "");
        category.setStatus("1");
        category.setCreateTime(new Date());
        category.setUpdateTime(new Date());
        //保存数据
        categoryDao.save(category);
        //返回结果
        return ResponseResult.SUCCESS("添加分类成功");
    }

    @Override
    public ResponseResult getCategory(String categoryId) {
        Category category = categoryDao.findOneById(categoryId);
        if (category == null) {
            return ResponseResult.FAILED("分类不存在.");
        }
        return ResponseResult.SUCCESS("获取分类成功.").setData(category);
    }

    @Override
    public ResponseResult listCategories() {

        //创建条件
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime", "order");
        //判断用户角色，普通用户/未登录用户，只能获取到正常的category
        //管理员账号，可以拿到所有的分类
        User user = userService.checkLoginUser();
        List<Category> categories;
        if(user == null || Constants.User.ROLE_NORMAL.equals(user.getRoles())){
            //只能获取到正常的category
            categories = categoryDao.listCategoryByStatus("1");
        }else {
            //查询
            categories = categoryDao.findAll(sort);

        }
        //返回结果
        return ResponseResult.SUCCESS("获取分类列表成功.").setData(categories);
    }

    @Override
    public ResponseResult updateCategory(String categoryId, Category category) {

        //第一步，找出来
        Category categoryFromDb = categoryDao.findOneById(categoryId);
        if (categoryFromDb == null) {
            return ResponseResult.FAILED("分类不存在.");
        }
        //第二步是对内容进行判断，有些字段是不可以为空的
        String name = category.getName();
        if (!TextUtils.isEmpty(name)) {
            categoryFromDb.setName(name);
        }
        String pinyin = category.getPinyin();
        if (!TextUtils.isEmpty(pinyin)) {
            categoryFromDb.setPinyin(pinyin);
        }
        String description = category.getDescription();
        if (!TextUtils.isEmpty(description)) {
            categoryFromDb.setDescription(description);
        }
        categoryFromDb.setOrder(category.getOrder());
        categoryFromDb.setUpdateTime(new Date());
        //第三步保存数据
        categoryDao.save(categoryFromDb);
        //第四部返回数据
        return ResponseResult.SUCCESS("更新成功.");
    }

    @Override
    public ResponseResult deleteCategory(String categoryId) {
        int result = categoryDao.deleteCategoryByUpdateState(categoryId);
        if (result == 0) {
            return ResponseResult.FAILED("该分类不存在.");
        }
        return ResponseResult.SUCCESS("删除分类成功.");
    }
}
