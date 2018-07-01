package com.simon.uiwatch.util;

import android.util.Log;

/**
 * 用于处理Log的输出,Android系统中对Log大小有限制,超出需要拆分显示
 *
 * @author guohaiyang1
 * @date 18-6-27
 */
public class LogUtils {

    /**
     * 文字最大限制
     */
    private static final int MAX_SIZE = 2000;

    /**
     * 输出日志
     *
     * @param tag tag
     * @param msg msg
     */
    public static void printLog(String tag, String msg) {
        // 获取当前log的长度
        int msgSize = msg.length();
        if (msgSize <= MAX_SIZE) {
            Log.e(tag, msg);
            return;
        }
        //如果超出最大值,看需要输出几次
        int count = msgSize / MAX_SIZE;
        int remainder = msgSize % MAX_SIZE;
        if (remainder != 0) {
            count++;
        }
        //遍历输出
        for (int i = 0; i < count; i++) {
            int start = i * MAX_SIZE;
            int end = (i + 1) * MAX_SIZE;
            end = Math.min(end, msgSize);
            Log.e(tag, msg.substring(start, end));
        }
    }
}
