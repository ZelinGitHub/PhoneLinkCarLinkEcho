# 模块说明
MVP模式
#### app 手机互联应用界面逻辑相关代码（hicar+carlink）
#### CAServiceManager 设备连接等接口的定义以及presenter层的实现
#### HiCarPresenterComponent 服务层具体实现，主要是跟各个相关模块的交互逻辑做处理，例如对蓝牙模块的交互、对语音模块的交互等
#### HiCarServiceComponent 服务管理，主要是做服务的注册、取消注册、对外暴露方法、添加事件、移除事件、发布事件

# 分支说明
#### master 默认分支
#### feature_c236 C236开发分支
#### feature_cd569 CD569开发分支

###hicar工程

工程最好在cd569上面运行，有一部华为手机。

#### 模拟物理长按

模拟方控的小艺切换

下面这个指令是模拟车机长按方向盘的语音(47)，切换车机上的hicar功能(231)
将下面的脚本编辑成 .sh 的文件格式，adb 推送到车机中， chmod 777 vo.sh ，然后 ./vo.sh 运行
```
#!/bin/sh

sendevent /dev/input/event1 1 47 1
sendevent /dev/input/event1 0 0 0

echo "down"

sleep 3

sendevent /dev/input/event1 1 47 0
sendevent /dev/input/event1 0 0 0

echo "up"

```

如果需要短按方向盘上的语音按钮,那么可以发
`adb shell input keyevent 231`


需要使用 华为的 sdk bat 脚本注入各种插件

hicar 的文件夹名称是 system/app/InCallHiCar

Hicarservice 本身是为了启动的时候初始化各种的服务

关于华为的sdk操作
打开sdk 的日志开关
`adb shell > setprop persist.vendor.hicarsdk.debug 1 //打开调试日志 > setprop persist.vendor.hicarsdk.debug 0 //关闭调试日志`

`adb shell disable-verify 100`   Android studio 可以连接上adb



### 关于音频日志的抓取

车机端

打开车机上的 华为 sdk 的开关之后再进行测试

`adb shell > setprop persist.vendor.hicarsdk.debug 1`

车机开始测试的时候打开tcpdump抓取

`tcpdump -i ap0 -s 0 -w /sdcard/ap0.pcap &`

然后在车机的 sdcard 中可以获取tcpdump日志

测试完毕后，等待两分钟再抓取车机日志。

```
adb -s 0123456789ABCDEF root
adb -s 0123456789ABCDEF remount



adb -s 0123456789ABCDEF pull  /resources/mtklog/mobilelog
adb -s 0123456789ABCDEF pull  /data/misc/bluetooth/logs

::adb -s 0123456789ABCDEF shell rm -rf /resources/mtklog/mobilelog
::adb -s 0123456789ABCDEF shell rm -rf /data/misc/bluetooth/logs

pause
```

### PCM(车机音频抓取电话输入音频)

dump一下音频吗，执行完 setprop vendor.streamin.aec.pcm.dump 1命令后打hicar电话，打完电话后把data/vendor/audiohal/audio_dump/目录导出来




手机端(ROOT)

1.需要启动 tcpdump 的抓取，参考 `无线tcpdump ` 这个文件夹中的操作

电脑连接手机先执行 1和 2 。等测试完毕后再执行3导出日志。

2.执行 `GetAppLog_phone_withBT v1.1.bat` 脚本抓取手机端的日志

手机端(商用机)

1、手机开启adb调试
2、手机开启ap log开关：通话界面拨号*#*#2846579#*#*进入工程模式，在"后台设置"中打开"AP LOG设置"
3、执行脚本(商用手机清除日志&设置缓存.bat)清除手机当前日志并设置最大缓存区大小
4、执行测试内容
5、执行脚本(商用手机获取缓存区内所有日志.bat)获取日志

HiCar问题

HiCar的问题主要可分为两类：
1、连接问题
   低概率出现连接失败，重连失败，不自动断开，断开卡住等，目前已关闭大部分问题
   后续测试可能会持续提这方面DTM
2、音频问题
   音频有混音，误唤醒，播放不完整，播放失败等问题，可以先找周维科周工分析音频日志

   需要华为一起分析的话可以通过外网邮件或微信联系华为马孟钊或黄远强，马工负责认证，黄工是维护

   mamengzhao@huawei.com      huangyuanqiang1@huawei.com