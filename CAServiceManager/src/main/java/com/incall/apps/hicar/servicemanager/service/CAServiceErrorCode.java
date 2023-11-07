package com.incall.apps.hicar.servicemanager.service;

/**
 * 错误码
 * @author KongJing
 * 2021.4.29
 */
public enum  CAServiceErrorCode {
    //未找到对应的Service
    SERVICE_NOT_FOUND("CAServiceError_O001","未找到对应的Service"),
    //未找到对应的方法
    METHOD_NOT_FOUND("CAServiceError_O002","未找到对应的方法"),
    //非法参数
    ILLEGAL_ARGUMENT("CAServiceError_O004","非法参数");

    private String value;
    private String desc;

    private CAServiceErrorCode(String value, String desc) {
        this.setValue(value);
        this.setDesc(desc);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "[code:" + this.value + ", message:"+this.desc+ "]" ;
    }
}
