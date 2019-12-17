package com.lpz.dragerview;

import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

/**
 * Created by Lisen.Liu on 2018/11/2.
 */

public class MyViewPagerAdapter extends FragmentStatePagerAdapter implements DragLayerLayout.IDragActionCallback<DataBean> {
    private DragLayerLayout mDragLayerLayout;
    private String TAG="MyViewPagerAdapter";
    public MyViewPagerAdapter(FragmentManager fm, DragLayerLayout dragLayerLayout) {
        super(fm);
        mDragLayerLayout = dragLayerLayout;
    }

    private SparseArray<Fragment> mFragments = new SparseArray<>();

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = mFragments.get(position);
        if (fragment == null) {
            fragment = MyFragment.getInstance(mDragLayerLayout, position, 5 + position);
            mFragments.put(position, fragment);
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 20;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "TAB-" + position;
    }

    // ----- BEGIN DragLayerLayout.IDragActionCallback<String> -----


    @Override
    public void onStartDrag(Bitmap dragBitmap, View dragView, int startPage, int startPosition, DataBean data) {
        Log.i(TAG, "onStartDrag " + " startPage=[" + startPage + "], startPosition=[" + startPosition + "], data=[" + data + "]");
        Fragment dragFragment = mFragments.get(startPage);
        if (dragFragment != null && dragFragment instanceof DragLayerLayout.IDragDataCallback) {
            Log.i(TAG, "onStartDrag, success.");
            ((DragLayerLayout.IDragDataCallback) dragFragment).removeItem(data, startPosition);
            ((DragLayerLayout.IDragDataCallback) dragFragment).addTmpItem(dragBitmap, startPosition);
        } else {
            Log.e(TAG, "onStartDrag, fail.");
        }
    }

    @Override
    public void onDrag(Bitmap dragBitmap, View dragView, int startPage, int startPosition, int lastPage, int lastPosition, int currentPage, int currentPosition, DataBean data) {
        Log.i(TAG, "onDrag, startPage=[" + startPage + "], startPosition=[" + startPosition + "], lastPage=[" + lastPage + "], lastPosition=[" + lastPosition + "], currentPage=[" + currentPage + "], currentPosition=[" + currentPosition + "], data=[" + data + "]");
        Fragment dragFragment = mFragments.get(lastPage);
        Fragment dropFragment = mFragments.get(currentPage);
        if (dragFragment != null && dragFragment instanceof DragLayerLayout.IDragDataCallback
                && dropFragment != null && dropFragment instanceof DragLayerLayout.IDragDataCallback) {
            Log.i(TAG, "onDrag, success.");
            ((DragLayerLayout.IDragDataCallback) dragFragment).removeTmpItem(lastPosition);
            ((DragLayerLayout.IDragDataCallback) dropFragment).addTmpItem(dragBitmap, currentPosition);
        } else {
            Log.e(TAG, "onDrag, fail.");
        }
    }

    @Override
    public void onDrop(View dragView, int startPage, int startPosition, int lastPage, int lastPosition, int currentPage, int currentPosition, DataBean data) {
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
            ((DragLayerLayout.IDragDataCallback) dropFragment).doSwipeData(startPage, startPosition, currentPage, currentPosition, data);
        }
    }


    // ----- END DragLayerLayout.IDragActionCallback<String> -----
}
