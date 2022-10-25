package net.feng.blog.service;

import net.feng.blog.response.ResponseResult;

public interface IWebInfoService {
    ResponseResult getWebInfoTitle();

    ResponseResult setWebInfoTitle(String title);

    ResponseResult getWebInfo();

    ResponseResult setWebInfo(String keywords, String description);

    ResponseResult getStatisticsInfo();

    void updateViewCount();
}
