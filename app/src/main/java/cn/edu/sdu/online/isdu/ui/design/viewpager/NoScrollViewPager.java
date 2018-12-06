package cn.edu.sdu.online.isdu.ui.design.viewpager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Currency;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/6/12
 *
 * 滚动时锁定的ViewPager
 *
 * #优化滑动手势减少冲突
 ****************************************************
 */

public class NoScrollViewPager extends ViewPager {
    private boolean isScroll = false; // 能否左右翻页
    private AppBarLayout appBarLayout;

    private int currentState = 0;

    public final static int STATE_COLLAPSED = 0; // 折叠状态
    public final static int STATE_EXPANDED = 1; // 展开状态
    public final static int STATE_NORMAL = 2; // 正常状态

    private float lastY;
    private float lastX;

    public NoScrollViewPager(@NonNull Context context) {
        super(context);
    }

    public NoScrollViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean flag = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = ev.getRawY();
                lastX = ev.getRawX();
                break;
            case MotionEvent.ACTION_UP:
//                if (Math.abs(ev.getRawY() - lastY) > Math.abs(ev.getRawX() - lastX)) {
                    if (ev.getRawY() > lastY) {
                        // 向下滑
                        if (currentState != STATE_EXPANDED)
                            if (Math.abs(ev.getRawY() - lastY) < 0.3 * appBarLayout.getTotalScrollRange()) {
                                // 折叠
                                appBarLayout.setExpanded(false, true);
                                currentState = STATE_COLLAPSED;
                                flag = true;
                            } else {
                                // 展开
                                appBarLayout.setExpanded(true, true);
                                currentState = STATE_EXPANDED;
                                flag = true;
                            }
                    } else {
                        // 向上滑
                        if (currentState != STATE_COLLAPSED)
                            if (Math.abs(ev.getRawY() - lastY) < 0.3 * appBarLayout.getTotalScrollRange()) {
                                // 展开
                                appBarLayout.setExpanded(true, true);
                                flag = true;
                            } else {
                                // 折叠
                                appBarLayout.setExpanded(false, true);
                                flag = true;
                            }
                    }

                    if (flag) return false;
//                }
        }
        if (isScroll) {
            return super.onInterceptTouchEvent(ev);
//            if (!super.onInterceptTouchEvent(ev)) {
//
//                if (Math.abs(ev.getRawY() - lastY) < Math.abs(ev.getRawX() - lastX)) {
//                    return true;
//                } else return false;
//
//            } else return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.d("AAA", "onTouchEvent,isScroll=" + isScroll);
        if (isScroll) {
            return super.onTouchEvent(ev);
        } else {
            return true;
        }
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        Log.d("AAA", "dispatchNestedFling");
        return super.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        Log.d("AAA", "onNestedFling");
        return super.onNestedFling(target, velocityX, velocityY, consumed);
    }

    public void setScroll(boolean scroll) {
        isScroll = scroll;
    }

    public void setAppBarLayout(AppBarLayout appBarLayout) {
        this.appBarLayout = appBarLayout;
        if (appBarLayout != null) {
            this.appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (verticalOffset == 0)
                        currentState = STATE_EXPANDED;
                    else if (verticalOffset == -appBarLayout.getTotalScrollRange())
                        currentState = STATE_COLLAPSED;
                    else
                        currentState = STATE_NORMAL;
                }
            });
        }
    }
}
