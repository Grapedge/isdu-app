package cn.edu.sdu.online.isdu.ui.design.viewpager;

import android.content.Context;
import android.graphics.PointF;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ChildViewPager extends ViewPager {

    public ChildViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private float mLastMotionX;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final float x = ev.getX();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                mLastMotionX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                // 若连续快速滑动，则不触发ACTION_DOWN和ACTION_UP，直接进入ACTION_MOVE处理
                // 即还处于动画中时一定进入的是ACTION_MOVE
                if (x - mLastMotionX > 5 && getCurrentItem() == 0) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                if (x - mLastMotionX < -5
                        && getCurrentItem() == getAdapter().getCount() - 1) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_UP:
                mLastMotionX = x;
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            default:
                break;
        }

        return super.dispatchTouchEvent(ev);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

}