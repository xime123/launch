package com.lpz.dragerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.lpz.dragerview.adapter.CommonAdapter;

import java.util.List;




/**
 * Created by xumin on 2018/11/7
 */
public abstract class DragRecycleViewAdapter<T> extends CommonAdapter<T> implements DragLayerLayout.IDragDataCallback<T> {
    private static final String TAG = DragRecycleViewAdapter.class.getSimpleName();

    protected Bitmap mTmpItemSnapShot;

    public DragRecycleViewAdapter(Context context, int layoutId, List<T> datas) {
        super(context, layoutId, datas);
    }


    @Override
    public void addItem(T item, int position) {
        Log.i(TAG, "0. addItem item=" + item + ", position=" + position);
        if (position >= 0 && position <= mDatas.size()) {
            Log.i(TAG, "1. addItem, add toPosition " + position);
            mDatas.add(position, item);
            notifyItemInserted(position);
        } else {
            Log.i(TAG, "2. addItem, add to last position ");
            mDatas.add(item);
            notifyItemInserted(mDatas.size() - 1);
        }
    }

    @Override
    public void removeItem(T item, int position) {
        Log.i(TAG, "0. removeItem item=" + item + ", position=" + position);
        if (position >= 0 & position < mDatas.size()) {
            T bean = mDatas.get(position);
            if (bean == item) {
                Log.i(TAG, "1. removeItem, position=" + position);
                mDatas.remove(position);
                notifyItemRemoved(position);
                return;
            }
        }

        int index = mDatas.lastIndexOf(item);
        if (index >= 0) {
            Log.i(TAG, "2. removeItem, index=" + index);
            mDatas.remove(index);
            notifyItemRemoved(index);
            return;
        }

        Log.e(TAG, "3. removeItem, can't find the item");
    }

    @Override
    public void addTmpItem(Bitmap map, int position) {
        Log.i(TAG, "addTmpItem, position=" + position);
        mTmpItemSnapShot = map;
        position = (position >= 0 & position < mDatas.size()) ? position : mDatas.size();
        mDatas.add(position, null);
        notifyItemInserted(position);
    }

    @Override
    public void removeTmpItem(int position) {
        Log.i(TAG, "0. removeTmpItem, position=" + position);
        mTmpItemSnapShot = null;
        if (position >= 0 && position < mDatas.size()) {
            Log.i(TAG, "1. removeTmpItem, position=" + position);
            mDatas.remove(position);
            notifyItemRemoved(position);
            return;
        } else {
            int pos = mDatas.indexOf(null);
            if (pos >= 0) {
                Log.i(TAG, "2. removeTmpItem, pos=" + pos);
                mDatas.remove(pos);
                notifyItemRemoved(pos);
                return;
            }
        }
        Log.e(TAG, "3. removeTmpItem, can't find the remove item.");
    }
}
