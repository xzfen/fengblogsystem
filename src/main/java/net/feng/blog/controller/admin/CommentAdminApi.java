package net.feng.blog.controller.admin;

import net.feng.blog.response.ResponseResult;
import net.feng.blog.service.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("@permission.admin()")
@RestController
@RequestMapping("/admin/comments")
public class CommentAdminApi {
    @Autowired
    private ICommentService commentService;
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
    @GetMapping("/list")
    public ResponseResult getCommentList(@RequestParam("page") int page, @RequestParam("size") int size){
        return commentService.listComments(page,size);
    }
    /*
     * 评论置顶,修改评论状态
     * */
    @PutMapping("/top/{commentId}")
    public ResponseResult topComment(@PathVariable("commentId") String commentId){
        return commentService.topComment(commentId);
    }
}
