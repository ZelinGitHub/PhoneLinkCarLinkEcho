package com.tinnove.hicarclient.beans;

import android.os.Parcel;
import android.os.Parcelable;
//应用程序信息
public class WTAppInfoBean implements Parcelable {
    private String mPackageName = null;
    private String mName = null;
    private byte[] mIcon = null;
    private int mType = 0;

    public WTAppInfoBean() {}

    public WTAppInfoBean(String pkg, String name, byte[] icon, int type) {
        this.mPackageName = pkg;
        this.mName = name;
        this.mIcon = icon;
        this.mType = type;
    }

    protected WTAppInfoBean(Parcel in) {
        mPackageName = in.readString();
        mName = in.readString();
        mIcon = in.createByteArray();
        mType = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPackageName);
        dest.writeString(mName);
        dest.writeByteArray(mIcon);
        dest.writeInt(mType);
    }

    public static final Creator<WTAppInfoBean> CREATOR = new Creator<WTAppInfoBean>() {
        @Override
        public WTAppInfoBean createFromParcel(Parcel in) {
            return new WTAppInfoBean(in);
        }

        @Override
        public WTAppInfoBean[] newArray(int size) {
            return new WTAppInfoBean[size];
        }
    };

    public String getmPackageName() {
        return mPackageName;
    }

    public void setmPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public byte[] getmIcon() {
        return mIcon;
    }

    public void setmIcon(byte[] mIcon) {
        this.mIcon = mIcon;
    }

    public int getmType() {
        return mType;
    }

    public void setmType(int mType) {
        this.mType = mType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
