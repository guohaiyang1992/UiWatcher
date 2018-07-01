package com.simon.uiwatch.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间工具类,用于格式化当天的时间和获取当时的时间信息
 *
 * @author guohaiyang1
 * @date 18-6-27
 */
public class TimeUtils {

    /**
     * 时间格式化
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

    /**
     * 时间格式化
     */
    private static final SimpleDateFormat DATE_FORMAT_1 = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 获取当前的时间 格式化：YYYY-MM-DD hh:mm:ss
     *
     * @return 获取当前的格式化后的时间
     */
    public static String getCurrentFormatTime() {
        return DATE_FORMAT.format(new Date());
    }


    /**
     * 获取当前的时间 格式化：YYYY-MM-DD
     *
     * @return 获取当前的格式化后的时间
     */
    public static String getFileFolderNameByTime() {
        return DATE_FORMAT_1.format(new Date());
    }
}
