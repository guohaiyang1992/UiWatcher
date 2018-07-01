package com.simon.uiwatch.log;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;


/**
 * 日志通知者
 * 用于定时给LogExecutor发送各种指令
 * 为保证数据获取的准确性和时效性，
 * 需要启用线程定时获取
 * <p>
 *
 * @author Simon
 * @version v1.0
 * @date 2018/7/1
 */
public class LogNotifier {
    private HandlerThread logNotifierThread;
    private Handler logNotifierHandler;
    private static LogNotifier instance;
    /**
     * 单例的LogExecutor
     */
    private LogExecutor logExecutor;

    //------------- type config-------------------
    /**
     * 数据收集
     */
    private static final int TYPE_COLLECTION = 0;
    /**
     * 数据输出
     */
    private static final int TYPE_OUTPUT = 1;

    /**
     * 默认Log的获取间隔
     */
    private static final long DEFAULT_DELAY = 16;

    private LogNotifier() {
        init();
    }

    public static LogNotifier getInstance() {
        if (instance == null) {
            synchronized (LogNotifier.class) {
                if (instance == null) {
                    instance = new LogNotifier();
                }
            }
        }
        return instance;
    }

    /**
     * 开始
     */
    public void start() {
        if (logExecutor == null || logNotifierThread == null) {
            init();
        }
        logExecutor.start();
        logNotifierThread.start();
        initLogNotifierHandler();
    }

    /**
     * 初始化
     */
    private void init() {
        logExecutor = LogExecutor.getInstance();
        logNotifierThread = new HandlerThread("LogNotifier_Thread");
    }

    /**
     * 初始化日志通知handler
     */
    private void initLogNotifierHandler() {
        if (logNotifierThread == null) {
            return;
        }
        logNotifierHandler = new Handler(logNotifierThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                handleLogNotifierMessage(msg);
            }
        };
    }

    /**
     * 分发LogNotifierMessage
     *
     * @param msg 消息
     */
    private void handleLogNotifierMessage(Message msg) {
        int type = msg.what;
        switch (type) {
            case TYPE_COLLECTION:
                //通知LogExecutor处理收集任务
                sendCollectionMessageToLogExecutor();
                //通知自身下次收集
                sendCollectionMessage(DEFAULT_DELAY);
                break;
            case TYPE_OUTPUT:
                //通知LogExecutor处理输出任务
                sendOutputMessageToLogExecutor();
                break;
            default:
                break;
        }
    }

    /**
     * 发送输出日志消息给LogExecutor
     */
    private void sendOutputMessageToLogExecutor() {
        if (logExecutor == null) {
            return;
        }
        Message message = logExecutor.obtainOutputMessage();
        logExecutor.sendLogExecutorMessage(message);
    }

    /**
     * 发送手机日志消息给LogExecutor
     */
    private void sendCollectionMessageToLogExecutor() {
        if (logExecutor == null) {
            return;
        }
        Thread mainThread = Looper.getMainLooper().getThread();
        StackTraceElement[] stackInfo = mainThread.getStackTrace();
        Message message = logExecutor.obtainCollectionMessage(stackInfo);
        logExecutor.sendLogExecutorMessage(message);
    }

    /**
     * 开始收集通知
     */
    public void startCollectionNotifier() {
        sendCollectionMessage(0);
    }

    /**
     * 开始输出并重启收集通知
     */
    public void startOutputAndRestartCollectionNotifier() {
        removeAllMessage();
        sendOutputMessage(0);
        startCollectionNotifier();
    }

    /**
     * 停止
     */
    public void stop() {
        if (logExecutor != null) {
            logExecutor.stop();
            logExecutor = null;
        }
        if (logNotifierHandler != null) {
            logNotifierHandler.removeCallbacksAndMessages(null);
            logNotifierHandler = null;
        }
        if (logNotifierThread != null) {
            logNotifierThread.quit();
            logNotifierThread = null;
        }
    }

    /**
     * 移除所有消息
     */
    private void removeAllMessage() {
        if (logNotifierHandler == null) {
            return;
        }
        logNotifierHandler.removeCallbacksAndMessages(null);
    }


    /**
     * 发送输出消息
     */
    private void sendOutputMessage(long delay) {
        if (logNotifierHandler == null) {
            return;
        }
        Message outputMessage = Message.obtain(logNotifierHandler, TYPE_OUTPUT);
        logNotifierHandler.sendMessageDelayed(outputMessage, delay);
    }

    /**
     * 发送收集消息到当前的handler
     */
    private void sendCollectionMessage(long delay) {
        if (logNotifierHandler == null) {
            return;
        }
        Message collectionMessage = Message.obtain(logNotifierHandler, TYPE_COLLECTION);
        logNotifierHandler.sendMessageDelayed(collectionMessage, delay);
    }

    //-----------------基础信息---------------------

    /**
     * 设置缓存数量
     *
     * @param cacheDataSize 缓存数量,默认10
     */
    public void setCacheDataSize(int cacheDataSize) {
        if (logExecutor != null) {
            logExecutor.setCacheDataSize(cacheDataSize);
        }
    }

    /**
     * 设置日志TAG
     *
     * @param tag tag
     */
    public void setTag(String tag) {
        if (logExecutor != null) {
            logExecutor.setTag(tag);
        }
    }

    /**
     * 设置是否需要缓存文件
     *
     * @param needCacheToFile true:缓存本地 false:不缓存到本地
     */
    public void setNeedCacheToFile(boolean needCacheToFile) {
        if (logExecutor != null) {
            logExecutor.setNeedCacheToFile(needCacheToFile);
        }
    }

    /**
     * 设置缓存的文件夹
     *
     * @param cacheFolder 文件夹
     */
    public void setCacheFolder(String cacheFolder) {
        if (logExecutor != null) {
            logExecutor.setCacheFolder(cacheFolder);
        }
    }

    /**
     * 设置筛选关键词
     *
     * @param keyWords 关键词集合
     */
    public void setKeyWords(String[] keyWords) {
        if (logExecutor != null) {
            logExecutor.setKeyWords(keyWords);
        }
    }
}
