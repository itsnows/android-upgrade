# android-upgrade
这是一个安卓软件升级库。<br>

功能：
====
1.升级模式支持普通升级、强制升级、灰度升级。<br>
2.安装包下载支持 断点续传，分流下载，动态网络监听下载。<br>
3.支持更新模板或自定义更新模板或下载链接<br>
4.支持Android 4.2以上所有设备<br>

依赖：
====

Or use Gradle:<br>
Add it in your root build.gradle at the end of repositories:<br>
allprojects {<br>
repositories {<br>
	...<br>
	maven { <br>
			url 'https://jitpack.io'<br>
	}<br>
}<br>

Add the dependency<br>
dependencies {<br>
	  compile 'com.github.itsnows:android-upgrade:1.1.4'<br>
	}<br>
  
Or Maven:<br>
Add the JitPack repository to your build file<br>
<repositories><br>
		<repository><br>
		    <id>jitpack.io</id><br>
		    <url>https://jitpack.io</url><br>
		</repository><br>
	</repositories><br>
   
Add the dependency<br>
<dependency><br>
	    <groupId>com.github.itsnows</groupId><br>
	    <artifactId>android-upgrade</artifactId><br>
	    <version>1.1.4</version><br>
</dependency><br>



