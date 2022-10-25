package net.feng.blog.dao;

import net.feng.blog.pojo.ArticleNoContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ArticleNoContentDao extends JpaRepository<ArticleNoContent, String>, JpaSpecificationExecutor<ArticleNoContent> {
    ArticleNoContent findOneById(String id);

    @Query(nativeQuery = true, value = "select * from `tb_article` where `label` like ? and `id` != ? and (`state` = '1' or `state` = '3') limit ?")
    List<ArticleNoContent> listArticleByLikeLabel(String label, String orginalArticleId, int size);

    @Query(nativeQuery = true,value = "SELECT * FROM `tb_article` where `id` != ? and (`state` = '1' or `state` = '3') ORDER BY `create_time` DESC LIMIT ?")
    List<ArticleNoContent> listLastedArticleBySize(String articleId, int dxSize);
}
