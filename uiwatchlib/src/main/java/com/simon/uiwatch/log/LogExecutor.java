package com.simon.uiwatch.log;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;

import com.simon.uiwatch.util.LogUtils;
import com.simon.uiwatch.util.TimeUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * 负责日志收集和输出
 *
 * @author Simon
 * @version v1.0
 * @date 2018/7/1
 * <p>
 */

public class LogExecutor {
    /**
     * 用于处理日志的线程
     */
    private HandlerThread logExecutorThread;
    /**
     * 用于处理日志的Handler
     */
    private Handler logExecutorHandler;
    /**
     * 实例
     */
    private static LogExecutor instance;
    //----------------base config----------------------
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
     * 缓存文件名称
     */
    private String cacheFileName = "UiWatcherLogData";

    /**
     * 关键词
     */
    private String[] keyWords = null;


    //------------- type config-------------------
    /**
     * 数据收集
     */
    private static final int TYPE_COLLECTION = 0;
    /**
     * 数据输出
     */
    private static final int TYPE_OUTPUT = 1;

    //------------- data config-------------------
    /**
     * 日志堆栈信息的构造builder
     */
    private StringBuilder logStackInfoBuilder;

    /**
     * log堆栈信息队列,只保存限制的条数,防止内存占用过大
     */
    private List<String> logStackInfoQueue;


    private LogExecutor() {
        init();
    }

    /**
     * 单例创建实例
     */
    public static LogExecutor getInstance() {
        if (instance == null) {
            synchronized (LogExecutor.class) {
                if (instance == null) {
                    instance = new LogExecutor();
                }
            }
        }
        return instance;
    }

    /**
     * 开启线程
     */
    public void start() {
        if (logExecutorThread == null) {
            init();
        }
        logExecutorThread.start();
        initLogExecutorHandler();
    }

    private void init() {
        logExecutorThread = new HandlerThread("LogExecutor_Thread");
        logStackInfoBuilder = new StringBuilder();
        logStackInfoQueue = new LinkedList<>();
    }

