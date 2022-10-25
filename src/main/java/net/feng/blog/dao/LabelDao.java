package net.feng.blog.dao;

import net.feng.blog.pojo.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface LabelDao extends JpaRepository<Label,String>,JpaSpecificationExecutor<Label> {
    Label findOneById(String id);

    Label findOneByName(String name);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE `tb_labels` set `count` = `count` + 1 where `name` = ?")
    int updateCountByName(String labelName);
}
