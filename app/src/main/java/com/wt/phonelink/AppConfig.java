package com.wt.phonelink;

public class AppConfig {
    public static int getDpi() {
        String flavor = BuildConfig.FLAVOR;
        switch (flavor) {
            case "default_main":
            case "c318":
            default:
                return 160;
        }
    }

    public static int getDisplayWidth() {
        String flavor = BuildConfig.FLAVOR;
        switch (flavor) {
            case "default_main":
            default:
                return 2560;
            case "c318":
                return 1920;
        }
    }
    public static int getExpectedDisplayWidth() {
        String flavor = BuildConfig.FLAVOR;
        switch (flavor) {
            case "default_main":
            default:
                return 2560;
            case "c318":
                return 1920;
        }
    }

    public static int getDisplayHeight() {
        String flavor = BuildConfig.FLAVOR;
        switch (flavor) {
            case "default_main":
            default:
                return 1600;
            case "c318":
                return 1080;
        }
    }

    public static int getExpectedDisplayHeight() {
        String flavor = BuildConfig.FLAVOR;
        switch (flavor) {
            case "default_main":
            default:
                return 1600;
            case "c318":
                return 1080;
        }
    }


    public static int getDockHeight() {
        String flavor = BuildConfig.FLAVOR;
        switch (flavor) {
            case "default_main":
            default:
                return 120;
            case "c318":
                return 120;
        }
    }
    public static int getStatusBarHeight() {
        String flavor = BuildConfig.FLAVOR;
        switch (flavor) {
            case "default_main":
            case "c318":
            default:
                return 112;
        }
    }


}
