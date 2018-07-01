package com.simon.uiwatch.callback;

import android.annotation.SuppressLint;
import android.view.Choreographer;

import com.simon.uiwatch.log.LogMonitor;

import java.util.concurrent.TimeUnit;

/**
 * 用于监听帧率的回调
 *
 * @author Simon
 * @version v1.0
 * @date 2018/7/1
 */
@SuppressLint("NewApi")
public class UiWatchFrameCallback implements Choreographer.FrameCallback {
    /**
     * 上次帧率时间戳
     */
    long lastFrameTimeNanos = 0;
    /**
     * 当前帧率时间戳
     */
    long currentFrameTimeNanos = 0;

    /**
     * 最小允许的跳过帧率的数量
     */
    private int minSkipFrameCount;

    /**
     * 是否退出,默认false
     */
    private boolean isExit = false;

    /**
     * 构造方法
     */
    public UiWatchFrameCallback(int minSkipFrameCount) {
        this.minSkipFrameCount = minSkipFrameCount;
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        //首次初始化last时间,并开启监控
        if (lastFrameTimeNanos == 0) {
            lastFrameTimeNanos = frameTimeNanos;
            LogMonitor.getInstance().startMonitor();
        }
        //初始化当前时间,计算帧率时间差,计算跳过的帧率,超出限制输出log,并重置
        currentFrameTimeNanos = frameTimeNanos;
        long diffMs = TimeUnit.MILLISECONDS.convert(currentFrameTimeNanos - lastFrameTimeNanos, TimeUnit.NANOSECONDS);
        int skipFrameCount = (int) (diffMs / 16.6);
        if (skipFrameCount > minSkipFrameCount) {
            LogMonitor.getInstance().startOutputAndResetCollectionMonitor();
        }
        //将当前的时间设置为last时间,用于下次计算,并重新注册
        lastFrameTimeNanos = currentFrameTimeNanos;
        //没退出的时候通知下次,否则不通知
        if (!isExit) {
            Choreographer.getInstance().postFrameCallback(this);
        }
    }

    /**
     * 设置是否退出
     *
     * @param exit true:退出 false:不退出
     */
    public void setExit(boolean exit) {
        isExit = exit;
    }
}
