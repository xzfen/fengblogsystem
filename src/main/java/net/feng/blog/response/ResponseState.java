package net.feng.blog.response;

public enum ResponseState {
    SUCCESS(true, 20000, "操作成功"),
    LOGIN_SUCCESS(true, 20001, "登录成功"),
    JOIN_IN_SUCCESS(true, 20002, "注册成功"),
    FAILED(false, 40000, "操作失败"),
    GET_RESOURCE_FAILED(false, 40001, "获取资源失败"),
    ACCOUNT_NOT_LOGIN(false, 40002, "账号未登录"),
    PERMISSION_DENIED(false, 40003, "权限不足"),
    ACCOUNT_DENIED(false, 40004, "账号被禁止"),
    LOGIN_FAILED(false, 49999, "登录失败"),
    WAiTING_FOR_SCAN(false, 40008, "等待扫码"),
    QR_CODE_DEPRECATE(false, 40009, "二维码已过期"),
    ERROR_403(false, 40403, "权限不足"),
    ERROR_404(false, 40404, "页面丢失"),
    ERROR_504(false, 40504, "系统繁忙，请稍后重试"),
    ERROR_505(false, 40505, "请求错误，请检查所提交数据");


    ResponseState(boolean success, int code, String message) {
        this.code = code;
        this.success = success;
        this.message = message;
    }

    private int code;
    private String message;
    private boolean success;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
