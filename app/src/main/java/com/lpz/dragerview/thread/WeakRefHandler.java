package com.lpz.dragerview.thread;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

public class WeakRefHandler<T> extends Handler {
    private WeakReference<T> mWeakReference;
    private Callback mCallback;

    public WeakRefHandler(Looper looper, T t, Callback callback) {
        super(looper);
        mCallback = callback;
        mWeakReference = new WeakReference<>(t);
    }

    @Override
    public void handleMessage(Message msg) {
        if (isAlive() && mCallback != null) {
            mCallback.handleMessage(msg);
        }
    }

    /**
     * 是否还存活
     *
     * @return
     */
    public boolean isAlive() {
        T t = mWeakReference.get();
        return t != null;
    }
}
