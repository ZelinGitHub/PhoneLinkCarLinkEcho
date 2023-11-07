package com.incall.apps.hicar.servicesdk.manager;

import androidx.annotation.NonNull;

public class RequestAppResp {
    private int RespCode;
    private String AppPackage;
    private String Description;

    public int getRespCode() {
        return RespCode;
    }

    public void setRespCode(int respCode) {
        RespCode = respCode;
    }

    public String getAppPackage() {
        return AppPackage;
    }

    public void setAppPackage(String appPackage) {
        AppPackage = appPackage;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    @NonNull
    @Override
    public String toString() {
        return "RequestAppResp{" +
                "RespCode=" + RespCode +
                ", AppPackage='" + AppPackage + '\'' +
                ", Description='" + Description + '\'' +
                '}';
    }
}
