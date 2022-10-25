package net.feng.blog.dao;

import net.feng.blog.pojo.FriendLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FriendLinkDao extends JpaRepository<FriendLink, String>, JpaSpecificationExecutor<FriendLink> {
    FriendLink findOneById(String id);
    int deleteAllById(String id);

    @Query(nativeQuery = true, value = "select * from `tb_friends` where `state` = ?")
    List<FriendLink> listFriendLinksByState(String state);
}
