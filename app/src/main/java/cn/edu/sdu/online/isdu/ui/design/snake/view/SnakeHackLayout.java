package cn.edu.sdu.online.isdu.ui.design.snake.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import cn.edu.sdu.online.isdu.ui.design.snake.Snake;
import cn.edu.sdu.online.isdu.ui.design.snake.config.SnakeConfigException;
import cn.edu.sdu.online.isdu.ui.design.snake.util.Logger;
import cn.edu.sdu.online.isdu.ui.design.snake.util.SwipeUpGestureDispatcher;
import cn.edu.sdu.online.isdu.ui.design.snake.util.Utils;

/**
 * SnakeHackLayout 用于滑动关闭视图处理
 *
 * @author Scott Smith 2017-12-13 14:24
 */
public class SnakeHackLayout extends FrameLayout {
    // 使用官方控件，简化拖拽处理
    private ViewDragHelper mViewDragHelper;
    private OnEdgeDragListener onEdgeDragListener;
    private List<Snake.OnDragListener> onDragListeners = new ArrayList<>();

    // 释放因子：决定页面滑动释放的力度（值为3，即页面滑动超过父控件宽度的1/3后页面可以滑动关闭）
    private final int DEFALT_RELEASE_FACTOR = 3;
    private int mReleaseFactor = DEFALT_RELEASE_FACTOR;
    private int mXRange;

    // 子视图原始坐标
    private PointF mOriginPoint = new PointF(0f, 0f);
    private OnReleaseStateListener onReleaseStateListener;
    private boolean mAllowDragChildView = true;

    // 阴影Drawable
    private GradientDrawable mShadowDrawable;

    // 阴影边缘默认起始颜色
    public static final int DEFAULT_SHADOW_START_COLOR = Color.parseColor("#00000000");
    // 阴影边缘默认结束颜色
    public static final int DEFAULT_SHADOW_END_COLOR = Color.parseColor("#50000000");

    private int mShadowStartColor = DEFAULT_SHADOW_START_COLOR;
    private int mShadowEndColor = DEFAULT_SHADOW_END_COLOR;
    private int mShadowWidth = (int) Utils.dp2px(getContext(),15f);
    private boolean isSettling = false;
    private boolean ignoreDragEvent = false;
    // 设置是否仅监听快速滑动手势，该行为在用户快速往右滑动页面任意部分时将触发页面关闭
    private boolean onlyListenToFastSwipe = false;
    // 设置阴影边缘是否隐藏，默认显示
    private boolean hideShadowOfEdge = false;
    private SnakeTouchInterceptor mCustomTouchInterceptor;
    private int mContentViewLeft;
    private int mContentViewTop;
    private boolean isInLayout = false;
    private float fractionX = 0f;
    private boolean mSwipeUpToHomeEnabled = false;

    private ViewTreeObserver.OnPreDrawListener mPreDrawListener = null;
    private SwipeUpGestureDispatcher mSwipeUpGestureDispatcher;
    private DragInterceptor mDragInterceptor;
    private int mInterceptScene = -1;
    private SnakeUIConfig mUIConfig = SnakeUIConfig.get();

    public SnakeHackLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SnakeHackLayout(@NonNull Context context) {
        this(context, null);
    }

    private void init(final Context context) {
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                if(null != mDragInterceptor) {
                    mInterceptScene = mDragInterceptor.intercept(SnakeHackLayout.this, child, pointerId);
                }

                Log.d("Jzz", "tryCaptureView getViewDragState=" + mViewDragHelper.getViewDragState());
                return !ignoreDragEvent && mViewDragHelper.getViewDragState() == ViewDragHelper.STATE_IDLE;
            }

            @Override
            public void onEdgeTouched(int edgeFlags, int pointerId) {
                if(ignoreDragEvent) return;

                if(null != onEdgeDragListener) {
                    onEdgeDragListener.onDragStart(SnakeHackLayout.this);
                }

                for(Snake.OnDragListener onDragListener: onDragListeners) {
                    View childView = null;
                    if(getChildCount() > 0) {
                        childView = getChildAt(0);
                    }
                    onDragListener.onDragStart(childView);
                }
            }

