package com.lpz.dragerview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by 061717190 on 2017/2/10.
 */
public abstract class BaseEvergrandeAdapter<T extends BaseEvergrandeAdapter.BaseViewHolder, D> extends BaseAdapter {
    protected Context mContext;
    private List<D> mDataList;
    protected boolean mIsRefresh = false;
    HashMap<D, Integer> mIdMap = new HashMap<D, Integer>();

    public BaseEvergrandeAdapter(List<D> list) {
        mDataList = list;
        for (int i = 0; i < list.size(); ++i) {
            mIdMap.put(list.get(i), i);
        }
    }

    /**
     * 局部更新数据，调用一次getView()方法；Google推荐的做法
     *
     * @param listView 要更新的listview
     * @param position 要更新的位置
     */
    public void notifyItemChanged(ListView listView, int position) {
        /**第一个可见的位置，减1是因为有些若隐若现的item判断会有问题**/
        int firstVisiblePosition = listView.getFirstVisiblePosition() - 1;
        /**最后一个可见的位置**/
        int lastVisiblePosition = listView.getLastVisiblePosition();

        if (position < 0) {
            position = 0;
        }
        /**在看见范围内才更新，不可见的滑动后自动会调用getView方法更新**/
        if (position >= firstVisiblePosition && position <= lastVisiblePosition) {
            /**获取指定位置view对象**/
            View view = listView.getChildAt(position - firstVisiblePosition);
            if (view != null) {
                mIsRefresh = true;
                getView(position, view, listView);
            }
        }
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public D getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= mIdMap.size()) {
            return -1;
        }
        D item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        T viewHolder;
        if (convertView == null) {
            if (mContext == null) {
                mContext = parent.getContext();
            }
            viewHolder = createViewHolder(LayoutInflater.from(mContext), position);
            convertView = viewHolder.mView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (T) convertView.getTag();
        }
        onBindViewHolder(viewHolder, position);
        return convertView;
    }

    public static abstract class BaseViewHolder {
        public final View mView;

        protected BaseViewHolder(View view) {
            this.mView = view;
        }

        public <K extends View> K findView(int id) {
            return (K) mView.findViewById(id);
        }
    }

    public List<D> getDataList() {
        return mDataList;
    }

    public void setDataList(List<D> list) {
        mDataList = list;
        mIdMap.clear();
        for (int i = 0; i < list.size(); ++i) {
            mIdMap.put(list.get(i), i);
        }
        notifyDataSetChanged();
    }

    public void addItems(List<D> list) {
        mDataList.addAll(list);
        for (int i = 0; i < list.size(); ++i) {
            mIdMap.put(list.get(i), i);
        }
        notifyDataSetChanged();
    }

    public void removeItem(D item) {
        mDataList.remove(item);
        mIdMap.remove(item);
        notifyDataSetChanged();
    }

    public void removeItem(int index) {
        D item = mDataList.remove(index);
        mIdMap.remove(item);
    }

    public void addItem(D item) {
        mDataList.add(item);
        mIdMap.put(item, mDataList.size());
    }

    public void addItem(int index, D item) {
        mDataList.add(index, item);
        mIdMap.put(item, mDataList.size());
    }

    public void removeAll() {
        mDataList.clear();
        mIdMap.clear();
    }

    public void replaceItem(int index, D item) {
        refreshItem(index, item);
        notifyDataSetChanged();
    }

    /**
     * 需要手动调用notifyItemChanged刷新UI
     * @param index
     * @param item
     */
    public void refreshItem(int index, D item) {
        mDataList.set(index, item);
        mIdMap.put(item, index);
    }

    public int getPosition(D item) {
        return mIdMap.get(item);
    }

    public abstract T createViewHolder(LayoutInflater inflater, int position);

    public abstract void onBindViewHolder(T holder, int position);
}
