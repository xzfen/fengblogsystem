package net.feng.blog.controller.portal;

import net.feng.blog.interceptor.CheckTooFrequentCommit;
import net.feng.blog.pojo.Comment;
import net.feng.blog.response.ResponseResult;
import net.feng.blog.service.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal/comments")
public class CommentPortalApi {

    @Autowired
    private ICommentService commentService;
    /*
     * 添加评论
     * */
    @CheckTooFrequentCommit
    @PostMapping
    public ResponseResult postComment(@RequestBody Comment comment){
        return commentService.postComment(comment);
    }
    /*
     * 删除评论
     * */
    @DeleteMapping("/{commentId}")
    public ResponseResult deleteComment(@PathVariable("commentId") String commentId){
        return commentService.deleteCommentById(commentId);
    }
    /*
     * 获取评论列表
     * */
    @GetMapping("/list/{articleId}/{page}/{size}")
    public ResponseResult listComments(@PathVariable("articleId") String articleId, @PathVariable("page") int page,@PathVariable("size") int size) {
        return commentService.listCommentByArticleId(articleId, page, size);
    }

}
