package com.lpz.dragerview.launcher;

import android.content.Context;
import android.view.ViewGroup;

import com.lpz.dragerview.R;
import com.lpz.dragerview.ScreenUtils;
import com.lpz.dragerview.adapter.MultiItemTypeAdapter;
import com.lpz.dragerview.adapter.base.ItemViewDelegate;
import com.lpz.dragerview.adapter.base.ViewHolder;

import java.util.List;




public class HomeDeviceAdapter extends MultiItemTypeAdapter<HomeDataBean> {
    private static final String TAG = HomeDeviceAdapter.class.getSimpleName();


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemViewDelegate itemViewDelegate = mItemViewDelegateManager.getItemViewDelegate(viewType);
        int layoutId = itemViewDelegate.getItemViewLayoutId();
        ViewHolder holder = ViewHolder.createViewHolder(mContext, parent, layoutId);
        onViewHolderCreated(holder, holder.getConvertView());
        setListener(parent, holder, viewType);
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.width = ScreenUtils.getScreenWith() / 2 - ScreenUtils.dpToPx(3);
        layoutParams.height = (int) ((float) layoutParams.width / 0.89f);
        return holder;
    }

    public HomeDeviceAdapter(Context context, List<HomeDataBean> datas) {
        super(context, datas);
        addItemViewDelegate(new FolderItemViewDelegateImpl(context, R.layout.collapse_folder_layout));
        addItemViewDelegate(new DefaultItemViewDelegateImpl(context, R.layout.collapse_common_item));

    }


}
