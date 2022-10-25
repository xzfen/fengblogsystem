package net.feng.blog.service;

import net.feng.blog.pojo.Category;
import net.feng.blog.response.ResponseResult;

public interface ICategoryService {
    ResponseResult addCategory(Category category);

    ResponseResult getCategory(String categoryId);

    ResponseResult listCategories();

    ResponseResult updateCategory(String categoryId, Category category);

    ResponseResult deleteCategory(String categoryId);
}
