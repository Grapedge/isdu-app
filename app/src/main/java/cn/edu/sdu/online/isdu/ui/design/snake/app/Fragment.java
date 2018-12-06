package cn.edu.sdu.online.isdu.ui.design.snake.app;

import android.animation.Animator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;


import java.lang.reflect.Field;

import cn.edu.sdu.online.isdu.ui.design.snake.Snake;
import cn.edu.sdu.online.isdu.ui.design.snake.animation.SnakeAnimationController;
import cn.edu.sdu.online.isdu.ui.design.snake.annotations.EnableDragToClose;
import cn.edu.sdu.online.isdu.ui.design.snake.config.SnakeConfigException;
import cn.edu.sdu.online.isdu.ui.design.snake.util.FragmentManagerHelper;
import cn.edu.sdu.online.isdu.ui.design.snake.view.SnakeHackLayout;
import cn.edu.sdu.online.isdu.ui.design.snake.view.SnakeTouchInterceptor;

/**
 * AppFragment
 *
 * @author Scott Smith 2018-03-04 14:45
 */
public class Fragment extends android.support.v4.app.Fragment implements SnakeAnimationController {
    private SnakeHackLayout mSnakeLayout;
    private boolean mDisableAnimation;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        replaceWithSnakeLayout(view);
    }

    private void replaceWithSnakeLayout(View view) {
        FragmentManagerHelper fragmentManagerHelper = FragmentManagerHelper.get(getActivity().getSupportFragmentManager());

        if(null == view || fragmentManagerHelper.supportBackStackEmpty()) return;

        EnableDragToClose enableDragToClose = getClass().getAnnotation(EnableDragToClose.class);
        if(null != enableDragToClose && !enableDragToClose.value()) return;

        mSnakeLayout = SnakeHackLayout.getLayout(getActivity());

        if(view.getParent() instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) view.getParent();
            parent.removeView(view);
            mSnakeLayout.addView(view);
            parent.addView(mSnakeLayout);
        }

        try {
            Field mView = android.support.v4.app.Fragment.class.getDeclaredField("mView");
            mView.setAccessible(true);
            mView.set(this, mSnakeLayout);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        Snake.openDragToCloseForFragment(mSnakeLayout, this);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return Snake.wrap(super.onCreateAnimation(transit, enter, nextAnim), this);
    }

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        return Snake.wrap(super.onCreateAnimator(transit, enter, nextAnim), this);
    }

    /**
     * Turn the slide-off function on or off
     *
     * @param enable true: on, false: off
     */
    public void enableDragToClose(Boolean enable) {
        EnableDragToClose enableDragToClose = getClass().getAnnotation(EnableDragToClose.class);
        if(enable) {
            if(null == enableDragToClose || !enableDragToClose.value()) {
                throw new SnakeConfigException("If you want to dynamically turn the slide-off feature on or off, add the EnableDragToClose annotation to " + getClass().getName() + " and set to true");
            }
        }

        if(null != mSnakeLayout) {
            mSnakeLayout.ignoreDragEvent(!enable);
        }
    }

    /**
     * Add OnDragListener for drag event.
     *
     * @param onDragListener {@link Snake.OnDragListener}
     */
    public void addOnDragListener(Snake.OnDragListener onDragListener) {
        if(null != mSnakeLayout && null != onDragListener) {
            mSnakeLayout.addOnDragListener(onDragListener);
        }
    }

    /**
     * Set custom touch interceptor.
     *
     * @param interceptor the touch interceptor
     */
    public void setCustomTouchInterceptor(SnakeTouchInterceptor interceptor) {
        if(null != mSnakeLayout && null != interceptor) {
            mSnakeLayout.setCustomTouchInterceptor(interceptor);
        }
    }

    /**
     * Turn the slide back to home function on or off
     *
     * @param enable true: on, false: off
     */
    public void enableSwipeUpToHome(Boolean enable) {
        if(null != mSnakeLayout) {
            mSnakeLayout.enableSwipeUpToHome(enable);
        }
    }

    @Override
    public void disableAnimation(boolean disable) {
        mDisableAnimation = disable;
    }

    @Override
    public boolean animationDisabled() {
        return mDisableAnimation;
    }
}
