package net.feng.blog.dao;

import net.feng.blog.pojo.Looper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LoopDao extends JpaRepository<Looper, String>, JpaSpecificationExecutor<Looper> {
    Looper findOneById(String looperId);

    @Query(nativeQuery = true, value = "select * from `tb_looper` where `state` = ?")
    List<Looper> listLoopByState(String state);
}
