package net.feng.blog.controller.admin;

import net.feng.blog.interceptor.CheckTooFrequentCommit;
import net.feng.blog.pojo.Article;
import net.feng.blog.response.ResponseResult;
import net.feng.blog.service.IArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@PreAuthorize("@permission.admin()")
@RestController
@RequestMapping("/admin/articles")
public class ArticleAdminApi {
    @Autowired
    private IArticleService articleService;
    /*
     * 添加文章
     * */
    @CheckTooFrequentCommit
    @PostMapping
    public ResponseResult postArticle(@RequestBody Article article){
        return articleService.postArticle(article);
    }
    /**
     * 如果是多用户，用户不可以删除，删除只是修改状态
     * 管理可以删除
     * <p>
     * 做成真的删除
     * 由于有外键链接到评论，因此这个接口目前不可用
     * @param articleId
     * @return
     */
    @DeleteMapping("/{articleId}")
    public ResponseResult deleteArticle(@PathVariable("articleId") String articleId){
        return articleService.deleteArticleById(articleId);
    }
    /**
     * 如果是多用户，用户不可以删除，删除只是修改状态
     * 这个是通过修改状态来标记删除
     *
     * @param articleId
     * @return
     */
    @DeleteMapping("/state/{articleId}")
    public ResponseResult deleteArticleByState(@PathVariable("articleId") String articleId){
        return articleService.deleteArticleByState(articleId);
    }

    /*
     * 修改文章
     * */
    @PutMapping("/{articleId}")
    public ResponseResult updateArticle(@PathVariable("articleId") String articleId, @RequestBody Article article){
        return articleService.updateArticle(articleId, article);
    }
    /*
     * 获取文章
     * */
    @GetMapping("/{articleId}")
    public ResponseResult getArticle(@PathVariable("articleId") String articleId){
        return articleService.getArticle(articleId);
    }
    /*
     * 获取文章列表
     * */
    @GetMapping("/list/{page}/{size}")
    public ResponseResult getArticleList(@PathVariable("page") int page,
                                         @PathVariable("size") int size,
                                         @RequestParam(value = "state", required = false) String state,
                                         @RequestParam(value = "keyword", required = false) String keyword,
                                         @RequestParam(value = "categoryId", required = false) String categoryId){
        return articleService.listArticles(page,size,state,keyword,categoryId);
    }
    /*
    * 文章置顶,修改文章状态
    * */
    @PutMapping("/top/{articleId}")
    public ResponseResult topArticle(@PathVariable("articleId") String articleId){
        return articleService.topArticle(articleId);
    }
}
