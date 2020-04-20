# android-upgrade
这是一个安卓软件升级库。<br>

简介：
====
1.升级模式支持普通升级、强制升级、灰度升级。<br>
2.下载支持断点续传、分流下载、动态网络监听下载。<br>
2.安装支持自动安装申请sd卡和安装权限、自动安装（root权限）、自动清除安装包。<br>
3.支持更新模板或自定义更新模板（json或xml）或下载链接<br>
4.支持通知栏显示和对话框显示（自定义主题）<br>
5.支持android 4.2以上设备<br>

![](https://github.com/itsnows/android-upgrade/raw/master/gif/Screenshot_1576573097.png)
![](https://github.com/itsnows/android-upgrade/raw/master/gif/Screenshot_1576573103.png) 
![](https://github.com/itsnows/android-upgrade/raw/master/gif/Screenshot_1576573106.png)
![](https://github.com/itsnows/android-upgrade/raw/master/gif/Screenshot_1576573116.png)
![](https://github.com/itsnows/android-upgrade/raw/master/gif/Screenshot_1576573138.png)
![](https://github.com/itsnows/android-upgrade/raw/master/gif/Screenshot_1576573246.png)

依赖：
====

Or use Gradle:<br>
Add it in your root build.gradle at the end of repositories:<br>
```groovy
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Add the dependency<br>
```groovy
dependencies {
	        compile 'com.github.itsnows:android-upgrade:release'
	}
```
Or Maven:<br>
Add the JitPack repository to your build file<br>
```groovy
<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```
   
Add the dependency<br>
```groovy
<dependency>
	    <groupId>com.github.itsnows</groupId>
	    <artifactId>android-upgrade</artifactId>
	    <version>1.1.4</version>
	</dependency>
```

使用：
====

1.更新文档（xml）<br>
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
                <item>#升级模式支持普通升级、强制升级、灰度升级</item>
                <item>#下载支持断点续传、分流下载、动态网络监听下载</item>
                <item>#安装支持自动安装申请sd卡和安装权限、自动安装（root权限）、自动清除安装包</item>
                <item>#支持通知栏显示和对话框显示（自定义主题）</item>
                <item>#支持自定义对话框</item>
                <item>#支持android 4.2以上设备</item>
            </log>
            <!--versionCode：新版App版本号-->
            <versionCode>5</versionCode>
            <!--versionCode：新版App版本名称-->
            <versionName>1.0.0.5</versionName>
            <!--downloadUrl：新版App下载链接-->
            <downloadUrl>http://gdown.baidu.com/data/wisegame/16f98e07f392294b/QQ_794.apk
            </downloadUrl>
            <!--md5：新版App安装包完整性-->
            <md5></md5>
        </stable>

        <!--测试版-->
        <beta>
            <!--device：测试版设备序列号-->
            <device>
                <sn>JGB9K17928918126</sn>
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
            <versionCode>5</versionCode>
            <!--versionCode：新版App版本名称-->
            <versionName>1.0.0.5</versionName>
            <!--downloadUrl：新版App下载链接-->
            <downloadUrl>http://gdown.baidu.com/data/wisegame/16f98e07f392294b/QQ_794.apk
            </downloadUrl>
            <!--md5：新版App安装包完整性-->
            <md5></md5>
        </beta>

</android>
```

2.更新文档（json）<br>
```json
{
  "android": {
    "stable": {
      "date": "2018-02-09",
      "mode": 1,
      "log": [
        "#升级模式支持普通升级、强制升级、灰度升级",
        "#下载支持断点续传、分流下载、动态网络监听下载",
        "#安装支持自动安装申请sd卡和安装权限、自动安装（root权限）、自动清除安装包",
        "#支持更新模板或自定义更新模板（json或xml）或下载链接",
        "#支持通知栏显示和对话框显示（自定义主题）",
        "#支持自定义对话框",
        "#支持android 4.2以上设备"
      ],
      "versionCode": 20,
      "versionName": "1.2.0",
      "downloadUrl": "http://gdown.baidu.com/data/wisegame/16f98e07f392294b/QQ_794.apk",
      "md5": null
    },
    "beta": {
      "device": [
        "JGB9K17928918126"
      ],
      "date": "2018-02-09",
      "mode": 1,
      "log": [
        "#内侧版本"
      ],
      "versionCode": 20,
      "versionName": "1.2.0",
      "downloadUrl": "http://gdown.baidu.com/data/wisegame/16f98e07f392294b/QQ_794.apk",
      "md5": null
    }
  }
}
```

3.代码调用<br>
```java
UpgradeManager manager = new UpgradeManager(this);

// 自动检测更新
manager.checkForUpdates(new UpgradeOptions.Builder()
       // 对话框主题（可选）
       .setTheme(ContextCompat.getColor(this, R.color.colorPrimary))
       // 通知栏图标（可选）
       .setIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
       // 通知栏标题（可选）
       .setTitle("腾讯QQ")
       // 通知栏描述（可选）
       .setDescription("更新通知栏")
       // 下载链接或更新文档链接
       .setUrl("https://gitee.com/itsnows/android-upgrade/raw/master/doc/app-update.json")
       // 下载文件存储路径（可选）
       .setStorage(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/com.upgrade.apk"))
       // 是否支持多线性下载（可选）
       .setMultithreadEnabled(true)
       // 线程池大小（可选）
       .setMultithreadPools(10)
       // 文件MD5（可选）
       .setMd5(null)
       // 是否自动删除安装包（可选）
       .setAutocleanEnabled(false)
       // 是否自动安装安装包（可选）
       .setAutomountEnabled(false)
       // 是否自动检测更新
       .build(), true);
		
// 手动检测更新
manager.checkForUpdates(new UpgradeOptions.Builder()
       .setIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
       .setTitle("腾讯QQ")
       .setDescription("更新通知栏")
       .setUrl("https://gitee.com/itsnows/android-upgrade/raw/master/doc/app-update.json")
       .setStorage(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/com.upgrade.apk"))
       .setMultithreadEnabled(true)
       .setMultithreadPools(1)
       .build(), true);

// 自定义下载更新
manager.checkForUpdates(new UpgradeOptions.Builder()
       .setIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
       .setTitle("腾讯QQ")
       .setDescription("更新通知栏")
       .setUrl("https://gitee.com/itsnows/android-upgrade/raw/master/doc/app-update.json")
       .setStorage(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/com.upgrade.apk"))
       .setMultithreadEnabled(true)
       .setMultithreadPools(1)
       .setMd5(null)
       .setAutocleanEnabled(true)
       .setAutomountEnabled(true)
       .build(), new OnUpgradeListener() {

            // 安装包下载（无需更新文档）
            @Override
            public void onUpdateAvailable(UpgradeClient client) {

            }

            // 发布版本
            @Override
            public void onUpdateAvailable(Upgrade.Stable stable, UpgradeClient client) {
            }

            // 测试版本
            @Override
            public void onUpdateAvailable(Upgrade.Beta beta, UpgradeClient client) {

            }

            // 没有可用的更新
            @Override
            public void onNoUpdateAvailable(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
		
// 下载安装包（无需更新文档）
manager.checkForUpdates(new UpgradeOptions.Builder()
       .setIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
       .setTitle("腾讯QQ")
       .setDescription("更新通知栏")
       .setUrl("http://gdown.baidu.com/data/wisegame/16f98e07f392294b/QQ_794.apk")
       .setStorage(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/com.upgrade.apk"))
       .setMultithreadEnabled(true)
       .setMultithreadPools(1)
       .setMd5(null)
       .build(), false);

// 取消检测
manager.cancel();

```




