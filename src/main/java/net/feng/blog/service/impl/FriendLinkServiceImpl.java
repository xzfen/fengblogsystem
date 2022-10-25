package net.feng.blog.service.impl;

import net.feng.blog.dao.FriendLinkDao;
import net.feng.blog.pojo.FriendLink;
import net.feng.blog.pojo.User;
import net.feng.blog.response.ResponseResult;
import net.feng.blog.service.IFriendLinkService;
import net.feng.blog.service.IUserService;
import net.feng.blog.utils.Constants;
import net.feng.blog.utils.SnowFlakeIdWorker;
import net.feng.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional
@Service
public class FriendLinkServiceImpl implements IFriendLinkService {

    @Autowired
    private SnowFlakeIdWorker idWorker;
    @Autowired
    private FriendLinkDao friendLinkDao;
    @Autowired
    IUserService userService;

    /**
     * 添加友情链接
     * */
    @Override
    public ResponseResult addFriendLink(FriendLink friendLink) {
        //判断数据
        String url = friendLink.getUrl();
        if(TextUtils.isEmpty(url)){
            return ResponseResult.FAILED("链接URL不可以为空");
        }
        String logo = friendLink.getLogo();
        if (TextUtils.isEmpty(logo)) {
            return ResponseResult.FAILED("LOGO不可以为空");
        }
        String name = friendLink.getName();
        if (TextUtils.isEmpty(name)) {
            return ResponseResult.FAILED("链接网站名不可以为空");
        }
        //补全数据
        friendLink.setId(idWorker.nextId() + "");
        friendLink.setOrder(0);
        friendLink.setState("1");
        friendLink.setUpdateTime(new Date());
        friendLink.setCreateTime(new Date());
        //保存数据
        friendLinkDao.save(friendLink);
        //返回结果
        return ResponseResult.SUCCESS("友情链接添加成功");
    }

    @Override
    public ResponseResult listFriendLinks() {

        //创建条件
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime", "order");
        //判断用户角色，普通用户/未登录用户，只能获取到正常的category
        //管理员账号，可以拿到所有的分类
        User user = userService.checkLoginUser();
        List<FriendLink> friendLinks;
        if(user == null || Constants.User.ROLE_NORMAL.equals(user.getRoles())){
            //只能获取到正常的category
            friendLinks = friendLinkDao.listFriendLinksByState("1");
        }else {
            //查询
            friendLinks = friendLinkDao.findAll(sort);

        }

        //返回条件
        return ResponseResult.SUCCESS("获取友情链接列表成功").setData(friendLinks);
    }

    @Override
    public ResponseResult getFriendLink(String friendLindId) {
        FriendLink friendLink = friendLinkDao.findOneById(friendLindId);
        if (friendLink == null) {
            return ResponseResult.FAILED("该友情链接不存在");
        }
        return ResponseResult.SUCCESS("获取成功").setData(friendLink);
    }

    @Override
    public ResponseResult deleteFriendLink(String friendLindId) {
        //直接删除
        int result = friendLinkDao.deleteAllById(friendLindId);
        //返回结果
        if (result == 0) {
            return ResponseResult.FAILED("该友情链接不存在");
        }
        return ResponseResult.SUCCESS("删除成功");
    }

    /**
     * 更新内容有什么：
     * Logo
     * 网站名称
     * url
     * order
     * @param friendLindId
     * @param friendLink
     * @return
     * */
    @Override
    public ResponseResult updateFriendLink(String friendLindId, FriendLink friendLink) {
        //第一步，找出来
        FriendLink friendLinkFromDb = friendLinkDao.findOneById(friendLindId);
        if (friendLinkFromDb == null) {
            return ResponseResult.FAILED("更新失败");
        }
        //第二步是对内容进行判断，有些字段是不可以为空的
        String logo = friendLink.getLogo();
        if (!TextUtils.isEmpty(logo)) {
            friendLinkFromDb.setLogo(logo);
        }
        String name = friendLink.getName();
        if (!TextUtils.isEmpty(name)) {
            friendLinkFromDb.setName(name);
        }
        String url = friendLink.getUrl();
        if (!TextUtils.isEmpty(url)) {
            friendLinkFromDb.setUrl(url);
        }
        friendLinkFromDb.setOrder(friendLink.getOrder());
        friendLinkFromDb.setUpdateTime(new Date());
        //保存数据
        friendLinkDao.save(friendLinkFromDb);
        //返回结果
        return ResponseResult.SUCCESS("更新成功");
    }
}
