package com.tinnove.schedulecard;

import androidx.annotation.NonNull;

//{
//	"MediaData": {
//		"AlbumArtURL": "http:\/\/y.gtimg.cn\/music\/photo_new\/T002R800x800M000004NEn9X0y2W3u_1.jpg",
//		"AlbumName": "Relax",
//		"AppName": "QQ音乐",
//		"Artist": "Junona Boys",
//		"ElapsedTime": 102,
//		"IsAudioFromCar": true,
//		"Name": "Relax",
//		"AppPackageName": "com.tencent.qqmusic",
//		"Status": 2,
//		"TotalTime": 149
//	}
//}
public class MediaData {
    private String AlbumArtURL;
    private String AlbumName;
    private String AppName;
    private String Artist;
    private int ElapsedTime;
    private boolean IsAudioFromCar;
    private String Name;
    private String AppPackageName;
    private int Status;
    private int TotalTime;

    public String getAlbumArtURL() {
        return AlbumArtURL;
    }

    public void setAlbumArtURL(String albumArtURL) {
        AlbumArtURL = albumArtURL;
    }

    public String getAlbumName() {
        return AlbumName;
    }

    public void setAlbumName(String albumName) {
        AlbumName = albumName;
    }

    public String getAppName() {
        return AppName;
    }

    public void setAppName(String appName) {
        AppName = appName;
    }

    public String getArtist() {
        return Artist;
    }

    public void setArtist(String artist) {
        Artist = artist;
    }

    public int getElapsedTime() {
        return ElapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        ElapsedTime = elapsedTime;
    }

    public boolean isAudioFromCar() {
        return IsAudioFromCar;
    }

    public void setAudioFromCar(boolean audioFromCar) {
        IsAudioFromCar = audioFromCar;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAppPackageName() {
        return AppPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        AppPackageName = appPackageName;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    public int getTotalTime() {
        return TotalTime;
    }

    public void setTotalTime(int totalTime) {
        TotalTime = totalTime;
    }

    @NonNull
    @Override
    public String toString() {
        return "MediaData{" +
                "AlbumArtURL='" + AlbumArtURL + '\'' +
                ", AlbumName='" + AlbumName + '\'' +
                ", AppName='" + AppName + '\'' +
                ", Artist='" + Artist + '\'' +
                ", ElapsedTime=" + ElapsedTime +
                ", IsAudioFromCar=" + IsAudioFromCar +
                ", Name='" + Name + '\'' +
                ", AppPackageName='" + AppPackageName + '\'' +
                ", Status=" + Status +
                ", TotalTime=" + TotalTime +
                '}';
    }
}
