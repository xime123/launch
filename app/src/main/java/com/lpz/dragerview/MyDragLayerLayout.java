package com.lpz.dragerview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;




public class MyDragLayerLayout<T> extends FrameLayout {
    private static final String TAG = MyDragLayerLayout.class.getSimpleName();

    public interface IDragActionCallback<T> {
        void onStartDrag(Bitmap dragBitmap, View dragView, int startPosition);

        void onDrag(Bitmap dragBitmap, View dragView, int startPosition, int lastPosition, int currentPosition);

        void onDrop(View dragView, int startPosition, int lastPosition, int currentPosition);

    }

    public interface IDragPosition<T> {

        void onDrop(View dragView, int startPosition, int lastPosition, int currentPosition, boolean out);

        void onSwap(View dragView, int startPosition, int lastPosition, int currentPosition, boolean out);

        /**
         * 合并文件夹
         * @param dragView
         * @param startPosition
         * @param lastPosition
         * @param currentPosition
         */
        void onInsert(View dragView, int startPosition, int lastPosition, int currentPosition);

        void unPack(View dragView, int startPosition);
    }


    public interface IDragDataCallback<T> {
        void addItem(T data, int position);

        void removeItem(T data, int position);

        void addTmpItem(Bitmap map, int position);

        void removeTmpItem(int position);
    }

    public MyDragLayerLayout(@NonNull Context context) {
        super(context);
    }

    public MyDragLayerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyDragLayerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private IDragActionCallback<T> mDragCallback;
    private IDragPosition<T> mDragPosition;
    // private int mStartDragPageIndex = -1;
    private RectF mDrawRegion = new RectF();
    private static final int DRAG_STATE_IDLE = 0;
    private static final int DRAG_STATE_START = 1;
    private static final int DRAG_STATE_DRAG = 2;
    private static final int DRAG_STATE_ANIMATION = 3;
    private int mDragState = -1;
    private PointF mLastPoint = new PointF();
    private Bitmap mDragSnapShot;
    private Bitmap mDragSnapShotEdge;
    private View mDragView;
    private RecyclerView mRecyclerView;
    //private ViewPager mViewPager;
    //  private T mData;
    private int TYPE_HOME=1;
    private int TYPE_PACKAGE=2;
    private int mItemStartPosition;
    private float mTopOffset;
    private float mLeftOffset;
    private float mBottomOffset;
    private int mItemLastPosition;
    private boolean mDoDropAnimation;
    private int mDragPageRangeStart = -1;
    private int mDragPageRangeEnd = -1;
    private volatile boolean mIsTouch = false;
    private int type=TYPE_HOME;
    private  FROMTYPE fromType=FROMTYPE.UNDEFINE;
    private boolean packed=false;
    private long SWAP_TIME=600;
    private long INSERT_GAP=150;
    //private int fromFolderPos;
    public void setDragPageRange(int dragPageRangeStart, int dragPageRangeEnd) {
        mDragPageRangeStart = dragPageRangeStart;
        mDragPageRangeEnd = dragPageRangeEnd;
    }

    public void setDragCallback(IDragActionCallback<T> dragCallback) {
        mDragCallback = dragCallback;
    }

    public void setDragPosition(IDragPosition<T> dragPosition) {
        mDragPosition = dragPosition;
    }

