package com.lpz.dragerview.thread.script;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.ArrayList;

/**
 * Created by hef on 2018/10/15.
 */

public abstract class ScriptTask {

    public static final int ERROR_CODE_OK = 0;
    public static final int ERROR_CODE_TIME_OUT = 1;
    public static final int ERROR_CODE_SCRIPT_ERROR = 2;
    public static final int ERROR_CODE_EXIT_ABNORMAL = 3;
    public static final int ERROR_CODE_CANCEL = 4;

    public static final int ERROR_CODE_BASE_CUSTOM = 100;  // 自定义的错误码都以这个值作为基准值

    private static final int EVENT_NEXT_STEP = 1;
    private static final int EVENT_TIME_OUT = 100;
    private static final int EVENT_EXIT_SUCCESS = 200;
    private static final int EVENT_EXIT_FAILED = 201;

    protected abstract String getTaskName();  // 任务名称，也是执行线程的名称
    protected abstract int getTimeout();
    protected abstract void setScripts();  // 设置好执行脚本

    private ArrayList<ScriptAction> mScripts = new ArrayList<>();

    private HandlerThread mExecutor;
    private Handler mHandler;

    private int mCurrentStep = 0;

    ITaskCallback mCallback;

    public void start(ITaskCallback callback){
        setScripts();
        mCurrentStep = 0;
        mCallback = callback;
        mExecutor = new HandlerThread(getTaskName());
        mExecutor.start();
        mHandler = new Handler(mExecutor.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what){
                    case EVENT_TIME_OUT:
                        onFailed(ERROR_CODE_TIME_OUT);
                        break;
                    case EVENT_NEXT_STEP:
                        doNext();
                        break;
                    case EVENT_EXIT_SUCCESS:
                        onSuccess(msg.arg1, msg.arg2, msg.obj);
                        break;
                    case EVENT_EXIT_FAILED:
                        onFailed(msg.arg1);
                        break;
                }
            }
        };
        mHandler.sendEmptyMessageDelayed(EVENT_TIME_OUT, getTimeout());
    }

    public void stop(){
        notifyFailed(ERROR_CODE_CANCEL);
    }

    protected void go(){
        if(mHandler != null) {
            mHandler.sendEmptyMessage(EVENT_NEXT_STEP);
        }
    }

    protected void go(Object... params){
        if(mHandler != null) {
            ScriptAction action = getNextAction();
            if(action != null){
                action.addParams(params);
                mHandler.sendEmptyMessage(EVENT_NEXT_STEP);
            } else {
                notifyFailed(ERROR_CODE_SCRIPT_ERROR);
            }
        }
    }

    protected void go(int delayed){
        if(mHandler != null) {
            mHandler.sendEmptyMessageDelayed(EVENT_NEXT_STEP, delayed);
        }
    }

    protected void go(int delayed, Object... params){
        if(mHandler != null) {
            ScriptAction action = getNextAction();
            if(action != null){
                action.addParams(params);
                mHandler.sendEmptyMessageDelayed(EVENT_NEXT_STEP, delayed);
            } else {
                notifyFailed(ERROR_CODE_SCRIPT_ERROR);
            }
        }
    }

    private ScriptAction getNextAction(){
        if(mScripts != null && mCurrentStep >= 0 && mCurrentStep+1 <= mScripts.size()){
            ScriptAction action = mScripts.get(mCurrentStep);
            return action;
        } else {
            return null;
        }
    }

    private void doNext(){
        ScriptAction action = getNextAction();
        if(action != null){
            action.execute(action.mParams);
            mCurrentStep++;
        } else {
            onFailed(ERROR_CODE_SCRIPT_ERROR);
        }
    }

    protected void addScript(ScriptAction action){
        mScripts.add(action);
    }

    protected void addScript(int index, ScriptAction action){
        mScripts.add(index, action);
    }

    protected abstract class ScriptAction{
        private String mTag;
        private Object[] mParams;

        public ScriptAction setTag(String tag){
            mTag = tag;
            return this;
        }

        public String getTag(){
            return mTag;
        }

        public ScriptAction addParams(Object... params){
            mParams = params;
            return this;
        }

        protected abstract void execute(Object... params);
    }

    // 只能在Task的执行线程中调用
    private void onSuccess(int arg1, int arg2, Object obj){
        if(mCallback != null){
            mCallback.onResult(ERROR_CODE_OK, arg1, arg2, obj);
        }

        exit();
    }

    // 只能在Task的执行线程中调用
    private void onFailed(int errorCode){
        if(mCallback != null){
            mCallback.onResult(errorCode, 0, 0, null);
        }

        exit();
    }

    private void exit(){
        if(mExecutor !=  null){
            mExecutor.getLooper().quit();
            mExecutor = null;
            mHandler = null;
        }
        mCallback = null;
    }


    protected void notifySuccess(){
        if(mHandler != null){
            mHandler.sendEmptyMessage(EVENT_EXIT_SUCCESS);
        }
    }

    protected void notifySuccessDelayed(int delayed){
        if(mHandler != null){
            mHandler.sendEmptyMessageDelayed(EVENT_EXIT_SUCCESS, delayed);
        }
    }

    protected void notifySuccess(int arg1, int arg2, Object obj){
        if(mHandler != null){
            Message msg = new Message();
            msg.what = EVENT_EXIT_SUCCESS;
            msg.arg1 = arg1;
            msg.arg2 = arg2;
            msg.obj = obj;
            mHandler.sendMessage(msg);
        }
    }

    protected void notifySuccessDelayed(int arg1, int arg2, Object obj, int delayed){
        if(mHandler != null){
            Message msg = new Message();
            msg.what = EVENT_EXIT_SUCCESS;
            msg.arg1 = arg1;
            msg.arg2 = arg2;
            msg.obj = obj;
            mHandler.sendMessageDelayed(msg, delayed);
        }
    }

    protected void notifyFailed(int errorCode){
        if(mHandler != null){
            Message msg = new Message();
            msg.what = EVENT_EXIT_FAILED;
            msg.arg1 = errorCode;
            mHandler.sendMessage(msg);
        }
    }

    protected void notifyFailedDelayed(int errorCode, int delayed){
        if(mHandler != null){
            Message msg = new Message();
            msg.what = EVENT_EXIT_FAILED;
            msg.arg1 = errorCode;
            mHandler.sendMessageDelayed(msg, delayed);
        }
    }

    interface ITaskCallback{
        void onResult(int errorCode, int arg1, int arg2, Object obj);
    }
}
