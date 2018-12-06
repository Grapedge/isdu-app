package cn.edu.sdu.online.isdu.ui.design.snake.animation;

/**
 * Animation controller, just used to control pop animation for fragment now.
 *
 * @author Scott Smith 2018-01-05 22:05
 */
public interface SnakeAnimationController {
    /**
     * Disable animation
     *
     * @param disable disabel or enable animation.
     */
    void disableAnimation(boolean disable);

    /**
     * Get the animation on.
     *
     * @return true:disabled false: enabled
     */
    boolean animationDisabled();
}
