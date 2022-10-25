package net.feng.blog.dao;

import net.feng.blog.pojo.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SettingDao extends JpaRepository<Setting,String>, JpaSpecificationExecutor<String> {
    //返回对象
    Setting findOneByKey(String key);
    //返回对象的集合
    //List<Setting> findByKey(String);
}
