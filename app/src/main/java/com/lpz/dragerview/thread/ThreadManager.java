package com.lpz.dragerview.thread;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ****************************************************************************
 * Copyright (C) 2018-2023 HD Corporation. All rights reserved
 * File        : ThreadManager.java
 * <p>
 * Description : 线程统一管理入口
 * <p>
 * Creation    : 2018-05-22
 * Author      : huanghaoyi@evergrande/zhoulong1@evergrande.cn
 * History     : 2018-05-22, Creation
 * Notes：
 * 1，提供主线程和逻辑线程API
 * 2，提供线程池API接口，分逻辑线程池和背景线程池两种
 * ****************************************************************************
 */
public class ThreadManager {
    private static final int MAX_SIZE_BACKGROUND_THREAD_POOL = 2;
    private static ThreadManager mInstance = null;
    private Handler mMainHandler = null; //主线程handler
    private Handler mLogicHandler = null; //logic线程handler，为减轻主线程负担
    private HandlerThread mLogicHandlerThread = null; //logic线程
    private ExecutorService mWorkThreadPool = null; //工作线程池，如网络请求
    private ExecutorService mBgThreadPool = null; //背景线程池，如io操作


    private ThreadManager() {
        getMainHandler();
        getLogicHandler();
        getWorkThreadPool();
        getBgThreadPool();
    }

    public static ThreadManager getInstance() {
        if (mInstance == null) {
            synchronized (ThreadManager.class) {
                if (mInstance == null) {
                    mInstance = new ThreadManager();
                }
            }
        }
        return mInstance;
    }

    public Handler getMainHandler() {
        if (mMainHandler == null) {
            synchronized (this) {
                if (mMainHandler == null) {
                    mMainHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return mMainHandler;
    }

    public void postUITask(Runnable runnable) {
        postDelayedUITask(runnable, 0);
    }

    public void postDelayedUITask(Runnable runnable, long delayTime) {
        if (mMainHandler != null) {
            mMainHandler.postDelayed(runnable, delayTime);
        }
    }

    public void postFrontUITask(Runnable runnable) {
        if (mMainHandler != null) {
            mMainHandler.postAtFrontOfQueue(runnable);
        }
    }

    public void removeUITask(Runnable runnable) {
        if (mMainHandler != null) {
            mMainHandler.removeCallbacks(runnable);
        }
    }

    public Handler getLogicHandler() {
        if (mLogicHandler == null) {
            synchronized (this) {
                if (mLogicHandler == null) {
                    mLogicHandlerThread = new HandlerThread("HD_LOGIC_THREAD");
                    mLogicHandlerThread.start();
                    mLogicHandler = new Handler(mLogicHandlerThread.getLooper());
                }
            }
        }
        return mLogicHandler;
    }

    public void postLogicTask(Runnable runnable) {
        postDelayedLogicTask(runnable, 0);
    }

    public void postDelayedLogicTask(Runnable runnable, long delayTime) {
        if (mLogicHandler != null) {
            mLogicHandler.postDelayed(runnable, delayTime);
        }
    }

    public void postFrontLogicTask(Runnable runnable) {
        if (mLogicHandler != null) {
            mLogicHandler.postAtFrontOfQueue(runnable);
        }
    }

    public void removeLogicTask(Runnable runnable) {
        if (mLogicHandler != null) {
            mLogicHandler.removeCallbacks(runnable);
        }
    }

    public ExecutorService getWorkThreadPool() {
        if (mWorkThreadPool == null) {
            synchronized (this) {
                if (mWorkThreadPool == null) {
                    //cpu密集型操作业界建议:最大线程数=处理器数*2+1
                    mWorkThreadPool = new ThreadPoolExecutor(4, calculateBestThreadCount() * 2 + 1,
                            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new LogicThreadFactory());
                }
            }
        }
        return mWorkThreadPool;
    }

    /**
     * @return 根据jvm当前的处理器个数决定线程池的最大线程个数
     */
    @SuppressWarnings("WeakerAccess")
    private int calculateBestThreadCount() {
        return RuntimeCompat.availableProcessors();
    }

    //设置工作线程池的最大线程个数
    public void setWorkThreadMaxPoolSize(int size) {
        if (mWorkThreadPool != null) {
            ((ThreadPoolExecutor) mWorkThreadPool).setMaximumPoolSize(size);
        }
    }

    //设置工作线程池的核心线程个数
    public void setWorkThreadPoolCorePoolSize(int size) {
        if (mWorkThreadPool != null) {
            ((ThreadPoolExecutor) mWorkThreadPool).setCorePoolSize(size);
        }
    }

    public void postWorkTask(Runnable runnable) {
        if (mWorkThreadPool != null) {
            mWorkThreadPool.execute(runnable);
        }
    }

    public ExecutorService getBgThreadPool() {
        if (mBgThreadPool == null) {
            synchronized (this) {
                mBgThreadPool = new ThreadPoolExecutor(0, MAX_SIZE_BACKGROUND_THREAD_POOL,
                        60L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new LogicThreadFactory());
            }
        }
        return mBgThreadPool;
    }

    public void postBgTask(Runnable runnable) {
        if (mBgThreadPool != null) {
            mBgThreadPool.execute(runnable);
        }
    }

    public boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }
}
