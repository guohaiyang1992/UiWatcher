# UiWatcher
## Ui卡顿监视工具，高频采集堆栈信息，方便定位问题代码。

---
功能：

 - 可设定帧率阈值（当跳过多少帧率时，输出堆栈信息）
 - 可设定堆栈信息缓存数量（数量缓存的越多,定位到问题代码的可能性越大）
 - 可设定是否缓存到本地（存储到本地文件,方便后续查阅）,可自定义缓存文件夹
 - 可设定关键词过滤（防止系统类和第三方类干扰）
 - 支持最小sdk版本为 16
 

---
使用方法:

 - 方式1：不使用本地缓存时，在你自己的Application内使用如下代码即可：

 ```java
 public class TestApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
      //是否缓存到本地、关键词、缓存个数、帧率阈值、tag
      UiWatcher.getInstance().isNeedCacheToFile(false).keyWords("com.simon.uiwatcherdemo").cacheSize(10).minSkipFrameCount(1).tag("simon").startWatch();

    }
}
 ```

 - 方式2：使用本地缓存时，在你自己的Activity内使用如下代码即可,注意使用前先申请存储权限：

 ```java
 public class TestActivity extends Activity {
    @Override
    public void onCreate() {
        super.onCreate();
      //关键词、缓存个数、帧率阈值、tag
      UiWatcher.getInstance().keyWords("com.simon.uiwatcherdemo").cacheSize(10).minSkipFrameCount(1).tag("simon").startWatch();

    }
}
 ```
 
---

测试打印信息：

```java
simon:
|￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣￣
|  Ui Thread blocking more than 1000 ms in this postion: 
|       com.simon.uiwatchdog.MainActivity.onCreate(MainActivity.java:16)  
|_______________________________________________________________
```





---

引入方法：

 - 在你的Project的 build.gradle 按下面的操作配置仓库。
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

 - 然后在你对应的Modlule内的build.gradle内按下面的方式进行引入。

	

```
dependencies {
     compile 'com.github.guohaiyang1992:UiWatchDog:1.1'
	}
```
