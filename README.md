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

 - 方式2：使用本地缓存时，在你自己的Activity（**仅启动一次的，用于申请存储权限**）内使用如下代码即可,**注意使用前先申请存储权限**：

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
使用原理说明：

 - 本库主要是监听帧率的回调，当绘制周期内（16.6ms）未完成绘制，就认为掉帧，此时根据设定的帧率的阈值，计算两次刷新的时间差计算出跳过的帧率，如果超出设定的帧率阈值，则打印日志信息并根据配置输出到文件。

 - **需要注意打印的堆栈信息，只是发生卡顿前指定数量的堆栈信息，并不能保证卡顿的代码就在其中，理论上说缓存的堆栈信息数量越多，定位到问题代码的可能性就越大。**

---
测试打印信息：

```java
     ~~~~~~~~~~~~~~~~~~~start~~~~~~~~~~~~~~~~~~~~~~ 
      
     ---------------------------------------------------
     com.simon.uiwatcherdemo.MainActivity$1.onClick(MainActivity.java:45)
     ---------------------------------------------------
     
     ---------------------------------------------------
     com.simon.uiwatcherdemo.MainActivity$1.onClick(MainActivity.java:53)
     ---------------------------------------------------
     
     ~~~~~~~~~~~~~~~~~~~end~~~~~~~~~~~~~~~~~~~~~~
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
     compile 'com.github.guohaiyang1992:UiWatcher:v1.0'
	}
```

---

> **注**：有些效果，文字无法描述，只有亲身体验才会感同身受。还请大家尽情使用，如有问题还请给我反馈。好不好，用过才知道~


