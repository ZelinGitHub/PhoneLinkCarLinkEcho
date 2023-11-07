package com.wt.phonelink;

import android.graphics.drawable.Drawable;

/**
 * @author renrui
 */
public class MainItemBean {


    private Drawable icon;
    private String title;
    private String subTitle;


    private String type;


    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public MainItemBean(String type, Drawable drawable, String title, String subTitle){
        this.type = type;
        this.icon = drawable;
        this.title = title;
        this.subTitle = subTitle;


    }

    public MainItemBean(){}

    @Override
    public String toString() {
        return "MainItemBean{" +
                "type=" + type +
                ", title='" + title + '\'' +
                ", subTitle='" + subTitle + '\'' +
                '}';
    }

}