            @Override
            public int getViewHorizontalDragRange(View child) {
                return mXRange;
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                return 0;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                if(null != mDragInterceptor) {
                    mInterceptScene = mDragInterceptor.intercept(SnakeHackLayout.this, child, 0);
                }

                if(left < mOriginPoint.x) left = (int) mOriginPoint.x;
                left = mViewDragHelper.isEdgeTouched(ViewDragHelper.EDGE_LEFT) ? left : (int) child.getX();

                // 取巧做法，往右平移一个像素，使onRelease方法被调用

                left = !onlyListenToFastSwipe ? left : (int) mOriginPoint.x;
                left = mInterceptScene < 0 ? left : (int)mOriginPoint.x;
                left = ignoreDragEvent ? 0 : left;

                Log.d("Jzz", "clampViewPositionHorizontal left=" + left);
                return left;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                Log.d("Jzz", "clampViewPositionHorizontal mOriginPoint.y=" + mOriginPoint.y);
                return (int) mOriginPoint.y;
            }

            @Override
            public void onViewDragStateChanged(int state) {
                Log.d("Jzz", "onViewDragStateChanged state=" + state);
                isSettling = ViewDragHelper.STATE_SETTLING == state;
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                if(!hideShadowOfEdge) {
                    int shadowStartColor = Utils.changeAlpha(mShadowStartColor, (int) (Color.alpha(mShadowStartColor) * (1 - (float)left / (float) mXRange)));
                    int shadowEndColor = Utils.changeAlpha(mShadowEndColor, (int) (Color.alpha(mShadowEndColor) * (1 - (float)left / (float) mXRange)));

                    mShadowDrawable.mutate();
                    mShadowDrawable.setColors(new int[] {shadowStartColor , shadowEndColor});
                    invalidate();
                }

                if(needListenForDraging(mViewDragHelper, changedView)) {
                    if(null != onEdgeDragListener) {
                        onEdgeDragListener.onDrag(SnakeHackLayout.this, changedView, left);
                    }

                    for(Snake.OnDragListener onDragListener : onDragListeners) {
                        onDragListener.onDrag(changedView, left, isSettling);
                    }
                }

                if(left <= 0 || left >= mXRange) {
                    if (null != onReleaseStateListener && isSettling) {
                        onReleaseStateListener.onReleaseCompleted(SnakeHackLayout.this, changedView);
                    }
                }

                if(left <= 0 && isSettling) {
                    for(Snake.OnDragListener onDragListener : onDragListeners) {
                        onDragListener.onBackToStartCompleted(changedView);
                    }
                }

                mContentViewLeft = left;
                mContentViewTop = top;
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                if(mViewDragHelper.isEdgeTouched(ViewDragHelper.EDGE_LEFT) || onlyListenToFastSwipe) {
                    boolean shouldClose = xvel > mViewDragHelper.getMinVelocity();

                    if(!shouldClose) {
                        shouldClose = releasedChild.getLeft() > mXRange / mReleaseFactor;
                    }

                    if(null != onEdgeDragListener) {
                        onEdgeDragListener.onRelease(SnakeHackLayout.this, releasedChild, releasedChild.getLeft(), shouldClose, mInterceptScene);
                    }

                    for(Snake.OnDragListener onDragListener : onDragListeners) {
                        onDragListener.onRelease(releasedChild, xvel);
                    }
                }
            }
        });
        mViewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);

        mShadowDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[] {mShadowStartColor, mShadowEndColor});

        mSwipeUpGestureDispatcher = SwipeUpGestureDispatcher.create(this,
                (int) mViewDragHelper.getMinVelocity(), mViewDragHelper.getEdgeSize(),
                new SwipeUpGestureDispatcher.OnSwipeUpListener() {
            @Override
            public void onSwipeUp(float velocityY, boolean isEdgeBottomTouched) {
                Logger.d("onSwipeUp: velocityY = " + velocityY + ", isEdgeBottomTouched = " + isEdgeBottomTouched);
                if(isEdgeBottomTouched) Utils.backToHome(getContext());
            }
        });
    }

    // Only listen for left edge was touched and the child view has left edge when finger leave
    private boolean needListenForDraging(ViewDragHelper viewDragHelper, View childView) {
        return viewDragHelper.isEdgeTouched(ViewDragHelper.EDGE_LEFT)
                || (viewDragHelper.getViewDragState() == ViewDragHelper.STATE_SETTLING
                    && childView.getLeft() > 0);
    }

    public static SnakeHackLayout getLayout(Context context, View contentView, boolean allowDragChildView) {
        SnakeHackLayout snakeHackLayout = new SnakeHackLayout(context);
        snakeHackLayout.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        snakeHackLayout.setAllowDragChildView(allowDragChildView);

        if (null != contentView) {
            snakeHackLayout.addView(contentView);
        }

        return snakeHackLayout;
    }

    public static SnakeHackLayout getLayout(Context context) {
        return getLayout(context, null, true);
    }

        @Override
    public void addView(View child) {
        if(getChildCount() > 0) {
            throw new IllegalStateException("SnakeHackLayout can host only one direct child. ");
        }
        super.addView(child);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mXRange = right - left;

        isInLayout = true;

        if(getChildCount() > 0) {
            View contentView = getChildAt(0);
            try {
                contentView.layout(mContentViewLeft, mContentViewTop,
                        mContentViewLeft + contentView.getMeasuredWidth(),
                        mContentViewTop + contentView.getMeasuredHeight());
            } catch (Exception e) {
                cn.edu.sdu.online.isdu.util.Logger.log(e);
            }
        }

        isInLayout = false;
    }

    @Override
    public void requestLayout() {
        if(!isInLayout) {
            super.requestLayout();
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.contentLeft = mContentViewLeft;
        ss.contentTop = mContentViewTop;

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if(!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;
        mContentViewLeft = savedState.contentLeft;
        mContentViewTop = savedState.contentTop;
        requestLayout();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != mCustomTouchInterceptor && mCustomTouchInterceptor.onTouchEvent(event)) {
            return true;
        }

        requestParentDisallowInterceptTouchEvent(false);
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (null != mCustomTouchInterceptor && mCustomTouchInterceptor.onTouchEvent(ev)) {
            return true;
        }

        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        requestParentDisallowInterceptTouchEvent(false);

        if(mSwipeUpToHomeEnabled) {
            mSwipeUpGestureDispatcher.dispatch(ev);
        }

        return super.dispatchTouchEvent(ev);
    }

    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        ViewParent parent = getParent();
        if(null != parent) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    @Override
    public void computeScroll() {
        if(mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void setFractionX(final float fractionX) {
        this.fractionX = fractionX;

        if(null == mPreDrawListener) {
            mPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    getViewTreeObserver().removeOnPreDrawListener(mPreDrawListener);
                    setTranslateX(fractionX);
                    return true;
                }
            };
            getViewTreeObserver().addOnPreDrawListener(mPreDrawListener);
        }

        setTranslateX(fractionX);
    }

    private void setTranslateX(float fractionX) {
        int width = getWidth();
        if(width <= 0f) return;

        setTranslationX(width * fractionX);
    }

    public float getFractionX() {
        return fractionX;
    }

    /**
     * 设置是否允许拖拽子视图（默认允许）
     *
     * @param allowDragChildView true 允许 false 禁止拖拽
     */
    @Deprecated
    public void setAllowDragChildView(boolean allowDragChildView) {
        mAllowDragChildView = allowDragChildView;
    }

    /**
     * 设置是否忽略当前拖拽事件（拖拽将不会导致子视图滑动，但onRelease滑动监听依然可以生效
     *
     * @param ignore true 忽略 false 反之
     */
    public void ignoreDragEvent(boolean ignore) {
        ignoreDragEvent = ignore;
        // Return to start position
        if(getChildCount() > 0) {
            smoothScrollToStart(getChildAt(0), null);
        }
    }

    public boolean ignoredDragEvent() {
        return ignoreDragEvent;
    }

    /**
     * Get snake ui config info.
     *
     * @return the ui config info
     */
    public SnakeUIConfig getUIConfig() {
        return mUIConfig;
    }

    /**
     * Set whether allow page link age in the ui config.
     *
     * @param allow true: allow, false: disallow
     */
    public void setAllowPageLinkageOfUIConfig(boolean allow) {
        mUIConfig.allowPageLinkage = allow;
    }

    /**
     * Just for internal use, don't call directly please.
     *
     * @param onEdgeDragListener the OnEdgeDragListener
     */
    public void setOnEdgeDragListener(OnEdgeDragListener onEdgeDragListener) {
        if(null != this.onEdgeDragListener) throw new SnakeConfigException("Don't assign values for onEdgeDragListener");

        this.onEdgeDragListener = onEdgeDragListener;
    }

    public void addOnDragListener(Snake.OnDragListener onDragListener) {
        onDragListeners.add(onDragListener);
    }

    public void setDragInterceptor(DragInterceptor dragInterceptor) {
        mDragInterceptor = dragInterceptor;
    }

    /**
     * 设置滑动监听最小检测速度
     *
     * @param minVelocity the minimum velocity.
     */
    public void setMinVelocity(int minVelocity) {
        mViewDragHelper.setMinVelocity(minVelocity);
        mSwipeUpGestureDispatcher.setMinVelocity(minVelocity);
    }

    /**
     * 设置渐变阴影起始颜色
     *
     * @param shadowStartColor 颜色整型值
     */
    public void setShadowStartColor(@ColorInt int shadowStartColor) {
        mShadowStartColor = shadowStartColor;
    }

    /**
     * 设置渐变阴影结束颜色
     *
     * @param shadowEndColor 颜色整形值
     */
    public void setShadowEndColor(@ColorInt int shadowEndColor) {
        mShadowEndColor = shadowEndColor;
    }

    /**
     * 设置是否仅监听快速滑动手势
     *
     * @param onlyListenToFastSwipe true 仅能使用快速滑动手势关闭当前页面 false 默认行为（滑动关闭）
     */
    public void setOnlyListenToFastSwipe(boolean onlyListenToFastSwipe) {
        this.onlyListenToFastSwipe = onlyListenToFastSwipe;
    }

    /**
     * Set custom touch interceptor to resolve draging conflict, you can ignore this method in the
     * majority of scenarios.
     *
     * @param interceptor the touch interceptor
     */
    public void setCustomTouchInterceptor(SnakeTouchInterceptor interceptor) {
        mCustomTouchInterceptor = interceptor;
    }

    /**
     * Eanble swipe up to home function.
     *
     * @param enable true: enable false: disable
     */
    public void enableSwipeUpToHome(boolean enable) {
        mSwipeUpToHomeEnabled = enable;
    }

    /**
     * Get swipe up to home open state.
     *
     * @return the state of swipe up to home
     */
    public boolean swipeUpToHomeEnabled() {
        return mSwipeUpToHomeEnabled;
    }

    /**
     * 获取是否仅监听快速滑动状态
     *
     * @return 监听状态
     */
    public boolean onlyListenToFastSwipe() {
        return onlyListenToFastSwipe;
    }

    /**
     * 设置阴影边缘是否隐藏
     *
     * @param hideShadowOfEdge true 隐藏， false 显示
     */
    public void hideShadowOfEdge(boolean hideShadowOfEdge) {
        this.hideShadowOfEdge = hideShadowOfEdge;
    }

    public void resetUI() {
        mViewDragHelper.abort();
    }

    /**
     * 平滑移动View到指定位置
     *
     * @param view 移动的目标子视图
     * @param x 水平坐标
     * @param y 垂直坐标
     * @param onReleaseStateListener the OnReleaseStateListener
     */
    public void smoothScrollTo(View view, int x, int y, final OnReleaseStateListener onReleaseStateListener) {
        if(null != view) {
            mViewDragHelper.smoothSlideViewTo(view, x, y);
            invalidate();

            this.onReleaseStateListener = onReleaseStateListener;
        }
    }

    /**
     * 平滑移动View到起始位置
     *
     * @param view 移动的目标子视图
     * @param onReleaseStateListener the OnReleaseStateListener
     */
    public void smoothScrollToStart(View view, OnReleaseStateListener onReleaseStateListener) {
        smoothScrollTo(view, (int) mOriginPoint.x, (int) mOriginPoint.y, onReleaseStateListener);
    }

    public void smoothScrollToStart(View view) {
        smoothScrollToStart(view, null);
    }

    /**
     * 平滑移动View到最后位置
     *
     * @param view 移动的目标子视图
     * @param onReleaseStateListener the OnReleaseStateListener
     */
    public void smoothScrollToLeave(View view, OnReleaseStateListener onReleaseStateListener) {
        smoothScrollTo(view, mXRange, (int) mOriginPoint.y, onReleaseStateListener);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if(getChildCount() <= 0) return;
        if(hideShadowOfEdge) return;

        canvas.save();
        int left = getChildAt(0).getLeft() - mShadowWidth;
        int top = 0;
        int right = left + mShadowWidth;
        int bottom = getHeight();

        mShadowDrawable.setBounds(left,top,right,bottom);
        mShadowDrawable.draw(canvas);

        canvas.restore();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if(getChildCount() > 0) {
            View childView = getChildAt(0);
            FrameLayout.LayoutParams lp = (LayoutParams) childView.getLayoutParams();

            float x = childView.getX() + lp.leftMargin;
            float y = childView.getY() + lp.topMargin;

            mOriginPoint = new PointF(x, y);
        }
    }

    public interface OnReleaseStateListener {
        /**
         * 释放完成（页面还原或关闭完成）
         *
         * @param parent 父布局
         * @param view 当前释放的View
         */
        void onReleaseCompleted(SnakeHackLayout parent, View view);
    }

    public abstract static class OnEdgeDragListener {
        /**
         * 控件拖拽开始
         *
         * @param parent 父布局
         */
        public void onDragStart(SnakeHackLayout parent) {}

        /**
         * 控件正在拖拽中
         *
         * @param parent 父布局
         * @param view 当前拖拽的View
         * @param left 距离容器左侧的距离（单位：像素）
         */
        public void onDrag(SnakeHackLayout parent, View view, int left) {}

        /**
         * 拖拽过程被释放
         *
         * @param parent 父布局
         * @param view 当前释放的View
         * @param left 距离容器左侧的距离（单位：像素）
         * @param shouldClose true 需要关闭当前页面 false 需要还原当前页面
         */
        public void onRelease(SnakeHackLayout parent, View view, int left, boolean shouldClose, int interceptScene) {}
    }

    /**
     * 拖拽拦截器，允许自定义拦截设置，可以在某些情况下开启拖拽，某些情况下禁用，但手势依然可以监听到
     */
    public static abstract class DragInterceptor {
        /**
         * 自定义拖拽拦截设置，返回负数表示不拦截，即允许拖拽。自定义正数用于标记拦截场景
         *
         * @param parent 父布局
         * @param view 当前拖拽的View
         * @param pointerId 触摸点ID
         * @return 自定义拦截场景值
         */
        public int intercept(SnakeHackLayout parent, View view, int pointerId) {
            return -1;
        }
    }

    public static class SavedState extends BaseSavedState {
        private int contentLeft;
        private int contentTop;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel in) {
            super(in);

            this.contentLeft = in.readInt();
            this.contentTop = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);

            out.writeInt(this.contentLeft);
            out.writeInt(this.contentTop);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
