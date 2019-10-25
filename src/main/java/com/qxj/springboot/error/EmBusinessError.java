package com.qxj.springboot.error;

public enum EmBusinessError implements CommonError {
    //通过错误类型10001
    PARAMETER_VALIDATION_ERROR(10001, "参数不合法"),
    UNKNOWN_ERROR(10002, "未知错误");



    EmBusinessError(int errCode, String errMsg) {
        this.errMsg = errMsg;
        this.errCode = errCode;
    }

    //java的枚举可以有全员变量的属性,本质上是一个面向对象的类
    private int errCode;
    private String errMsg;

    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}