    public void startDrag(View childView, int startPosition, boolean doDropAnimation, int headerHeight, RecyclerView recyclerView) {
        Log.i(TAG, "dragLayout, startPosition=" + startPosition + ", doDropAnimation=" + doDropAnimation + "   headerHeight=" + headerHeight);
        if (!mIsTouch) {
            Log.e(TAG, "startDrag because touch event has release, do nothing.");
            return;
        }
        mDragView = childView;
        mRecyclerView = recyclerView;
        mItemStartPosition = startPosition;
        mItemLastPosition = startPosition;
        mDoDropAnimation = doDropAnimation;
        mRecyclerViewScrollState = RECYCLER_VIEW_SCROLL_IDLE;

        float offsetX = 0;
        float offsetY = 0;

        ViewParent vp = childView.getParent();
        while (vp != null && vp instanceof View && vp != this) {
            if (!(vp.getParent() instanceof MyRelativeLayout)) {
                offsetX += ((View) vp).getX();
                offsetY += ((View) vp).getY();
            }
            vp = vp.getParent();
        }

        mTopOffset = offsetY - headerHeight;

        if (mDragSnapShot != null) {
            mDragSnapShot.recycle();
        }
        mDragSnapShot = Utils.getViewSnapshot(childView, 0xFF);
        mDragSnapShotEdge = Utils.getViewSnapshot(childView, 0xFF);

        mDrawRegion.set(childView.getX(), childView.getY(), childView.getX() + childView.getWidth(), childView.getY() + childView.getHeight());
        mDrawRegion.offset(offsetX, offsetY);
        mDragState = DRAG_STATE_START;
        Utils.vibrate(getContext());
        childView.setVisibility(INVISIBLE);
        postInvalidate();

        if (mDragCallback != null) {
            mDragCallback.onStartDrag(mDragSnapShotEdge, mDragView, mItemStartPosition);
        }
    }

    public void setBottomOffset(float bottomOffset) {
        this.mBottomOffset = bottomOffset;
    }

    private void endDrag() {
        resetHandler();
        if (mDoDropAnimation) {
            mDragState = DRAG_STATE_ANIMATION;
            doAnimationOnDrop();
        } else {
            mDragState = DRAG_STATE_IDLE;
            postInvalidate();
        }
        mDragView = null;
    }


    private void onDrop() {
        if (mDragState == DRAG_STATE_IDLE) return;
        if (mDragCallback != null) {


            int tmp = findInsertPosition(mLastX, mLastY);
            if (tmp == -1) {
                tmp = mItemStartPosition;
            }
            mDragCallback.onDrop(mDragView, mItemStartPosition, mItemLastPosition, tmp);
            mItemLastPosition = tmp;

        } else if (mDragPosition != null) {
            int tmp = findInsertPosition(mLastX, mLastY);
            boolean out = isOut(mLastX, mLastY);
            if (tmp == -1) {
                tmp = mItemStartPosition;
            }
            mDragPosition.onDrop(mDragView, mItemStartPosition, mItemLastPosition, tmp, out&&fromType!=FROMTYPE.FROM_HOME);
            mItemLastPosition = tmp;
        }
    }

