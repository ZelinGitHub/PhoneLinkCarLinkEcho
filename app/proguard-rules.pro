# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.tinnove.wecarspeech.clientsdk.enums.**{*;}
-keep class com.tinnove.wecarspeech.clientsdk.exception.**{*;}
-keep class com.tinnove.wecarspeech.clientsdk.impl.**{*;}
-keep class com.tinnove.wecarspeech.vframework.**{*;}
-keep class com.tinnove.wecarspeech.clientsdk.model.**{*;}
-keep class com.tinnove.wecarspeech.clientsdk.semantic.**{*;}
-keep class com.tinnove.wecarspeech.utils.log.**{*;}
-keep class com.tinnove.wecarspeech.clientsdk.utils.semantic.**{*;}
-keep class com.wt.phonelink.VoiceManager{*;}

-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
# And if you use AsyncExecutor:
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

-keep class com.huawei.**{*;}

-keep class com.share.connect.** {*;}
-dontwarn com.share.connect.**
-keep class com.ucar.** {*;}
-dontwarn com.ucar.**
-keep class com.ucarsink.sink.** {*;}
-dontwarn com.ucarsink.sink.**


-keep class com.tinnove.hicarclient.**{*;}
