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
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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



/**
 * Created by Lisen.Liu on 2018/11/2.
 */


public class DragLayerLayout<T> extends FrameLayout {
    private static final String TAG = DragLayerLayout.class.getSimpleName();

    public interface IDragActionCallback<T> {
        void onStartDrag(Bitmap dragBitmap, View dragView, int startPage, int startPosition, T data);

        void onDrag(Bitmap dragBitmap, View dragView, int startPage, int startPosition, int lastPage, int lastPosition, int currentPage, int currentPosition, T data);

        void onDrop(View dragView, int startPage, int startPosition, int lastPage, int lastPosition, int currentPage, int currentPosition, T data);

    }

    public interface IDragDataCallback<T> {
        void addItem(T data, int position);

        void removeItem(T data, int position);

        void addTmpItem(Bitmap map, int position);

        void removeTmpItem(int position);
        void doSwipeData(int startPage, int startPosition, int currentPage, int currentPosition, DataBean data);
    }

    public DragLayerLayout(@NonNull Context context) {
        super(context);
    }

    public DragLayerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DragLayerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private IDragActionCallback<T> mDragCallback;
    private int mStartDragPageIndex = -1;
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
    private ViewPager mViewPager;
    private T mData;
    private int mItemStartPosition;
    private float mTopOffset;
    private int mItemLastPosition;
    private boolean mDoDropAnimation;
    private int mDragPageRangeStart = -1;
    private int mDragPageRangeEnd = -1;
    private volatile boolean mIsTouch = false;

    public void setDragPageRange(int dragPageRangeStart, int dragPageRangeEnd) {
        mDragPageRangeStart = dragPageRangeStart;
        mDragPageRangeEnd = dragPageRangeEnd;
    }

    public void setDragCallback(IDragActionCallback<T> dragCallback) {
        mDragCallback = dragCallback;
    }

    public void startDrag(View childView, T data, int startPosition, boolean doDropAnimation) {
        Log.i(TAG, "startDrag, startPosition=" + startPosition + ", doDropAnimation=" + doDropAnimation);
        if (!mIsTouch) {
            Log.e(TAG, "startDrag because touch event has release, do nothing.");
            return;
        }
        mViewPager = null;
        mDragView = childView;
        mData = data;
        mItemStartPosition = startPosition;
        mItemLastPosition = startPosition;
        mDoDropAnimation = doDropAnimation;
        mRecyclerViewScrollState = RECYCLER_VIEW_SCROLL_IDLE;

        float offsetX = 0;
        float offsetY = 0;

        ViewParent vp = childView.getParent();
        while (vp != null && vp instanceof View && vp != this) {
            if (!(vp.getParent() instanceof ViewPager)) {
                offsetX += ((View) vp).getX();
                offsetY += ((View) vp).getY();
            } else if (mViewPager == null) {
                mViewPager = (ViewPager) vp.getParent();
            }
            vp = vp.getParent();
        }

        mTopOffset = offsetY;

        if (mViewPager == null) {
            return;
        }

        if (mDragSnapShot != null) {
            mDragSnapShot.recycle();
        }
        mDragSnapShot = Utils.getViewSnapshot(childView, 0xA0);
        mDragSnapShotEdge = Utils.getViewSnapshot(childView, 0x30);

        mDrawRegion.set(childView.getX(), childView.getY(), childView.getX() + childView.getWidth(), childView.getY() + childView.getHeight());
        mDrawRegion.offset(offsetX, offsetY);
        mDragState = DRAG_STATE_START;
        Utils.vibrate(getContext());
        postInvalidate();

        mStartDragPageIndex = mViewPager.getCurrentItem();
        if (mDragCallback != null) {
            mDragCallback.onStartDrag(mDragSnapShotEdge, mDragView, mStartDragPageIndex, mItemStartPosition, mData);
        }
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
        mData = null;
    }


