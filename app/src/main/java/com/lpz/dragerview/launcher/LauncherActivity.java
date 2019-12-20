package com.lpz.dragerview.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.lpz.dragerview.FROMTYPE;
import com.lpz.dragerview.MyDragLayerLayout;
import com.lpz.dragerview.MyRelativeLayout;
import com.lpz.dragerview.R;
import com.lpz.dragerview.ScreenUtils;
import com.lpz.dragerview.adapter.CommonAdapter;
import com.lpz.dragerview.adapter.base.ViewHolder;
import com.lpz.dragerview.thread.ThreadManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LauncherActivity extends AppCompatActivity {
    private final int TYPE_HOME=1;
    private final int TYPE_PACKAGE=2;
    private MyDragLayerLayout myDragLayerLayout;
    private ViewGroup packageView;
    private RecyclerView launcherRv;
    private RecyclerView packageRv;
    private List<HomeDataBean> homeList = new ArrayList<>();
    private List<HomeDataBean.DataBean> packList = new ArrayList<>();
    private HomeDeviceAdapter launcherAdapter;
    private CommonAdapter<HomeDataBean.DataBean> packageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        myDragLayerLayout=findViewById(R.id.root_drag);
        packageView = findViewById(R.id.pack_view);
        launcherRv=findViewById(R.id.rv_launcher);
        packageRv=findViewById(R.id.rv_package);
        initRecyclerView();
        initData();
    }

    private void initRecyclerView() {
        GridLayoutManager homeLayoutManager=new GridLayoutManager(this,2);
        GridLayoutManager packLayoutManager=new GridLayoutManager(this,4);

        launcherRv.addOnItemTouchListener(new LauncherItemClickListener(launcherRv));
        packageRv.addOnItemTouchListener(new PackageItemClickListener(packageRv));

        launcherRv.setLayoutManager(homeLayoutManager);
        packageRv.setLayoutManager(packLayoutManager);
        List<? extends RecyclerView.ItemDecoration> decorations = buildDefaultItemDecorations();
        if (decorations != null && decorations.size() > 0) {
            for (RecyclerView.ItemDecoration itemDecoration : buildDefaultItemDecorations()) {
                launcherRv.addItemDecoration(itemDecoration);
            }
        }
        updateDragLayout(TYPE_HOME,0);

        launcherRv.getRecycledViewPool().setMaxRecycledViews(0,0);
        launcherRv.getRecycledViewPool().setMaxRecycledViews(1,0);
    }

    private void updateDragLayout(int type,int  startPos){
        myDragLayerLayout.updateType(type);
        switch (type){
            case TYPE_HOME:
                myDragLayerLayout.setDragPosition(new LauncherDragListener());
                myDragLayerLayout.setBottomOffset(0);
                final int newStartPos=homeList.size()-1;//这里应该用文件夹右边的位置（adapter位置）

                myDragLayerLayout.updateRecyclerView(launcherRv,startPos, FROMTYPE.FROM_PACKAGE);
                break;
            case TYPE_PACKAGE:
                myDragLayerLayout.setDragPosition(new PackageDragListener());
                myDragLayerLayout.setBottomOffset(ScreenUtils.dpToPx(100));
                 int newPackStartPos=packList.size()-1;//这里应该用新建或者已建的文件夹位置的一下位置（adapter位置）
                myDragLayerLayout.updateRecyclerView(packageRv,startPos,FROMTYPE.FROM_HOME);
                break;
        }

    }

    private void initData() {
        for(int i=0;i<20;i++){
            HomeDataBean homeDataBean=new HomeDataBean();
            HomeDataBean.DataBean dataBean=new HomeDataBean.DataBean();
            dataBean.setName("设备"+i);
            homeDataBean.setDataBean(dataBean);
            homeList.add(homeDataBean);
        }
        launcherAdapter=new HomeDeviceAdapter(this,homeList);
        launcherRv.setAdapter(launcherAdapter);
        packageAdapter=new CommonAdapter<HomeDataBean.DataBean>(this,R.layout.item_package_layout, packList) {
            @Override
            protected void convert(ViewHolder holder, HomeDataBean.DataBean s, int position) {
//                if(s.isEmpty()){
//                    holder.getView(R.id.iv_package).setVisibility(View.INVISIBLE);
//                }else {
//                    holder.getView(R.id.iv_package).setVisibility(View.VISIBLE);
//                    holder.setText(R.id.tv_device_name, s.getName());
//                }

                holder.getView(R.id.iv_package).setVisibility(View.VISIBLE);
                holder.setText(R.id.tv_device_name, s.getName());
            }
        };
        packageRv.setAdapter(packageAdapter);
    }

    class LauncherItemClickListener extends OnRecyclerItemClickListener{

        public LauncherItemClickListener(RecyclerView recyclerView) {
            super(recyclerView);
        }

        @Override
        public void onItemClick(RecyclerView.ViewHolder vh, int pos) {
            HomeDataBean homeDataBean= homeList.get(pos);
            HomeDataBean.FolderBean folderBean= homeDataBean.getFolderBean();
            if(folderBean!=null){
                packList.clear();
                packList.addAll(folderBean.getNameList());
                packageAdapter.notifyDataSetChanged();
                showPackageView();
                ThreadManager.getInstance().postDelayedUITask(new Runnable() {
                    @Override
                    public void run() {
                        updateDragLayout(TYPE_PACKAGE,0);
                    }
                },200);
            }
        }

        @Override
        public void onItemLongClick(RecyclerView.ViewHolder vh, int pos) {
            myDragLayerLayout.startDrag(vh.itemView, vh.getAdapterPosition(), false,/*header.getHeight()*/0, launcherRv);
        }
    }

    class PackageItemClickListener extends OnRecyclerItemClickListener{

        public PackageItemClickListener(RecyclerView recyclerView) {
            super(recyclerView);
        }

        @Override
        public void onItemClick(RecyclerView.ViewHolder vh, int pos) {

        }

        @Override
        public void onItemLongClick(RecyclerView.ViewHolder vh, int pos) {
            myDragLayerLayout.startDrag(vh.itemView, vh.getAdapterPosition(), false,/*header.getHeight()*/0, packageRv);
        }
    }

    /**
     * 首页拖动
     */
    class LauncherDragListener implements MyDragLayerLayout.IDragPosition {

        @Override
        public void onDrop(View dragView, int startPosition, int lastPosition, int currentPosition, boolean out) {
            dragView.setVisibility(View.VISIBLE);
            for(HomeDataBean homeDataBean:homeList){
                homeDataBean.setNeedHide(false);
            }
            launcherAdapter.notifyDataSetChanged();
        }

        @Override
        public void onSwap(View dragView, int startPosition, int lastPosition, int currentPosition, boolean out) {
            if (startPosition == currentPosition) {
                return;
            }
            Log.i("mTest","onSwap==startPosition="+startPosition+" currentPosition="+currentPosition);
            if (currentPosition > homeList.size() - 1)
                currentPosition = homeList.size() - 1;

            int fromIndex = startPosition;
            int toIndex = currentPosition;
            HomeDataBean homeDataBean=homeList.get(fromIndex);
            homeDataBean.setNeedHide(true);
            if (fromIndex < toIndex) {
                if (toIndex >= homeList.size()) return;
                for (int i = fromIndex; i < toIndex; i++) {
                    Collections.swap(homeList, i, i + 1);
                }
            } else {
                if (fromIndex >= homeList.size()) return;
                for (int i = fromIndex; i > toIndex; i--) {
                    Collections.swap(homeList, i, i - 1);
                }
            }
            launcherAdapter.notifyItemMoved(startPosition, currentPosition);

        }

        @Override
        public void onInsert(View dragView, int startPosition, int lastPosition, int currentPosition) {
            packList.clear();
            HomeDataBean fromItem=homeList.get(startPosition);

            if(fromItem.getFolderBean()!=null){//文件夹不能合并
                return;
            }
            HomeDataBean.DataBean fromdata=fromItem.getDataBean();
            HomeDataBean toItem=homeList.get(currentPosition);
            final HomeDataBean.FolderBean folderBean;
            if(toItem.getFolderBean()!=null){
                //拖到文件夹里面
                folderBean = toItem.getFolderBean();
                List<HomeDataBean.DataBean> roomDeviceList = folderBean.getNameList();
                roomDeviceList.add(fromdata);
                homeList.remove(fromItem);
            }else {
                //拖到设备里面，需要创建文件夹
                final HomeDataBean.DataBean toRoomDevice = toItem.getDataBean();
                List<HomeDataBean.DataBean> roomDeviceList = new ArrayList<>();
                roomDeviceList.add(toRoomDevice);
                roomDeviceList.add(fromdata);
                folderBean = createFolder(roomDeviceList);
                HomeDataBean itemBean = new HomeDataBean();

                itemBean.setFolderBean(folderBean);
                homeList.remove(startPosition);
                final int index = startPosition < currentPosition ? currentPosition - 1 : currentPosition;
                homeList.set(index, itemBean);
            }
            //创建一个空元素

            packList.addAll(folderBean.getNameList());
            //2,更新packageList,并刷新
            packageAdapter.notifyDataSetChanged();
            launcherAdapter.notifyDataSetChanged();
            //1,显示package view
            showPackageView();

            //3,更新recyclerview
            ThreadManager.getInstance().postDelayedUITask(new Runnable() {
                @Override
                public void run() {
                    updateDragLayout(TYPE_PACKAGE,packList.size()-1);

                }
            },200);

        }

        @Override
        public void unPack(View dragView, int startPosition) {

        }
    }

    public HomeDataBean.FolderBean createFolder(List<HomeDataBean.DataBean> folderDeviceList) {
        HomeDataBean.FolderBean folderBean = new HomeDataBean.FolderBean();
        folderBean.setNameList(folderDeviceList);
        return folderBean;
    }

    private void showPackageView() {
        ValueAnimator animator=ObjectAnimator.ofFloat(0f,1f);
        animator.setDuration(400);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                packageView.setAlpha(0);
                packageView.setVisibility(View.VISIBLE);
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value= (float) animation.getAnimatedValue();
                packageView.setAlpha(value);
                packageView.setScaleX(value);
                packageView.setScaleY(value);
            }
        });
        animator.start();
    }

    private void hidePackageView() {
        ValueAnimator animator=ObjectAnimator.ofFloat(1.0f,0f);
        animator.setDuration(400);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                packageView.setAlpha(0);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                packageView.setVisibility(View.GONE);
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value= (float) animation.getAnimatedValue();
                packageView.setAlpha(value);
                packageView.setScaleX(value);
                packageView.setScaleY(value);
            }
        });
        animator.start();
    }

    /**
     * 文件夹拖动
     */
    class PackageDragListener implements MyDragLayerLayout.IDragPosition {

        @Override
        public void onDrop(View dragView, int startPosition, int lastPosition, int currentPosition, boolean out) {
//            if (out) {
//                //1,packageView 要隐藏
//                //2,recyclerview要更新
//                //3,新增item,插入到最后
//                //4，更新DraglayerLayout的StartPosition
//            } else {
//                dragView.setVisibility(View.VISIBLE);
//                for(HomeDataBean.DataBean dataBean:packList){
//                    dataBean.setEmpty(false);
//                }
//                packageAdapter.notifyDataSetChanged();
//            }

            dragView.setVisibility(View.VISIBLE);
            for(HomeDataBean.DataBean dataBean:packList){
                dataBean.setEmpty(false);
            }
            packageAdapter.notifyDataSetChanged();
        }

        @Override
        public void onSwap(View dragView, int startPosition, int lastPosition, int currentPosition, boolean out) {
            if (startPosition == currentPosition) {
                return;
            }
            if (currentPosition > packList.size() - 1){
                currentPosition = packList.size() - 1;

            }

            int fromIndex = startPosition;
            int toIndex = currentPosition;
            if (fromIndex < toIndex) {
                if (toIndex >= packList.size()) return;
                for (int i = fromIndex; i < toIndex; i++) {
                    Collections.swap(packList, i, i + 1);
                }
            } else {
                if (fromIndex >= packList.size()) return;
                for (int i = fromIndex; i > toIndex; i--) {
                    Collections.swap(packList, i, i - 1);
                }
            }
            packageAdapter.notifyItemMoved(startPosition, currentPosition);
        }

        @Override
        public void onInsert(View dragView, int startPosition, int lastPosition, int currentPosition) {

        }

        @Override
        public void unPack(View dragView, int startPosition) {
            hidePackageView();
            int index=-1;
            int pos=0;
            HomeDataBean.DataBean dataBean=packList.get(startPosition);
            for(int i=0;i<homeList.size();i++){
                HomeDataBean homeDataBean=homeList.get(i);
                HomeDataBean.FolderBean folderBean=homeDataBean.getFolderBean();
                if(folderBean!=null){
                    for(HomeDataBean.DataBean dataBean1:folderBean.getNameList()){
                        if(dataBean.equals(dataBean1)){
                            index=i;
                            break;
                        }
                    }
                }
            }
            if(packList.size()>2){
                packList.remove(startPosition);
                homeList.get(index).getFolderBean().getNameList().clear();
                homeList.get(index).getFolderBean().getNameList().addAll(packList);
                addNewItem(index,dataBean);
                pos=index+1;
            }else {

                for (int j = 0; j < packList.size(); j++) {
                    HomeDataBean.DataBean roomDevice = packList.get(j);
                    HomeDataBean homeItemBean = new HomeDataBean();
                    homeItemBean.setDataBean(roomDevice);
                    if (j == 0) {
                        homeList.set(index, homeItemBean);
                        pos=index;
                    } else {
                        homeList.add(index + j, homeItemBean);
                        pos=index+j;
                    }
                }

            }
            launcherAdapter.notifyDataSetChanged();
            packageAdapter.notifyDataSetChanged();
          //  launcherRv.smoothScrollToPosition(homeList.size()-1);
            //一定要延时，否则页面还没显示出来来不及取出child,优化点：可以考虑把演示放到函数内部

            //updateDragLayout(TYPE_HOME,index);
            //3,更新recyclerview
            updateAll(TYPE_HOME,pos);
        }
    }

    private void updateAll(final int type,final int index){
        ThreadManager.getInstance().postDelayedUITask(new Runnable() {
            @Override
            public void run() {
                updateDragLayout(type,index);

            }
        },200);
    }

    private void addNewItem(int index, HomeDataBean.DataBean roomDevice) {
        HomeDataBean homeItemBean = new HomeDataBean();

        homeItemBean.setDataBean(roomDevice);
        homeList.add(index + 1, homeItemBean);
    }

    @Override
    public void onBackPressed() {
        if(packageRv.getVisibility()==View.VISIBLE){
            hidePackageView();
            updateDragLayout(TYPE_HOME,0);
        }

    }


    protected List<? extends RecyclerView.ItemDecoration> buildDefaultItemDecorations() {
        int color = getResources().getColor(R.color.transparent);
        return Collections.singletonList(new DividerGridItemDecoration(this, 3, color) {
            @Override
            public boolean[] getItemSidesIsHaveOffsets(int itemPosition) {
                //顺序:left, top, right, bottom
                boolean[] booleans = {false, false, false, false};

                if (itemPosition <= 1) {
                    return booleans;
                } else {
                    switch (itemPosition % 2) {
                        case 0:
                            booleans[1] = true;
                            booleans[2] = true;
                            break;
                        case 1:
                            booleans[1] = true;
                            break;

                    }
                }
                return booleans;
            }
        });
    }
}