    /**
     * 初始化logExecutorHandler
     */
    private void initLogExecutorHandler() {
        if (logExecutorThread == null) {
            return;
        }
        logExecutorHandler = new Handler(logExecutorThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                handleLogExecutorMessage(msg);
            }
        };
    }

    /**
     * 用于分发LogExecutorMessage
     *
     * @param msg 消息
     */
    private void handleLogExecutorMessage(Message msg) {
        int type = msg.what;
        Object stackInfo = msg.obj;
        switch (type) {
            case TYPE_COLLECTION:
                handleLogExecutorCollectionMessage(stackInfo);
                break;
            case TYPE_OUTPUT:
                handleLogExecutorOutputMessage();
                break;
            default:
                break;
        }
    }

    /**
     * 用于处理输出消息
     */
    private void handleLogExecutorOutputMessage() {
        //输出堆栈信息
        startOutputLogTask();
    }

    /**
     * 开始输出日志任务
     */
    private void startOutputLogTask() {
        //校验缓存信息的队列
        if (logStackInfoQueue == null || logStackInfoQueue.isEmpty()) {
            return;
        }
        //将获取的队列内的内容遍历获取
        logStackInfoBuilder.delete(0, logStackInfoBuilder.length());
        logStackInfoBuilder.append(" \n");
        logStackInfoBuilder.append(" \n");
        logStackInfoBuilder.append("~~~~~~~~~~~~~~~~~~~start~~~~~~~~~~~~~~~~~~~~~~");
        logStackInfoBuilder.append(" \n");
        logStackInfoBuilder.append(" \n");
        for (String stackInfo : logStackInfoQueue) {
            logStackInfoBuilder.append(stackInfo);
            logStackInfoBuilder.append("\n");
        }
        logStackInfoBuilder.append("~~~~~~~~~~~~~~~~~~~end~~~~~~~~~~~~~~~~~~~~~~");
        logStackInfoBuilder.append("\n");
        logStackInfoBuilder.append(" \n");
        //清除原队列数据
        logStackInfoQueue.clear();
        //获取全部的堆栈信息
        String allStackInfo = logStackInfoBuilder.toString();
        //输出信息并视情况缓存
        LogUtils.printLog(tag, allStackInfo);
        //检测是否需要存储到本地
        if (isNeedCacheToFile) {
            saveAllStackInfoToFile(allStackInfo);
        }
    }

    /**
     * 保存所有的堆栈信息到文件
     *
     * @param allStackInfo 所有的堆栈信息
     */
    private void saveAllStackInfoToFile(String allStackInfo) {
        RandomAccessFile rfile = null;
        //获取文件通道
        FileChannel channel;
        try {
            //根据配置生成文件夹地址
            final String finalFileRootFolderPath = Environment.getExternalStorageDirectory() + "/" + cacheFolder;
            final String finalFileFolderPath = finalFileRootFolderPath + "/" + TimeUtils.getFileFolderNameByTime();
            //校验文件夹是否存在,不存在则创建
            File fileFolder = new File(finalFileFolderPath);
            if (!fileFolder.exists()) {
                fileFolder.mkdirs();
            }
            //校验文件是否存在
            String cacheFilePath = finalFileFolderPath + "/" + cacheFileName + ".txt";
            File cacheFile = new File(cacheFilePath);
            if (!cacheFile.exists()) {
                cacheFile.createNewFile();
            }
            //追加文件写入新的堆栈信息
            //获取文件
            rfile = new RandomAccessFile(cacheFilePath, "rw");
            //获取文件通道
            channel = rfile.getChannel();
            channel.position(channel.size());
            //写入缓冲区
            byte[] allStackInfoBytes = allStackInfo.getBytes();
            ByteBuffer buff = ByteBuffer.wrap(allStackInfoBytes);
            buff.put(allStackInfoBytes);
            buff.flip();
            //写入文件
            channel.write(buff);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关闭流,刷新到文件
            if (rfile != null) {
                try {
                    rfile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 用于处理收集消息
     *
     * @param stackInfo 待缓存堆栈信息
     */
    private void handleLogExecutorCollectionMessage(Object stackInfo) {
        //收集堆栈信息
        startCollectionLogTask(stackInfo);
    }

    /**
     * 发送给LogExecutor 消息
     *
     * @param msg
     */
    public void sendLogExecutorMessage(Message msg) {
        if (logExecutorHandler == null || msg == null || msg.getTarget() == null) {
            return;
        }
        logExecutorHandler.sendMessage(msg);
    }

    /**
     * 创建收集的消息
     *
     * @param stackInfo 堆栈消息
     */
    public Message obtainCollectionMessage(Object stackInfo) {
        if (stackInfo == null) {
            return null;
        }
        Message collectionMsg = Message.obtain(logExecutorHandler, TYPE_COLLECTION, stackInfo);
        return collectionMsg;
    }

    /**
     * 创建输出的消息
     */
    public Message obtainOutputMessage() {
        Message outputMsg = Message.obtain(logExecutorHandler, TYPE_OUTPUT, null);
        return outputMsg;
    }

    /**
     * 开始日志收集任务
     *
     * @param stackInfo 堆栈信息对象
     */
    private void startCollectionLogTask(Object stackInfo) {
        //校验数据类型是否正确
        if (!(stackInfo instanceof StackTraceElement[])) {
            return;
        }
        StackTraceElement[] stackTraceElements = (StackTraceElement[]) stackInfo;

        //检验堆栈信息
        if (stackTraceElements == null || stackTraceElements.length == 0) {
            return;
        }

        //初始化Log信息
        logStackInfoBuilder.delete(0, logStackInfoBuilder.length());
        logStackInfoBuilder.append("---------------------------------------------------");
        logStackInfoBuilder.append("\n");

        //追加堆栈信息
        boolean hasUsefulInfo = false;
        for (StackTraceElement mStackInfo : stackTraceElements) {
            String info = mStackInfo.toString();
            if (checkInfoUseful(info)) {
                logStackInfoBuilder.append(info);
                logStackInfoBuilder.append("\n");
                hasUsefulInfo = true;
            }
        }
        logStackInfoBuilder.append("---------------------------------------------------");
        logStackInfoBuilder.append("\n");

        //判断是否需要添加
        //获取当前堆栈信息,存储到队列
        String currentStackInfo = logStackInfoBuilder.toString();
        //获取上一个堆栈信息比较是否相同
        String lastInfo = logStackInfoQueue.isEmpty() ? "" : logStackInfoQueue.get(logStackInfoQueue.size() - 1);
        //不相同且有有效内容
        boolean isNeedAdd = hasUsefulInfo && !currentStackInfo.equals(lastInfo);
        if (isNeedAdd) {
            //将当前堆栈信息存储到队列中
            logStackInfoQueue.add(currentStackInfo);
        }
        //判断存储队列是否已经超出限制,视情况移除队列前的内容,在队尾增加
        if (logStackInfoQueue.size() > cacheDataSize) {
            logStackInfoQueue.remove(0);
        }
    }

    /**
     * 校验信息是否有效
     */
    private boolean checkInfoUseful(String info) {
        //未设置关键词默认为全通过
        if (keyWords == null || keyWords.length == 0) {
            return true;
        }
        //校验内容是否有效,无效直接
        if (TextUtils.isEmpty(info)) {
            return false;
        }
        //校验是否包含关键词,包含返回true,反之返回false
        for (String keyWord : keyWords) {
            if (info.contains(keyWord)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 关闭执行
     */
    public void stop() {
        if (logExecutorHandler != null) {
            logExecutorHandler.removeCallbacksAndMessages(null);
            logExecutorHandler = null;
        }
        if (logExecutorThread != null) {
            logExecutorThread.quit();
            logExecutorThread = null;
        }
    }

    //----------------基础信息的配置函数----------------

    /**
     * 设置缓存数量
     *
     * @param cacheDataSize 缓存数量
     */
    public void setCacheDataSize(int cacheDataSize) {
        this.cacheDataSize = cacheDataSize;
    }

    /**
     * 设置日志输出TAG
     *
     * @param tag tag
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * 设置是否需要缓存到本地文件
     *
     * @param needCacheToFile true:需要 false:不需要
     */
    public void setNeedCacheToFile(boolean needCacheToFile) {
        isNeedCacheToFile = needCacheToFile;
    }

    /**
     * 设置缓存文件夹的名称
     *
     * @param cacheFolder 缓存文件夹名称
     */
    public void setCacheFolder(String cacheFolder) {
        this.cacheFolder = cacheFolder;
    }

    /**
     * 设置缓存文件名称
     *
     * @param cacheFileName 缓存文件名
     */
    public void setCacheFileName(String cacheFileName) {
        this.cacheFileName = cacheFileName;
    }

    /**
     * 设置过滤关键词 （排除不是关键词内的内容）
     *
     * @param keyWords 关键词
     */
    public void setKeyWords(String[] keyWords) {
        this.keyWords = keyWords;
    }
}
