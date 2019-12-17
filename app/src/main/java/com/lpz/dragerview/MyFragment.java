package com.lpz.dragerview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;



public class MyFragment extends Fragment implements DragLayerLayout.IDragDataCallback<DataBean> {
    private RecyclerView recyclerView;
    private DragLayerLayout<DataBean> mDragLayerLayout;
    private String mPrefixName;
    private int count;
    List<DataBean> datas = new ArrayList<>();

    public static MyFragment getInstance(DragLayerLayout dragLayerLayout, int id, int count) {
        MyFragment fragment = new MyFragment();
        fragment.mDragLayerLayout = dragLayerLayout;
        fragment.mPrefixName = "G" + id + "-";
        fragment.count = count;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.my_fragment_layout, null);
        ;
        recyclerView = view.findViewById(R.id.recycler_view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private RecyclerViewAdapter mAdapter = null;
    private GridLayoutManager mGridLayoutManager;

    private void init() {
        Activity activity = getActivity();
        mGridLayoutManager = new GridLayoutManager(activity, 3);
        recyclerView.setLayoutManager(mGridLayoutManager);

        for (int i = 0; i < count; i++) {
            datas.add(new DataBean(mPrefixName + i));
        }
        mAdapter = new RecyclerViewAdapter(activity, datas);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void addItem(DataBean data, int position) {
        mAdapter.addItem(data, position);
    }

    @Override
    public void removeItem(DataBean data, int position) {
        mAdapter.removeItem(data,position);
    }

    @Override
    public void addTmpItem(Bitmap map, int position) {

    }

    @Override
    public void removeTmpItem(int position) {

    }

    @Override
    public void doSwipeData(int startPage, int startPosition, int currentPage, int currentPosition, DataBean data) {

    }


    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.VH> {
        private Context mContext;
        private List<DataBean> mData;

        private void addItem(DataBean item,int pos) {
            mData.add(item);
            notifyItemInserted(pos);
            //mGridLayoutManager.scrollToPositionWithOffset(mData.size() - 1, 0);
            recyclerView.smoothScrollToPosition(pos);
        }

        private void removeItem(DataBean item,int pos) {
            int index = mData.lastIndexOf(item);
            if (index >= 0) {
                mData.remove(index);
                notifyItemRemoved(index);
            }
        }

        public RecyclerViewAdapter(Context context, List<DataBean> data) {
            mContext = context;
            mData = data;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View binding = LayoutInflater.from(mContext).inflate(R.layout.item_layout, parent, false);
            return new VH(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.bindViewInfo(position);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        class VH extends RecyclerView.ViewHolder implements View.OnLongClickListener {

            private DataBean data;
            private int position;

            public VH(View binding) {
                super(binding);
                binding.setOnLongClickListener(this);

            }

            private void bindViewInfo(int position) {
                this.position = position;
                data = mData.get(position);
                ImageView icon = itemView.findViewById(R.id.icon);
                TextView name = itemView.findViewById(R.id.name);
                icon.setImageResource(data.resId);
                name.setText(data.name);
            }

            @Override
            public boolean onLongClick(View v) {
                mDragLayerLayout.startDrag(v, data, position,true);
                return true;
            }
        }

    }

}
