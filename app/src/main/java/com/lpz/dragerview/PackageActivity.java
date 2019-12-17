package com.lpz.dragerview;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PackageActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerViewAdapter mAdapter = null;
    List<DataBean> datas = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_fragment_layout);
        recyclerView = findViewById(R.id.recycler_view);
        init();
    }

    private void init() {
        GridLayoutManager  mGridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(mGridLayoutManager);

        for (int i = 0; i < 2; i++) {
            datas.add(new DataBean("ddd" + i));
        }
        mAdapter = new RecyclerViewAdapter(this, datas);
        recyclerView.setAdapter(mAdapter);
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.VH> {
        private Context mContext;
        private List<DataBean> mData;

        private void addItem(DataBean item) {
            mData.add(item);
            notifyItemInserted(mData.size() - 1);
            //mGridLayoutManager.scrollToPositionWithOffset(mData.size() - 1, 0);
            recyclerView.smoothScrollToPosition(mData.size() - 1);
        }

        private void removeItem(DataBean item) {
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
        public RecyclerViewAdapter.VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View binding = LayoutInflater.from(mContext).inflate(R.layout.item_layout, parent, false);
            return new RecyclerViewAdapter.VH(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerViewAdapter.VH holder, int position) {
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

                return true;
            }
        }

    }
}
