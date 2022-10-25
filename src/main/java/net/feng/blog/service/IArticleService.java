package net.feng.blog.service;

import net.feng.blog.pojo.Article;
import net.feng.blog.response.ResponseResult;

public interface IArticleService {
    ResponseResult postArticle(Article article);

    ResponseResult listArticles(int page, int size, String state, String keyword, String categoryId);

    ResponseResult getArticle(String articleId);

    ResponseResult updateArticle(String articleId, Article article);

    ResponseResult deleteArticleById(String articleId);

    ResponseResult deleteArticleByState(String articleId);

    ResponseResult topArticle(String articleId);

    ResponseResult listTopArticles();

    ResponseResult listRecommendArticle(String articleId, int size);

    ResponseResult listArticlesByLabel(String label, int page, int size);

    ResponseResult listLabels(int size);

    ResponseResult doSearchArticle(String keyword, int page, int size);
}
