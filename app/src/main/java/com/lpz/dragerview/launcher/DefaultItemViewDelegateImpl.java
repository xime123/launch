package com.lpz.dragerview.launcher;

import android.content.Context;


import com.lpz.dragerview.R;
import com.lpz.dragerview.adapter.base.ItemViewDelegate;
import com.lpz.dragerview.adapter.base.ViewHolder;



public class DefaultItemViewDelegateImpl  implements ItemViewDelegate<HomeDataBean> {
    private int layoutId;
    private Context context;

    public DefaultItemViewDelegateImpl(Context context, int layoutId) {
        this.layoutId = layoutId;
        this.context = context;
    }

    @Override
    public int getItemViewLayoutId() {
        return layoutId;
    }

    @Override
    public boolean isForViewType(HomeDataBean item, int position) {
        HomeDataBean.FolderBean folderBean = item.getFolderBean();
        if (folderBean == null) return true;
        return false;

    }

    @Override
    public void convert(final ViewHolder holder, final HomeDataBean o, int position) {


        holder.setText(R.id.tv_device_name, o.getDataBean().getName());

    }
}
