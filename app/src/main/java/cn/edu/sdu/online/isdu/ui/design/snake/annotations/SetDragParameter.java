package cn.edu.sdu.online.isdu.ui.design.snake.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 滑动关闭参数设置（可以用于单页特殊处理)
 *
 * @author Scott Smith 2017-12-19 17:43
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SetDragParameter {
    /**
     * 仅监听快速滑动关闭
     */
    boolean onlyListenToFastSwipe() default false;

    /**
     * 快速滑动最小检测速度
     */
    int minVelocity() default 2000;

    /**
     * 隐藏阴影边缘
     */
    boolean hideShadowOfEdge() default false;

    /**
     * 阴影起始颜色（阴影边缘隐藏后，该设置失效)
     */
    String shadowStartColor() default "#00000000";

    /**
     * 阴影边缘结束颜色（阴影边缘隐藏后，该设置失效）
     */
    String shadowEndColor() default "#50000000";

    /**
     * 开启快速上滑返回桌面功能，默认为false
     */
    boolean enableSwipeUpToHome() default false;

    /**
     * 是否允许页面联动，默认为true
     */
    boolean allowPageLinkage() default true;
}
