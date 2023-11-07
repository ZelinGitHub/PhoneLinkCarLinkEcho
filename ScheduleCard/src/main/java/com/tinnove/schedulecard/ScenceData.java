package com.tinnove.schedulecard;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class ScenceData {
    private int Type;
    private int Status;
    private int Id;
    private String Title;
    private byte[] AppIcon;
    private String AppPackage;
    private byte[] InfoImage;
    private String MainText;
    private String SubText;
    private String OptText;

    public byte[] getAppIcon() {
        return AppIcon;
    }

    public void setAppIcon(byte[] appIcon) {
        AppIcon = appIcon;
    }

    public byte[] getInfoImage() {
        return InfoImage;
    }

    public void setInfoImage(byte[] infoImage) {
        InfoImage = infoImage;
    }

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getAppPackage() {
        return AppPackage;
    }

    public void setAppPackage(String appPackage) {
        AppPackage = appPackage;
    }

    public String getMainText() {
        return MainText;
    }

    public void setMainText(String mainText) {
        MainText = mainText;
    }

    public String getSubText() {
        return SubText;
    }

    public void setSubText(String subText) {
        SubText = subText;
    }

    public String getOptText() {
        return OptText;
    }

    public void setOptText(String optText) {
        OptText = optText;
    }

    @NonNull
    @Override
    public String toString() {
        return "ScenceData{" +
                "Type=" + Type +
                ", Status=" + Status +
                ", Id=" + Id +
                ", Title='" + Title + '\'' +
                ", AppIcon=" + Arrays.toString(AppIcon) +
                ", AppPackage='" + AppPackage + '\'' +
                ", InfoImage=" + Arrays.toString(InfoImage) +
                ", MainText='" + MainText + '\'' +
                ", SubText='" + SubText + '\'' +
                ", OptText='" + OptText + '\'' +
                '}';
    }
}
