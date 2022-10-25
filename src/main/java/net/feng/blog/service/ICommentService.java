package net.feng.blog.service;

import net.feng.blog.pojo.Comment;
import net.feng.blog.response.ResponseResult;

public interface ICommentService {
    ResponseResult postComment(Comment comment);

    ResponseResult listCommentByArticleId(String articleId, int page, int size);

    ResponseResult deleteCommentById(String commentId);

    ResponseResult listComments(int page, int size);

    ResponseResult topComment(String commentId);
}
