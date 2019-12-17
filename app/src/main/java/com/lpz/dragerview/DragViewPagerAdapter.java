package com.lpz.dragerview;

import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.View;

import java.util.List;



/**
 * Created by xumin on 2018/11/7
 */
public abstract class DragViewPagerAdapter<T> extends FragmentStatePagerAdapter implements DragLayerLayout.IDragActionCallback<T> {
    private static final String TAG = DragViewPagerAdapter.class.getSimpleName();

    private List<String> titles;
    protected List<Fragment> mFragments;

    public DragViewPagerAdapter(FragmentManager fm, List<Fragment> fragments, List<String> titles) {
        super(fm);
        this.mFragments = fragments;
        this.titles = titles;
    }


    @Override
    public Fragment getItem(int position) {
        Fragment fragment = mFragments.get(position);
        return fragment;
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (titles != null && titles.size() > position && position >= 0) {
            return titles.get(position);
        } else {
            return null;
        }
    }


    @Override
    public void onStartDrag(Bitmap bitmap, View dragView, int startPage, int startPosition, T data) {
        Log.i(TAG, "onStartDrag " + " startPage=[" + startPage + "], startPosition=[" + startPosition + "], data=[" + data + "]");
        Fragment dragFragment = mFragments.get(startPage);
        if (dragFragment != null && dragFragment instanceof DragLayerLayout.IDragDataCallback) {
            Log.i(TAG, "onStartDrag, success.");
            ((DragLayerLayout.IDragDataCallback) dragFragment).removeItem(data, startPosition);
            ((DragLayerLayout.IDragDataCallback) dragFragment).addTmpItem(bitmap, startPosition);
        } else {
            Log.e(TAG, "onStartDrag, fail.");
        }
    }

    @Override
    public void onDrag(Bitmap bitmap, View dragView, int startPage, int startPosition, int lastPage, int lastPosition, int currentPage, int currentPosition, T data) {
        Log.i(TAG, "onDrag, startPage=[" + startPage + "], startPosition=[" + startPosition + "], lastPage=[" + lastPage + "], lastPosition=[" + lastPosition + "], currentPage=[" + currentPage + "], currentPosition=[" + currentPosition + "], data=[" + data + "]");
        Fragment dragFragment = mFragments.get(lastPage);
        Fragment dropFragment = mFragments.get(currentPage);
        if (dragFragment != null && dragFragment instanceof DragLayerLayout.IDragDataCallback
                && dropFragment != null && dropFragment instanceof DragLayerLayout.IDragDataCallback) {
            Log.i(TAG, "onDrag, success.");
            ((DragLayerLayout.IDragDataCallback) dragFragment).removeTmpItem(lastPosition);
            ((DragLayerLayout.IDragDataCallback) dropFragment).addTmpItem(bitmap, currentPosition);
        } else {
            Log.e(TAG, "onDrag, fail.");
        }
    }

    @Override
    public void onDrop(View dragView, int startPage, int startPosition, int lastPage, int lastPosition, int currentPage, int currentPosition, T data) {
        Log.i(TAG, "onDrop, startPage=[" + startPage + "], startPosition=[" + startPosition + "], lastPage=[" + lastPage + "], lastPosition=[" + lastPosition + "], currentPage=[" + currentPage + "], currentPosition=[" + currentPosition + "], data=[" + data + "]");
        Fragment dragFragment = mFragments.get(lastPage);
        Fragment dropFragment = mFragments.get(currentPage);
        if (dragFragment != null && dragFragment instanceof DragLayerLayout.IDragDataCallback
                && dropFragment != null && dropFragment instanceof DragLayerLayout.IDragDataCallback) {
            Log.i(TAG, "onDrop, success.");
            ((DragLayerLayout.IDragDataCallback) dragFragment).removeTmpItem(lastPosition);
            ((DragLayerLayout.IDragDataCallback) dropFragment).addItem(data, currentPosition);
        } else {
            Log.e(TAG, "onDrop, fail.");
        }
        if (startPage != currentPage || startPosition != currentPosition) {
            doSwipeData(startPage, startPosition, currentPage, currentPosition, data);
        }
    }

    protected abstract void doSwipeData(int fromPage, int fromPosition, int toPage, int toPosition, T data);
}
