package net.feng.blog.service;

import net.feng.blog.pojo.FriendLink;
import net.feng.blog.response.ResponseResult;

public interface IFriendLinkService {
    ResponseResult addFriendLink(FriendLink friendLink);

    ResponseResult listFriendLinks();

    ResponseResult getFriendLink(String friendLindId);

    ResponseResult deleteFriendLink(String friendLindId);

    ResponseResult updateFriendLink(String friendLindId, FriendLink friendLink);
}
