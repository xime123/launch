package com.lpz.dragerview.thread;

import android.support.annotation.NonNull;

import java.util.concurrent.ThreadFactory;

public class LogicThreadFactory implements ThreadFactory
{
    @Override
    public Thread newThread(@NonNull Runnable r)
    {
        Thread thread = new Thread(r);
        thread.setName(String.format("%s-%d", "HDLogicThread", thread.getId()));
        return thread;
    }
}