    private void onDrop() {
        if (mDragCallback != null && mViewPager != null) {
            PagerAdapter adapter = mViewPager.getAdapter();
            if (adapter != null) {
                int index = mViewPager.getCurrentItem();
                int tmp = findInsertPosition(mLastX, mLastY);
                mDragCallback.onDrop(mDragView, mStartDragPageIndex, mItemStartPosition, index, mItemLastPosition, index, tmp, mData);
                mItemLastPosition = tmp;
            }
        }
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
                checkSlideAndMovePosition(mLastX, mLastY);
                if (mDragState == DRAG_STATE_START) {
                    mDragState = DRAG_STATE_DRAG;
                } else if (mDragState == DRAG_STATE_DRAG) {
                    float dx = mLastX - mLastPoint.x;
                    float dy = mLastY - mLastPoint.y;
                    mDrawRegion.offset(dx, dy);
                    postInvalidate();
                }
                mLastPoint.set(mLastX, mLastY);
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
                                slide2Page(false);
                            }
                            break;
                        }
                        case SLIDE_WAIT_TO_AUTO_SLIDE_RIGHT: {
                            if (mCurrentX - mSlideX >= SLIDE_CRITICAL) {
                                slide2Page(true);
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
                            slide2Page(false);
                            return;
                        }
                        break;
                    }
                    case SLIDE_CAN_RIGHT: {
                        if (x - mSlideX >= SLIDE_CRITICAL) {
                            mSlideState = SLIDE_WAIT_TO_AUTO_SLIDE_RIGHT;
                            slide2Page(true);
                            return;
                        }
                        break;
                    }
                }
            }
        }

        if (mDragCallback != null) {
            int currentPage = mViewPager.getCurrentItem();
            int tmp = findInsertPosition(x, y);
            if (tmp != mItemLastPosition) {
                mDragCallback.onDrag(mDragSnapShotEdge, mDragView, mStartDragPageIndex, mItemStartPosition, currentPage, mItemLastPosition, currentPage, tmp, mData);
                mItemLastPosition = tmp;
            }
        }
    }

    private void slide2Page(boolean next) {
        if (mViewPager != null) {
            PagerAdapter adapter = mViewPager.getAdapter();
            if (adapter != null) {
                int lastPage = mViewPager.getCurrentItem();
                int index = lastPage + (next ? 1 : -1);

                if (index >= 0 && index < adapter.getCount()) {
                    if ((index >= mDragPageRangeStart || mDragPageRangeStart < 0) && (index <= mDragPageRangeEnd || mDragPageRangeEnd < 0)) {
                        mHandler.sendEmptyMessageDelayed(0, 800);
                        mViewPager.setCurrentItem(index, true);
                        if (mDragCallback != null) {
                            int tmp = findInsertPosition(mLastX, mLastY);
                            mDragCallback.onDrag(mDragSnapShotEdge, mDragView, mStartDragPageIndex, mItemStartPosition, lastPage, mItemLastPosition, index, tmp, mData);
                            mItemLastPosition = tmp;
                        }
                    }
                }
            }
        }
    }

    private int findInsertPosition(float x, float y) {
        int result = -1;
        RecyclerView recyclerView = findCurrentRecyclerView();
        if (recyclerView != null) {
            RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
            if (lm != null && (lm instanceof GridLayoutManager)) {
                GridLayoutManager glm = (GridLayoutManager) lm;
                int first = glm.findFirstVisibleItemPosition();
                int last = glm.findLastVisibleItemPosition();
                result = last;
                for (int i = first; i <= last; i++) {
                    View view = glm.findViewByPosition(i);
                    int hitRegion = findPointHitItemRegion(view, x, y);
                    if (hitRegion != POINT_NO_HIT) {
                        if ((i == mItemLastPosition)
                                || ((i == mItemLastPosition + 1) && (hitRegion == POINT_HIT_LEFT))) {
                            return mItemLastPosition;
                        } else if ((i == mItemLastPosition + 1) && (hitRegion == POINT_HIT_RIGHT)) {
                            return i;
                        } else {
                            return i + hitRegion;
                        }
                    }
                }
            }
        }
        return result;
    }

    private RecyclerView findCurrentRecyclerView() {
        RecyclerView recyclerView = null;
        int index = mViewPager.getCurrentItem();
        Fragment fragment = (Fragment) mViewPager.getAdapter().instantiateItem(mViewPager, index);
        if (fragment != null) {
            View rootView = fragment.getView();
            if (rootView instanceof RecyclerView) {
                recyclerView = (RecyclerView) rootView;
            } else if (rootView != null && (rootView instanceof ViewGroup)) {
                recyclerView = findChildRecyclerView((ViewGroup) rootView);
            }
        }
        return recyclerView;
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
    private int mRecyclerViewScrollState = RECYCLER_VIEW_SCROLL_IDLE;

    private void checkRecyclerViewScroll(float y) {
        RecyclerView recyclerView = findCurrentRecyclerView();
        if (recyclerView != null) {
            if (y > mTopOffset && y < mTopOffset + RECYCLER_VIEW_AUTO_SCROLL_EDGE) {
                if (mRecyclerViewScrollState != RECYCLER_VIEW_SCROLL_DOWN) {
                    mRecyclerViewScrollState = RECYCLER_VIEW_SCROLL_DOWN;
                    mHandler.sendEmptyMessage(RECYCLER_VIEW_SCROLL_DOWN);
                }
            } else if (y > getHeight() - RECYCLER_VIEW_AUTO_SCROLL_EDGE) {
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
                        recyclerView.scrollBy(0, dy);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
