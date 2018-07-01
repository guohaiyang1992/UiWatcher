package com.simon.uiwatch.core;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;
import android.view.Choreographer;

import com.simon.uiwatch.callback.UiWatchFrameCallback;
import com.simon.uiwatch.log.LogMonitor;

/**
 * 用于观察Android Ui的卡顿情况
 * 原理：通过监听渲染信号之间的时间差来确定是否卡顿,并采用高频采样确保数据的准确性
 * 功能：
 * <p>
 * 1.可限定帧率检测阈值
 * 2.设置日志输出TAG
 * 3.设置存储的堆栈数量
 * 4.设置是否开启缓存本地
 * 5.设置本地缓存文件夹地址、按照天拆分文件夹
 * <p>
 * 注意：此工具类要求api>=16
 * 细节处理：
 * 1.对于监控的内容采用高频采样,然后根据设置是否开启缓存,缓存到本地,方便后的定位(包含时间戳和分割)
 * 2.限制最大堆的数量
 * 3.采样间隔,16ms 默认获取采样卡顿前10帧的堆栈数据
 *
 * @author guohaiyang1
 * @date 18-6-27
 * @see <a href="https://mp.weixin.qq.com/s/MthGj4AwFPL2JrZ0x1i4fw">广研 Android 卡顿监控系统</a>
 * <p>
 */
public class UiWatcher {

    /**
     * 帧率阈值,默认为1(超出1帧时间视为卡顿)
     */
    private int minSkipFrameCount = 1;

    /**
     * 堆栈信息最多保存条数,最少1至多无限制,默认10
     */
    private int cacheDataSize = 10;

    /**
     * 卡顿时输出日志的log,默认为 UiWatcher
     */
    private String tag = "UiWatcher";

    /**
     * 是否开启缓存到文件,默认为true
     */
    private boolean isNeedCacheToFile = true;

    /**
     * 缓存的文件夹地址
     */
    private String cacheFolder = "UiWatcher";

    /**
     * 待筛选的关键词(用于剔除不重要信息,可选,不填入则不剔除)
     */
    private String[] keyWords = null;

    /**
     * 帧率回调
     */
    private UiWatchFrameCallback frameCallback;

    /**
     * 是否观察中
     */
    private boolean isWatching = false;

    /**
     * 单例对象
     */
    private static volatile UiWatcher instance;

    /**
     * 私有化
     */
    private UiWatcher() {

    }

    //-----------------对外静态方法---------------------

    /**
     * 创建UiWatcher
     */
    public static UiWatcher getInstance() {
        if (instance == null) {
            synchronized (UiWatcher.class) {
                if (instance == null) {
                    instance = new UiWatcher();
                }
            }
        }
        return instance;
    }


    //-----------------对外公有方法---------------------

    /**
     * 帧率阈值(默认1),当跳帧超出此阈值时报警,输出日志并缓存(视开关情况)
     *
     * @param minSkipFrameCount 帧率阈值
     */
    public UiWatcher minSkipFrameCount(int minSkipFrameCount) {
        this.minSkipFrameCount = minSkipFrameCount;
        return this;
    }


    /**
     * tag,用于日志输出,标识，默认是UiWatcher
     *
     * @param tag tag
     */
    public UiWatcher tag(String tag) {
        this.tag = tag;
        return this;
    }

    /**
     * 是否需要缓存日志到文件
     *
     * @param isNeedCacheToFile true:缓存 false:不缓存
     */
    public UiWatcher isNeedCacheToFile(boolean isNeedCacheToFile) {
        this.isNeedCacheToFile = isNeedCacheToFile;
        return this;
    }

    /**
     * 缓存文件夹
     *
     * @param cacheFolder 文件夹
     */
    public UiWatcher cacheFolder(String cacheFolder) {
        this.cacheFolder = cacheFolder;
        return this;
    }

    /**
     * 缓存堆栈数量
     *
     * @param cacheDataSize 缓存帧率数量
     */
    public UiWatcher cacheSize(int cacheDataSize) {
        this.cacheDataSize = cacheDataSize;
        return this;
    }

    /**
     * 待筛选的关键词
     *
     * @param keyWords 关键词
     */
    public UiWatcher keyWords(String... keyWords) {
        this.keyWords = keyWords;
        return this;
    }

    /**
     * 用于开启监听，必执行方法！！
     */
    @SuppressLint("NewApi")
    public void startWatch() {
        if (isWatching) {
            return;
        }
        //开始之前校验参数
        if (minSkipFrameCount < 1) {
            throw new IllegalArgumentException("minSkipFrameCount 必须大于等于1！");
        }
        if (cacheDataSize < 1) {
            throw new IllegalArgumentException("cacheDataSize 必须大于等于1！");
        }
        if (TextUtils.isEmpty(tag)) {
            throw new IllegalArgumentException("tag 不允许为null或者空！");
        }
        if (isNeedCacheToFile) {
            if (TextUtils.isEmpty(cacheFolder)) {
                throw new IllegalArgumentException("缓存文件夹不允许为null或者空！");
            }
        }
        //将对应的参数进行配置
        LogMonitor.getInstance().setCacheDataSize(cacheDataSize);
        LogMonitor.getInstance().setCacheFolder(cacheFolder);
        LogMonitor.getInstance().setNeedCacheToFile(isNeedCacheToFile);
        LogMonitor.getInstance().setKeyWords(keyWords);
        LogMonitor.getInstance().setTag(tag);
        //将当前回调注册到系统
        frameCallback = new UiWatchFrameCallback(minSkipFrameCount);
        Choreographer.getInstance().postFrameCallback(frameCallback);
    }

    @SuppressLint("NewApi")
    public void stopWatch() {
        //关闭帧率监听
        if (frameCallback != null) {
            frameCallback.setExit(true);
            Choreographer.getInstance().removeFrameCallback(frameCallback);
            frameCallback = null;
        }
        //关闭日志监听以及相关的线程等资源
        LogMonitor.getInstance().stopMonitor();
        //切换当前的状态
        isWatching = false;
    }


}
