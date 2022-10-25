package net.feng.blog.dao;

import net.feng.blog.pojo.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CommentDao extends JpaRepository<Comment, String>, JpaSpecificationExecutor<Comment> {
    Comment findOneById(String commentId);
    void deleteAllByArticleId(String articleId);

    Page<Comment> findAllByArticleId(String articleId, Pageable pageable);
}
