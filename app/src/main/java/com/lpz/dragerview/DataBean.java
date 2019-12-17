package com.lpz.dragerview;

import java.util.Random;



public class DataBean {
    private static final int[] IMAGE_RES = new int[] {
            R.drawable.zk_img_biaozhun,
            R.drawable.zk_img_qijian,
            R.drawable.zk_img_zhongkong,

    };
    public int resId;
    public String name;


    public DataBean(String name) {
        Random random = new Random();
        resId = IMAGE_RES[random.nextInt(IMAGE_RES.length)];
        this.name = name;
    }
}
