package net.feng.blog.controller.portal;

import net.feng.blog.response.ResponseResult;
import net.feng.blog.service.IArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/search")
public class SearchPortalApi {
    @Autowired
    private IArticleService articleService;
    @GetMapping
    public ResponseResult doSearch(@RequestParam("keyword") String keyword,
                                   @RequestParam("page") int page,
                                   @RequestParam("size") int size){
        return articleService.doSearchArticle(keyword,page,size);
    }
}
