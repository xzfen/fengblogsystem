package net.feng.blog.service;

import net.feng.blog.pojo.Looper;
import net.feng.blog.response.ResponseResult;

public interface ILoopService {
    ResponseResult addLoop(Looper looper);

    ResponseResult updateLoop(String looperId, Looper looper);

    ResponseResult deleteLoop(String looperId);

    ResponseResult listLoops();

    ResponseResult getLoop(String looperId);
}
