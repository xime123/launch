package com.lpz.dragerview.launcher;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lpz.dragerview.R;
import com.lpz.dragerview.adapter.CommonAdapter;
import com.lpz.dragerview.adapter.base.ItemViewDelegate;
import com.lpz.dragerview.adapter.base.ViewHolder;

import java.util.ArrayList;
import java.util.List;


public class FolderItemViewDelegateImpl implements ItemViewDelegate<HomeDataBean> {
    private int layoutId;
    private Context context;

    public FolderItemViewDelegateImpl(Context context, int layoutId) {
        this.layoutId = layoutId;
        this.context = context;
    }

    @Override
    public int getItemViewLayoutId() {
        return layoutId;
    }

    @Override
    public boolean isForViewType(HomeDataBean item, int position) {
        return item.getFolderBean() != null;

    }

    @Override
    public void convert(final ViewHolder holder, final HomeDataBean o, int position) {

        RecyclerView recyclerView = holder.getView(R.id.rv_folder);
        final List<HomeDataBean.DataBean> roomDevices = o.getFolderBean().getNameList();
        List<HomeDataBean.DataBean> fixSizeDeviceList = new ArrayList<>();
        if (roomDevices.size() > 4) {
            fixSizeDeviceList.addAll(roomDevices.subList(0, 4));
        } else {
            fixSizeDeviceList.addAll(roomDevices);
        }
        holder.getView(R.id.rv_click_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.getConvertView().performClick();
            }
        });
        holder.getView(R.id.rv_click_view).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                holder.getConvertView().performLongClick();
                return true;
            }
        });
        CommonAdapter<HomeDataBean.DataBean> adapter = new CommonAdapter<HomeDataBean.DataBean>(context, R.layout.item_collapse_folder_item_layout, fixSizeDeviceList) {
            @Override
            protected void convert(ViewHolder innerHolder, HomeDataBean.DataBean roomDevice, int position) {

            }
        };
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        recyclerView.setAdapter(adapter);

    }
}
