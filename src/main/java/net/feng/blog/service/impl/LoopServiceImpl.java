package net.feng.blog.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.feng.blog.dao.LoopDao;
import net.feng.blog.pojo.Looper;
import net.feng.blog.pojo.User;
import net.feng.blog.response.ResponseResult;
import net.feng.blog.service.ILoopService;
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

@Slf4j
@Service
@Transactional
public class LoopServiceImpl implements ILoopService {

    @Autowired
    private SnowFlakeIdWorker idWorker;
    @Autowired
    private LoopDao loopDao;
    @Autowired
    private IUserService userService;

    @Override
    public ResponseResult addLoop(Looper looper) {
        //检查数据
        String title = looper.getTitle();
        if (TextUtils.isEmpty(title)) {
            return ResponseResult.FAILED("标题不可以为空.");
        }
        String imageUrl = looper.getImageUrl();
        if (TextUtils.isEmpty(imageUrl)) {
            return ResponseResult.FAILED("图片不可以为空.");
        }
        String targetUrl = looper.getTargetUrl();
        if (TextUtils.isEmpty(targetUrl)) {
            return ResponseResult.FAILED("跳转链接不可以为空.");
        }
        //补充数据
        looper.setId(idWorker.nextId() + "");
        looper.setCreateTime(new Date());
        looper.setUpdateTime(new Date());
        //保存数据
        loopDao.save(looper);
        //返回结果
        return ResponseResult.SUCCESS("轮播图添加成功.");
    }

    @Override
    public ResponseResult updateLoop(String looperId, Looper looper) {
        //找出来
        Looper loopFromDb = loopDao.findOneById(looperId);
        if (loopFromDb == null) {
            return ResponseResult.FAILED("轮播图不存在.");
        }
        //不可以为空的，要判空
        String title = looper.getTitle();
        if (!TextUtils.isEmpty(title)) {
            loopFromDb.setTitle(title);
        }
        String targetUrl = looper.getTargetUrl();
        if (!TextUtils.isEmpty(targetUrl)) {
            loopFromDb.setTargetUrl(targetUrl);
        }
        String imageUrl = looper.getImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            loopFromDb.setImageUrl(imageUrl);
        }
        if (!TextUtils.isEmpty(looper.getState())) {
            loopFromDb.setState(looper.getState());
        }
        loopFromDb.setOrder(looper.getOrder());
        loopFromDb.setUpdateTime(new Date());
        //可以为空的直接设置
        //保存回去
        loopDao.save(loopFromDb);
        return ResponseResult.SUCCESS("轮播图更新成功.");
    }

    @Override
    public ResponseResult deleteLoop(String looperId) {
        loopDao.deleteById(looperId);
        return ResponseResult.SUCCESS("删除成功.");
    }

    @Override
    public ResponseResult listLoops() {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        User user = userService.checkLoginUser();
        List<Looper> Loopers;
        if (user == null || !Constants.User.ROLE_ADMIN.equals(user.getRoles())) {
            //只能获取到正常的Looper
            Loopers = loopDao.listLoopByState("1");
        } else {
            //查询
            Loopers = loopDao.findAll(sort);
        }
        return ResponseResult.SUCCESS("获取轮播图列表成功.").setData(Loopers);
    }

    @Override
    public ResponseResult getLoop(String looperId) {
        Looper looper = loopDao.findOneById(looperId);
        if (looper == null) {
            return ResponseResult.FAILED("轮播图不存在.");
        }
        return ResponseResult.SUCCESS("轮播图获取成功.").setData(looper);
    }
}
