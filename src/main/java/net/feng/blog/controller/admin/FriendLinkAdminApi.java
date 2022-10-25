package net.feng.blog.controller.admin;

import net.feng.blog.pojo.FriendLink;
import net.feng.blog.response.ResponseResult;
import net.feng.blog.service.IFriendLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("@permission.admin()")
@RestController
@RequestMapping("/admin/friendLinks")
public class FriendLinkAdminApi {

    @Autowired
    private IFriendLinkService friendLinkService;
    /**
     * 添加友情链接
     * */
    @PostMapping
    public ResponseResult addFriendLink(@RequestBody FriendLink friendLink){
        return friendLinkService.addFriendLink(friendLink);
    }
    /**
     * 删除友情链接
     * */
    @DeleteMapping("/{friendLindId}")
    public ResponseResult deleteFriendLink(@PathVariable("friendLindId") String friendLindId){
        return friendLinkService.deleteFriendLink(friendLindId);
    }
    /**
     * 修改友情链接
     * */
    @PutMapping("/{friendLindId}")
    public ResponseResult updateFriendLink(@PathVariable("friendLindId") String friendLindId,
                                            @RequestBody FriendLink friendLink){
        return friendLinkService.updateFriendLink(friendLindId,friendLink);
    }
    /**
     * 获取友情链接
     * */
    @GetMapping("/{friendLindId}")
    public ResponseResult getFriendLink(@PathVariable("friendLindId") String friendLindId){
        return friendLinkService.getFriendLink(friendLindId);
    }
    /**
     * 获取友情链接列表
     * */
    @GetMapping("/list")
    public ResponseResult getFriendLinkList(){
        return friendLinkService.listFriendLinks();
    }
}