    private boolean isOut(float mLastX, float mLastY) {
        RectF rectF = new RectF();
        rectF.set(mRecyclerView.getLeft(), mRecyclerView.getTop(), mRecyclerView.getRight(), mRecyclerView.getBottom());
        return !rectF.contains(mLastX, mLastY);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = (mDragState >= DRAG_STATE_START) || super.onInterceptTouchEvent(ev);
        int action = ev.getAction();
        Log.i(TAG, "onInterceptTouchEvent, mDragState=" + mDragState + ", intercept=" + intercept + ", action=" + action);
        mIsTouch = !((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL));
        mLastX = ev.getX();
        mLastY = ev.getY();
        if (intercept && action == MotionEvent.ACTION_UP) {
            Log.e(TAG, "onInterceptTouchEvent, error handler");
            //if finger up too quickly, won't call onTouchEvent, so must handle up event here.
            doOnTouchEventUp();
        }
        return intercept;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        Log.i(TAG, "onTouchEvent, mDragState=" + mDragState + ", action=" + action);
        if (mDragState < 0) {
            return false;
        }
        mIsTouch = !((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_CANCEL));
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                mLastX = event.getX();
                mLastY = event.getY();
                checkRecyclerViewScroll(mLastY);
                //  checkSlideAndMovePosition(mLastX, mLastY);
                if (mDragState == DRAG_STATE_START) {
                    mDragState = DRAG_STATE_DRAG;
                } else if (mDragState == DRAG_STATE_DRAG) {
                    float dx = mLastX - mLastPoint.x;
                    float dy = mLastY - mLastPoint.y;
                    mDrawRegion.offset(dx, dy);
                    postInvalidate();
                }
                mLastPoint.set(mLastX, mLastY);
                doOnTouchEventMove();
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                doOnTouchEventUp();
                break;
            }

        }
        return true;//super.onTouchEvent(event);
    }

    private void doOnTouchEventUp() {
        resetSlideState();
        onDrop();
        endDrag();
    }

    private boolean onSwap = true;

    private void doOnTouchEventMove() {

        if (mDragState == DRAG_STATE_IDLE) return;
        if (mDragPosition != null) {
            int tmp=-1;
            boolean canInsert=false;
            if(type==TYPE_HOME){
                Object []values=findInsertOrSwapPosition(mLastX, mLastY);
                 tmp = (int) values[0];
                canInsert= (boolean) values[1];
            }else {
                tmp=findInsertPosition(mLastX,mLastY);
            }
            boolean out = isOut(mLastX, mLastY);
            if(out&&fromType==FROMTYPE.UNDEFINE&&!packed){
                packed=true;
                mDragPosition.unPack(mDragView, mItemStartPosition);
                onSwap=true;
                mHandler.removeMessages(UPDATE_SWAP_FLAG);
                return;
            }
            if (tmp == -1 || tmp == mItemStartPosition||mRecyclerViewScrollState!=RECYCLER_VIEW_SCROLL_IDLE ) {
                mHandler.removeMessages(UPDATE_SWAP_FLAG);
                return;
            }
            if(onSwap){
                updateSwapFlag();
                return;
            }
            mHandler.removeMessages(UPDATE_SWAP_FLAG);
            onSwap = true;

            Log.i(TAG, "tmp=" + tmp + "  mItemStartPosition=" + mItemStartPosition + " onSwap=" + onSwap);
            if(canInsert&&type==TYPE_HOME&&fromType!=FROMTYPE.FROM_PACKAGE){
                mDragPosition.onInsert(mDragView, mItemStartPosition, mItemLastPosition, tmp);
            }else {
                mDragPosition.onSwap(mDragView, mItemStartPosition, mItemLastPosition, tmp, false);

            }
            mItemStartPosition = tmp;

        }

    }

    private boolean canInsert(View view,float x,float y){
            int centerX=view.getRight()-view.getWidth()/2;
            int centerY=view.getBottom()-view.getHeight()/2;
            int dx= (int) Math.abs(x-centerX);
            int dy= (int) Math.abs(y-centerY);
            if(dx<=INSERT_GAP&&dy<=INSERT_GAP){
                return true;
            }
            return false;
    }

    private void updateSwapFlag() {
        Message message=Message.obtain();
        message.what=UPDATE_SWAP_FLAG;
        message.obj=false;

        mHandler.sendMessageDelayed(message,SWAP_TIME);
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mDragState >= DRAG_STATE_START && mDragSnapShot != null && !mDragSnapShot.isRecycled()) {
            canvas.drawBitmap(mDragSnapShot, mDrawRegion.left, mDrawRegion.top, null);
        }
    }

    private static final int SLIDE_EDGE = 200;
    private static final int SLIDE_CRITICAL = 80;
    private static final int SLIDE_IDLE = 0;
    private static final int SLIDE_CAN_LEFT = 1;
    private static final int SLIDE_CAN_RIGHT = 2;
    private static final int SLIDE_WAIT_TO_AUTO_SLIDE_LEFT = 3;
    private static final int SLIDE_WAIT_TO_AUTO_SLIDE_RIGHT = 4;
    private int mSlideState = SLIDE_IDLE;
    private float mSlideX;
    private float mCurrentX = SLIDE_EDGE + SLIDE_CRITICAL;
    private float mLastX;
    private float mLastY;

    private void resetSlideState() {
        mSlideState = SLIDE_IDLE;
        mCurrentX = SLIDE_EDGE + SLIDE_CRITICAL;
        this.fromType=FROMTYPE.UNDEFINE;
        this.packed=false;

        mHandler.removeMessages(UPDATE_SWAP_FLAG);
        onSwap=true;
    }

    private void resetHandler() {
        mHandler.removeMessages(0);
        mHandler.removeMessages(RECYCLER_VIEW_SCROLL_DOWN);
        mHandler.removeMessages(RECYCLER_VIEW_SCROLL_UP);
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: {
                    switch (mSlideState) {
                        case SLIDE_WAIT_TO_AUTO_SLIDE_LEFT: {
                            if (mSlideX - mCurrentX >= SLIDE_CRITICAL) {
                                // slide2Page(false);
                            }
                            break;
                        }
                        case SLIDE_WAIT_TO_AUTO_SLIDE_RIGHT: {
                            if (mCurrentX - mSlideX >= SLIDE_CRITICAL) {
                                //slide2Page(true);
                            }
                            break;
                        }
                    }

                    break;
                }

                case RECYCLER_VIEW_SCROLL_UP: {
                    if (autoScrollRecyclerView(20)) {
                        mHandler.sendEmptyMessageDelayed(RECYCLER_VIEW_SCROLL_UP, 10);
                    }
                    break;
                }
                case RECYCLER_VIEW_SCROLL_DOWN: {
                    if (autoScrollRecyclerView(-20)) {
                        mHandler.sendEmptyMessageDelayed(RECYCLER_VIEW_SCROLL_DOWN, 10);
                    }
                    break;
                }
                case UPDATE_SWAP_FLAG:
                    onSwap= (boolean) msg.obj;
                    break;
            }

        }
    };

    private void checkSlideAndMovePosition(float x, float y) {
        mCurrentX = x;
        if (mSlideState == SLIDE_IDLE) {
            if (x <= SLIDE_EDGE) {
                mSlideState = SLIDE_CAN_LEFT;
            } else if (x >= getWidth() - SLIDE_EDGE) {
                mSlideState = SLIDE_CAN_RIGHT;
            }
            mSlideX = x;
        } else {
            if (x > SLIDE_EDGE && x < getWidth() - SLIDE_EDGE) {
                resetSlideState();
                mHandler.removeMessages(0);
            } else {
                switch (mSlideState) {
                    case SLIDE_CAN_LEFT: {
                        if (mSlideX - x >= SLIDE_CRITICAL) {
                            mSlideState = SLIDE_WAIT_TO_AUTO_SLIDE_LEFT;
                            // slide2Page(false);
                            return;
                        }
                        break;
                    }
                    case SLIDE_CAN_RIGHT: {
                        if (x - mSlideX >= SLIDE_CRITICAL) {
                            mSlideState = SLIDE_WAIT_TO_AUTO_SLIDE_RIGHT;
                            // slide2Page(true);
                            return;
                        }
                        break;
                    }
                }
            }
        }

        if (mDragCallback != null) {
            int tmp = findInsertPosition(x, y);
            if (tmp != mItemLastPosition) {
                mDragCallback.onDrag(mDragSnapShotEdge, mDragView, mItemStartPosition, mItemLastPosition, tmp);
                mItemLastPosition = tmp;
            }
        }
    }

    private int findInsertPosition(float x, float y) {
        y = y - mTopOffset;
//        View childView = mRecyclerView.findChildViewUnder(x, y);
//        int result = -1;
//        RecyclerView recyclerView = findCurrentRecyclerView();
//        if (recyclerView != null) {
//            result = recyclerView.indexOfChild(childView);
//        }
//        return result;
        x=x- ScreenUtils.dpToPx(42);
        int result = -1;
        RecyclerView recyclerView = findCurrentRecyclerView();
        if (recyclerView != null) {
            RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
            if (lm != null && (lm instanceof GridLayoutManager)) {
                GridLayoutManager glm = (GridLayoutManager) lm;
                int first = glm.findFirstVisibleItemPosition();
                int last = glm.findLastVisibleItemPosition();
                if(first<0||last<1){
                    return result ;
                }
                for (int i = first; i <= last; i++) {
                    View child = glm.findViewByPosition(i);
                    Log.i("mtest","x="+x+"  y="+y+"index="+i+" left="+child.getLeft()+" right="+child.getRight()+"  top="+child.getTop()+"  bottom="+child.getBottom());
                    final float translationX = child.getTranslationX();
                    final float translationY = child.getTranslationY();
                    if (x >= child.getLeft() + SWAP_EDGE + translationX
                            && x <= child.getRight() - SWAP_EDGE + translationX
                            && y >= child.getTop() + SWAP_EDGE + translationY
                            && y <= child.getBottom() - SWAP_EDGE + translationY) {
                        return i;
                    }
//                    int hitRegion = findPointHitItemRegion(view, x, y);
//                    if (hitRegion != POINT_NO_HIT) {
//                        if ((i == mItemLastPosition)
//                                || ((i == mItemLastPosition + 1) && (hitRegion == POINT_HIT_LEFT))) {
//                            return mItemLastPosition;
//                        } else if ((i == mItemLastPosition + 1) && (hitRegion == POINT_HIT_RIGHT)) {
//                            return i;
//                        } else {
//                            return i + hitRegion;
//                        }
//                    }
                }
            }
        }
        return result;
    }

    private Object [] findInsertOrSwapPosition(float x, float y) {
        y = y - mTopOffset;
        x=x- ScreenUtils.dpToPx(42);
        int result = -1;
        boolean canInsert=false;
        Object []valus=new Object[]{result,canInsert};
        RecyclerView recyclerView = findCurrentRecyclerView();
        if (recyclerView != null) {
            RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
            if (lm != null && (lm instanceof GridLayoutManager)) {
                GridLayoutManager glm = (GridLayoutManager) lm;
                int first = glm.findFirstVisibleItemPosition();
                int last = glm.findLastVisibleItemPosition();
                if(first<0||last<1){
                    return valus ;
                }
                for (int i = first; i <= last; i++) {
                    View child = glm.findViewByPosition(i);
                    Log.i("mtest","x="+x+"  y="+y+"index="+i+" left="+child.getLeft()+" right="+child.getRight()+"  top="+child.getTop()+"  bottom="+child.getBottom());
                    final float translationX = child.getTranslationX();
                    final float translationY = child.getTranslationY();
                    if (x >= child.getLeft() + SWAP_EDGE + translationX
                            && x <= child.getRight() - SWAP_EDGE + translationX
                            && y >= child.getTop() + SWAP_EDGE + translationY
                            && y <= child.getBottom() - SWAP_EDGE + translationY) {
                        valus[0]=i;
                        valus[1]=canInsert(child,x,y);
                        return valus;
                    }
//                    int hitRegion = findPointHitItemRegion(view, x, y);
//                    if (hitRegion != POINT_NO_HIT) {
//                        if ((i == mItemLastPosition)
//                                || ((i == mItemLastPosition + 1) && (hitRegion == POINT_HIT_LEFT))) {
//                            return mItemLastPosition;
//                        } else if ((i == mItemLastPosition + 1) && (hitRegion == POINT_HIT_RIGHT)) {
//                            return i;
//                        } else {
//                            return i + hitRegion;
//                        }
//                    }
                }
            }
        }
        return valus;
    }

    private int SWAP_EDGE = 0;

    private int findSwapPosition(float curX, float curY) {
        curY = curY - mTopOffset;
        View childView = mRecyclerView.findChildViewUnder(curX, curY);
        if (childView == null) return -1;

        int right = (int) (curX + childView.getWidth());
        int bottom = (int) (curY + childView.getHeight());

        int winnerScore = -1;
        final int dx = (int) (curX - childView.getLeft());
        final int dy = (int) (curY - childView.getTop());
        if (dx > 0) {
            int diff = childView.getRight() - right + SWAP_EDGE;
            if (diff < 0 && childView.getRight() > childView.getRight()) {
                return mRecyclerView.indexOfChild(childView);
            }
        }
        if (dx < 0) {
            int diff = (int) (childView.getLeft() - curX - SWAP_EDGE);
            if (diff > 0 && childView.getLeft() < childView.getLeft()) {
                return mRecyclerView.indexOfChild(childView);
            }
        }
        if (dy < 0) {
            int diff = (int) (childView.getTop() - curY - SWAP_EDGE);
            if (diff > 0 && childView.getTop() < childView.getTop()) {
                return mRecyclerView.indexOfChild(childView);
            }
        }

        if (dy > 0) {
            int diff = childView.getBottom() - bottom + SWAP_EDGE;
            if (diff < 0 && childView.getBottom() > childView.getBottom()) {
                return mRecyclerView.indexOfChild(childView);
            }
        }

        return -1;
    }


    private RecyclerView findCurrentRecyclerView() {

        return mRecyclerView;
    }

    private RecyclerView findChildRecyclerView(ViewGroup parent) {
        for (int i = 0, N = parent.getChildCount(); i < N; i++) {
            View v = parent.getChildAt(i);
            if (v instanceof RecyclerView) {
                return (RecyclerView) v;
            } else if (v instanceof ViewGroup) {
                RecyclerView tmp = findChildRecyclerView((ViewGroup) v);
                if (tmp != null) {
                    return tmp;
                }
            }
        }
        return null;
    }

    private RectF mRect = new RectF();
    private static final int POINT_NO_HIT = -1;
    private static final int POINT_HIT_LEFT = 0;
    private static final int POINT_HIT_RIGHT = 1;

    private int findPointHitItemRegion(View view, float x, float y) {
        if (view != null) {
            mRect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
            mRect.offset(0, mTopOffset);
            if (mRect.contains(x, y)) {
                return x <= mRect.centerX() ? POINT_HIT_LEFT : POINT_HIT_RIGHT;
            }
        }
        return POINT_NO_HIT;
    }


    @Override
    protected void onDetachedFromWindow() {
        resetSlideState();
        resetHandler();
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        super.onDetachedFromWindow();
    }

    private ValueAnimator mAnimator;

    private void doAnimationOnDrop() {
        View view = findDropView();
        if (view != null) {
            PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat("X", mDrawRegion.left, view.getLeft());
            PropertyValuesHolder holder2 = PropertyValuesHolder.ofFloat("Y", mDrawRegion.top, view.getTop() + mTopOffset);
            mAnimator = ObjectAnimator.ofPropertyValuesHolder(holder1, holder2);

            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Float x = (Float) animation.getAnimatedValue("X");
                    Float y = (Float) animation.getAnimatedValue("Y");
                    float dx = x - mDrawRegion.left;
                    float dy = y - mDrawRegion.top;
                    mDrawRegion.offset(dx, dy);
                    postInvalidate();
                }

            });

            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mAnimator = null;
                    mDragState = DRAG_STATE_IDLE;
                    postInvalidate();
                }
            });

            mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            mAnimator.setDuration(200);
            mAnimator.start();
        } else {
            mDragState = DRAG_STATE_IDLE;
            postInvalidate();
        }
    }

    private View findDropView() {
        RecyclerView recyclerView = findCurrentRecyclerView();
        if (recyclerView != null) {
            RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
            if (lm != null && (lm instanceof GridLayoutManager)) {
                GridLayoutManager glm = (GridLayoutManager) lm;
                int first = glm.findFirstVisibleItemPosition();
                int last = glm.findLastCompletelyVisibleItemPosition();
                if (mItemLastPosition >= first && mItemLastPosition <= last) {
                    return glm.findViewByPosition(mItemLastPosition);
                }
            }
        }
        return null;
    }


    private static final int RECYCLER_VIEW_AUTO_SCROLL_EDGE = 180;
    private static final int RECYCLER_VIEW_SCROLL_IDLE = 0;
    private static final int RECYCLER_VIEW_SCROLL_UP = 1;
    private static final int RECYCLER_VIEW_SCROLL_DOWN = 2;
    private static final int UPDATE_SWAP_FLAG = 3;
    private int mRecyclerViewScrollState = RECYCLER_VIEW_SCROLL_IDLE;

    private void checkRecyclerViewScroll(float y) {


        RecyclerView recyclerView = findCurrentRecyclerView();
        if (recyclerView != null) {
            if (y > mTopOffset && y < mTopOffset + RECYCLER_VIEW_AUTO_SCROLL_EDGE) {
                if (mRecyclerViewScrollState != RECYCLER_VIEW_SCROLL_DOWN) {
                    mRecyclerViewScrollState = RECYCLER_VIEW_SCROLL_DOWN;
                    mHandler.sendEmptyMessage(RECYCLER_VIEW_SCROLL_DOWN);
                }
            } else if (y > getHeight() - mBottomOffset - RECYCLER_VIEW_AUTO_SCROLL_EDGE) {
                if (mRecyclerViewScrollState != RECYCLER_VIEW_SCROLL_UP) {
                    mRecyclerViewScrollState = RECYCLER_VIEW_SCROLL_UP;
                    mHandler.sendEmptyMessage(RECYCLER_VIEW_SCROLL_UP);
                }
            } else {
                if (mRecyclerViewScrollState != RECYCLER_VIEW_SCROLL_IDLE) {
                    mRecyclerViewScrollState = RECYCLER_VIEW_SCROLL_IDLE;
                    mHandler.removeMessages(RECYCLER_VIEW_SCROLL_UP);
                    mHandler.removeMessages(RECYCLER_VIEW_SCROLL_DOWN);
                }
            }
        }
    }

    private boolean autoScrollRecyclerView(int dy) {
        RecyclerView recyclerView = findCurrentRecyclerView();
        if (recyclerView != null) {
            RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
            if (lm != null && (lm instanceof GridLayoutManager)) {
                GridLayoutManager glm = (GridLayoutManager) lm;
                int first = glm.findFirstCompletelyVisibleItemPosition();
                int last = glm.findLastCompletelyVisibleItemPosition();
                int count = recyclerView.getAdapter().getItemCount();
                if (glm.canScrollVertically()) {
                    if ((first > 0 && dy < 0)
                            || (last < count - 1 && dy > 0)) {
                        Log.i(TAG, "dragLayout  autoScrollRecyclerView===>last=" + last + "   count=" + count + "  dy=" + dy);
                        recyclerView.scrollBy(0, dy);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void updateRecyclerView(RecyclerView currentRv,int newStartPost,FROMTYPE  fromType){
        mRecyclerView=currentRv;
        mItemStartPosition = newStartPost;
        mItemLastPosition = newStartPost;
        float offsetX = 0;
        float offsetY = 0;
      //  int childViewCount=mRecyclerView.getChildCount();
       GridLayoutManager glm= (GridLayoutManager) mRecyclerView.getLayoutManager();
        int firstPos=glm.findFirstVisibleItemPosition();
        int endPos=glm.findLastVisibleItemPosition();
        int childViewCount=mRecyclerView.getLayoutManager().getChildCount();
        Log.i("mTest","childViewCount="+childViewCount+" newStartPost="+newStartPost+" firstPos="+firstPos+"  endPos="+endPos);
        //View childView=mRecyclerView.getChildAt(newStartPost);
        View childView = mRecyclerView .getLayoutManager().findViewByPosition(newStartPost);
        if(childView==null||mDragView==null)return;
        mDragView.setVisibility(VISIBLE);
        mDragView = childView;
        ViewParent vp = childView.getParent();
        while (vp != null && vp instanceof View && vp != this) {
            if (!(vp.getParent() instanceof MyRelativeLayout)) {
                offsetX += ((View) vp).getX();
                offsetY += ((View) vp).getY();
            }
            vp = vp.getParent();
        }
        this.fromType=fromType;
        mTopOffset = offsetY ;

        if (mDragSnapShot != null) {
            mDragSnapShot.recycle();
        }
        mDragSnapShot = Utils.getViewSnapshot(childView, 0xFF);
        mDragSnapShotEdge = Utils.getViewSnapshot(childView, 0xFF);

        mDrawRegion.set(mLastX-childView.getWidth()/2, mLastY-childView.getHeight()/2-100, mLastX + childView.getWidth()/2, mLastY + childView.getHeight()/2-100);
        childView.setVisibility(INVISIBLE);
        postInvalidate();
    }

    public void updateType(int type){
        this.type=type;
    }


}
