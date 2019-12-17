package com.lpz.dragerview;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by xumin on 2018/11/7
 */
public abstract class DragFragment<T> extends Fragment implements DragLayerLayout.IDragDataCallback<T> {
    protected DragLayerLayout<DataBean> mDragLayerLayout;

    public void setmDragLayerLayout(DragLayerLayout<DataBean> mDragLayerLayout) {
        this.mDragLayerLayout = mDragLayerLayout;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), null);
        return view;
    }

    protected abstract int getLayoutId();

    public abstract DragRecycleViewAdapter<T> getDragAdapter();

    @Override
    public void addItem(T data, int position) {
        getDragAdapter().addItem(data, position);
    }

    @Override
    public void removeItem(T data, int position) {
        getDragAdapter().removeItem(data, position);
    }

    @Override
    public void addTmpItem(Bitmap map, int position) {
        getDragAdapter().addTmpItem(map, position);
    }

    @Override
    public void removeTmpItem(int position) {
        getDragAdapter().removeTmpItem(position);
    }
}
