package com.simon.uiwatcherdemo;

import android.app.Application;
import android.os.Build;

import com.simon.uiwatch.core.UiWatcher;

/**
 * 默认开启了日志文件本地缓存,如果不需要请设置isNeedCacheToFile 为false,关闭本地文件缓存,
 * 使用存储需要存储权限,在6.0之后需要手动申请权限,
 * 目前由于技术所限制,application 内还无法申请权限,补救方法就是在启动activity 的时候手动申请,
 * 后续的操作日志就应该可以写入了
 *
 * @author guohaiyang1
 * @date 18-6-29
 */
public class UiWatcherApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            UiWatcher.getInstance().keyWords("com.simon.uiwatcherdemo").cacheSize(10).minSkipFrameCount(1).tag("ghy").startWatch();
        }
    }
}
