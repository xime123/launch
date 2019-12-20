package com.lpz.dragerview.loading;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class LoadingView extends View {
    private Paint mPaint=new Paint();
    private Rect one=new Rect();
    private Rect two=new Rect();
    private Rect three=new Rect();
    private Rect four=new Rect();
    private Rect five=new Rect();

    private int mHeight;
    private int mWidth;
    private int mLen=80;
    private int gapLeft=20;
    private int gapRight=20;
    public LoadingView(Context context) {
        super(context);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight=h;
        mWidth=w;
        initRectf();
    }

    private void initRectf() {
        int weiWid=mWidth/4;


    }


}
