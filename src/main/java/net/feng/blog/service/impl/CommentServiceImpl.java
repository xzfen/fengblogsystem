package net.feng.blog.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import net.feng.blog.dao.ArticleNoContentDao;
import net.feng.blog.dao.CommentDao;
import net.feng.blog.pojo.ArticleNoContent;
import net.feng.blog.pojo.Comment;
import net.feng.blog.pojo.PageList;
import net.feng.blog.pojo.User;
import net.feng.blog.response.ResponseResult;
import net.feng.blog.service.ICommentService;
import net.feng.blog.service.IUserService;
import net.feng.blog.utils.Constants;
import net.feng.blog.utils.RedisUtil;
import net.feng.blog.utils.SnowFlakeIdWorker;
import net.feng.blog.utils.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
@Transactional
public class CommentServiceImpl implements ICommentService {
    @Autowired
    private SnowFlakeIdWorker idWorker;
    @Autowired
    private IUserService userService;
    @Autowired
    private ArticleNoContentDao articleNoContentDao;
    @Autowired
    private CommentDao commentDao;

    /**
     * 发表评论
     *
     * @param comment 评论
     * @return
     */
    @Override
    public ResponseResult postComment(Comment comment) {
        //检查用户是否有登录
        User user = userService.checkLoginUser();
        if (user == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        //检查内容
        String articleId = comment.getArticleId();
        if (TextUtils.isEmpty(articleId)) {
            return ResponseResult.FAILED("文章ID不可以为空.");
        }
        ArticleNoContent article = articleNoContentDao.findOneById(articleId);
        if (article == null) {
            return ResponseResult.FAILED("文章不存在.");
        }
        String content = comment.getContent();
        if (TextUtils.isEmpty(content)) {
            return ResponseResult.FAILED("评论内容不可以为空.");
        }
        //补全内容
        comment.setId(idWorker.nextId() + "");
        comment.setUpdateTime(new Date());
        comment.setCreateTime(new Date());
        comment.setUserAvatar(user.getAvatar());
        comment.setUserName(user.getUserName());
        comment.setUserId(user.getId());
        //保存入库
        commentDao.save(comment);
        //清除对应文章的评论缓存
        redisUtil.del(Constants.Comment.KEY_COMMENT_FIRST_PAGE_CACHE + comment.getArticleId());
        //返回结果
        return ResponseResult.SUCCESS("评论成功");
    }

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private Gson gson;
    /**
     * 获取文章的评论
     * 评论的排序策略：
     * 最基本的就按时间排序-->升序和降序-->先发表的在前面或者后发表的在前面
     * <p>
     * 置顶的：一定在前最前面
     * <p>
     * 后发表的：前单位时间内会排在前面，过了此单位时间，会按点赞量和发表时间进行排序
     *
     * @param articleId
     * @param page
     * @param size
     * @return
     */
    @Override
    public ResponseResult listCommentByArticleId(String articleId, int page, int size) {
        //参数检查
        if (page < Constants.Page.DEFAULT_PAGE){
            page = Constants.Page.DEFAULT_PAGE;
        }
        if (size < Constants.Page.DEFAULT_SIZE){
            size = Constants.Page.DEFAULT_SIZE;
        }
        //如果是第一页，那我们先从缓存中获取
        if(page == 1){
            String cacheJson = (String) redisUtil.get(Constants.Comment.KEY_COMMENT_FIRST_PAGE_CACHE + articleId);
            if (!TextUtils.isEmpty(cacheJson)) {
                PageList<Comment> result = gson.fromJson(cacheJson, new TypeToken<PageList<Comment>>(){}.getType());
                log.info("comment list from redis...");
                return ResponseResult.SUCCESS("评论列表获取成功.").setData(result);
            }
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "state", "createTime");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Comment> all = commentDao.findAllByArticleId(articleId, pageable);
        //把结果转成pageList
        PageList<Comment> result = new PageList<>();
        result.parsePage(all);
        //保存一份到缓存
        if(page == 1){
            redisUtil.set(Constants.Comment.KEY_COMMENT_FIRST_PAGE_CACHE + articleId,gson.toJson(result),Constants.TimeValueInSecond.MIN_5);
        }
        return ResponseResult.SUCCESS("评论列表获取成功.").setData(result);
    }

    @Override
    public ResponseResult deleteCommentById(String commentId) {
        //检查用户角色
        User user = userService.checkLoginUser();
        if (user == null) {
            return ResponseResult.ACCOUNT_NOT_LOGIN();
        }
        //把评论找出来，比对用户权限
        Comment comment = commentDao.findOneById(commentId);
        if (comment == null) {
            return ResponseResult.FAILED("评论不存在");
        }
        if(user.getId().equals(comment.getUserId()) || Constants.User.ROLE_ADMIN.equals(user.getRoles())) {
            //用户ID一样或者是管理员，可以删除评论
            commentDao.deleteById(commentId);
            //清除对应文章的评论缓存
            redisUtil.del(Constants.Comment.KEY_COMMENT_FIRST_PAGE_CACHE + comment.getArticleId());
            return ResponseResult.SUCCESS("评论删除成功。");
        } else {
            return ResponseResult.PERMISSION_DENIED();
        }
    }

    @Override
    public ResponseResult listComments(int page, int size) {
        //参数检查
        if (page < Constants.Page.DEFAULT_PAGE){
            page = Constants.Page.DEFAULT_PAGE;
        }
        if (size < Constants.Page.DEFAULT_SIZE){
            size = Constants.Page.DEFAULT_SIZE;
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Comment> all = commentDao.findAll(pageable);
        return ResponseResult.SUCCESS("评论列表获取成功.").setData(all);
    }

    @Override
    public ResponseResult topComment(String commentId) {
        Comment comment = commentDao.findOneById(commentId);
        if (comment == null) {
            return ResponseResult.FAILED("评论不存在");
        }
        String state = comment.getState();
        if (Constants.Comment.STATE_PUBLISH.equals(state)) {
            comment.setState(Constants.Comment.STATE_TOP);
            return ResponseResult.FAILED("置顶成功");
        }else if(Constants.Comment.STATE_TOP.equals(state)) {
            comment.setState(Constants.Comment.STATE_PUBLISH);
            return ResponseResult.FAILED("取消置顶");
        }else {
            return ResponseResult.FAILED("评论状态非法");
        }
    }

}
