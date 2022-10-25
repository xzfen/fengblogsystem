package net.feng.blog.dao;

import net.feng.blog.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserDao extends JpaRepository<User,String>, JpaSpecificationExecutor<User> {
    User findOneByUserName(String userName);

    User findOneById(String Id);

    User findOneByEmail(String email);

    @Modifying
    @Query(nativeQuery = true, value = "update tb_user set `password` = ? where `email` = ?")
    int updatePasswordByEmail(String encode, String email);

    @Modifying
    @Query(nativeQuery = true, value = "update `tb_user` set `email` = ? where `id` = ?")
    int updateEmailById(String email, String id);
}
