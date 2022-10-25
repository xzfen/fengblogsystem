package net.feng.blog.dao;

import net.feng.blog.pojo.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenDao extends JpaRepository<RefreshToken,String>,JpaSpecificationExecutor<RefreshToken> {

    RefreshToken findOneByTokenKey(String tokenKey);

    RefreshToken findOneByMobileTokenKey(String mobileTokenKey);

    RefreshToken findOneByUserId(String userId);

    int deleteAllByTokenKey(String tokenKey);

    @Modifying
    @Query(nativeQuery = true, value = "update `tb_refresh_token` set `mobile_token_key` = '' where `mobile_token_key` = ?")
    void deleteMobileTokenKey(String tokenKey);

    @Modifying
    @Query(nativeQuery = true, value = "update `tb_refresh_token` set `token_key` = '' where `token_key` = ?")
    void deletePcTokenKey(String tokenKey);
}
