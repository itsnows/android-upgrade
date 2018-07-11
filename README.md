# android-upgrade
这是一个安卓软件升级库。<br>

功能：
====
1.升级模式支持普通升级、强制升级、灰度升级。<br>
2.安装包下载支持 断点续传，分流下载，动态网络监听下载。<br>
3.支持更新模板或自定义更新模板或下载链接<br>
4.支持通知栏显示和对话框显示<br>
5.支持Android 4.2以上所有设备<br>

依赖：
====

Or use Gradle:<br>
Add it in your root build.gradle at the end of repositories:<br>
allprojects {<br>
repositories {<br>
	...<br>
	maven {<br>
			url 'https://jitpack.io'<br>
	}<br>
}<br>

Add the dependency<br>
dependencies {<br>
	  compile 'com.github.itsnows:android-upgrade:1.1.4'<br>
	}<br>
  
Or Maven:<br>
Add the JitPack repository to your build file<br>
\<repositories><br>
		\<repository><br>
		    \<id>jitpack.io\</id><br>
		    \<url>https://jitpack.io\</url><br>
		\</repository><br>
	\</repositories><br>
   
Add the dependency<br>
\<dependency><br>
	    \<groupId>com.github.itsnows\</groupId><br>
	    \<artifactId>android-upgrade\</artifactId><br>
	    \<version>1.1.4\</version><br>
\</dependency><br>

使用：
====

1.更新文档<br>
```xml
<?xml version="1.0" encoding="utf-8"?>
<android>

    <!--稳定版-->
    <stable>
        <!--date：更新日期-->
        <date>2018-02-09</date>
        <!--mode：更新模式 1普通 2强制-->
        <mode>1</mode>
        <!--log：更新说明-->
        <log>
            <item>#新增商城模块（测试阶段，请勿付款）</item>
            <item>#新增通知栏开关</item>
            <item>#更换每日计步算法（由于开发组无华为P10设备，如还是偶发性数据异常情况，请华为P10用户出现问题请及时和我们反馈。）</item>
            <item>#优化计步模块启动速度</item>
            <item>#优化运动轨迹（定位偏差，GPS信号不好数据异常）</item>
            <item>#优化闹钟稳定性</item>
            <item>#优化App稳定性</item>
            <item>#优化App性能</item>
            <item>#优化App框架</item>
            <item>#修复部分已知Bug</item>
        </log>
        <!--versionCode：新版App版本号-->
        <versionCode>86</versionCode>
        <!--versionCode：新版App版本名称-->
        <versionName>1.0.8.6</versionName>
        <!--dowanloadUrl：新版App下载链接-->
        <dowanloadUrl>http://gdown.baidu.com/data/wisegame/16f98e07f392294b/QQ_794.apk</dowanloadUrl>
        <!--md5：新版App安装包完整性-->
        <md5></md5>
    </stable>

    <!--测试版-->
    <beta>
        <!--device：测试版设备序列号-->
        <device>
            <sn>JGB9K17928918126</sn>
            <sn>BTFDU17113013878</sn>
            <sn>RSIZAINFYSFASKCE</sn>
            <sn>3a2c55a</sn>
            <sn>RV8G303QPPD</sn>
            <sn>AYKNW17C13006681</sn>
            <sn>f4e3d8e9</sn>
            <sn>73ead6d60804</sn>
            <sn>621QEDQ93W5JA</sn>
            <sn>7eb8859</sn>
            <sn>LKX7N18105004066</sn>
            <sn>08776ae70703</sn>
        </device>
        <!--date：更新日期-->
        <date>2018-02-09</date>
        <!--mode：更新模式 1普通 2强制-->
        <mode>1</mode>
        <!--log：更新说明-->
        <log>
            <item>#内侧版本</item>
        </log>
        <!--versionCode：新版App版本号-->
        <versionCode>86</versionCode>
        <!--versionCode：新版App版本名称-->
        <versionName>1.0.8.6</versionName>
        <!--dowanloadUrl：新版App下载链接-->
        <dowanloadUrl>http://gdown.baidu.com/data/wisegame/16f98e07f392294b/QQ_794.apk</dowanloadUrl>
        <!--md5：新版App安装包完整性-->
        <md5></md5>
    </beta>

</android>
```

1.自动检测更新<br>
```java
UpgradeManager manager = new UpgradeManager(this);
manager.checkForUpdates(new UpgradeOptions.Builder()
                .setIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                // 通知栏标题（可选）
                .setTitle("腾讯QQ")
                // 通知栏描述（可选）
                .setDescription("更新通知栏")
                // 下载链接或更新文档链接
                .setUrl("http://www.rainen.cn/test/app-update-common.xml")
                // 下载文件存储路径（可选）
                .setStorage(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/com.upgrade.apk"))
                // 是否支持多线性下载（可选）
                .setMultithreadEnabled(true)
                // 线程池大小（可选）
                .setMultithreadPools(10)
                // 文件MD5（可选）
                .setMd5(null)
                .build(), false);
```




