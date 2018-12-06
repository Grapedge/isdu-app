package cn.edu.sdu.online.isdu.ui.design;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.BaseInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.edu.sdu.online.isdu.util.Logger;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/14
 *
 * 可拖动缩放的ImageView
 ****************************************************
 */

public class DraggableImageView extends ImageView {

    public static float dragMinScale = 1f;
    public static float dragMaxScale = 4;
    private static final int MODE_SCALE = 1;
    private static final int MODE_TRANSLATE = 2;

    public static float dragThreshold = 100; // 拖动距离阈值

    private Context mContext;
    private Bitmap bitmap;

    private float mImageWidth; // 图片真实宽度
    private float mImageHeight; // 图片真实高度
    private float mScreenWidth; // 屏幕宽度
    private float mScreenHeight; // 屏幕高度

    private float mWidth, mHeight; // 图片当前宽高
    private float dx, dy; // 图片当前位移

    private int curMode = 0;

    private Paint paint = new Paint();
    private AnimRectF rectF = new AnimRectF();
    private AnimRectF lastRectF = new AnimRectF();
    private AnimRectF normalRectF = new AnimRectF();

    private TouchEventCountThread touchEventCountThread = new TouchEventCountThread();
    private OnClickListener onClickListener;
    private OnLongClickListener onLongClickListener;

    private Runnable updateRectRunnable = new Runnable() {
        @Override
        public void run() {
            lastRectF.set(rectF);
        }
    };
    private Handler updateHandler = new Handler();


    public DraggableImageView(final Context context) {
        super(context);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        mContext = context;
    }

    public DraggableImageView(final Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public DraggableImageView(final Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public void setImageResource(int resId) {
        if (resId != 0) {
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), resId);
            mImageWidth = bitmap.getWidth();
            mImageHeight = bitmap.getHeight();

            initBitmap();
        }
        postInvalidate();
    }

    public void setImageBitmap(Bitmap bm) {
        if (bm != null) {
            bitmap = bm;
            mImageWidth = bitmap.getWidth();
            mImageHeight = bitmap.getHeight();

            initBitmap();
        }
        postInvalidate();
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mScreenWidth = getMeasuredWidth();
        mScreenHeight = getMeasuredHeight();
        initBitmap();
    }

    /**
     * 初始化图片尺寸
     */
    private void initBitmap() {
        if (mScreenWidth - 1f < 0) {
            post(new Runnable() {
                @Override
                public void run() {
                    initBitmap();
                }
            });
        } else {
            // 设定缩放矩阵
            // 先缩放X轴，再缩放y轴
            mWidth = mImageWidth;
            mHeight = mImageHeight;
            float curScale; // curScale记录了大图片缩小后的缩放倍数
            if (mImageWidth > mScreenWidth) {
                mWidth = mScreenWidth;
                curScale = mWidth / mImageWidth;
                mHeight = curScale * mHeight;
                dragMaxScale = 2 / curScale;
                dragMinScale = 1f;
            }
            if (mHeight > mScreenHeight) {
                if (mWidth < mScreenWidth) {
                    mWidth = mScreenWidth;
                    curScale = mWidth / mImageWidth;

                    mHeight = curScale * mHeight;
                    dragMaxScale = 2 / curScale;
                    dragMinScale = 1f;
                }
            }

            // 设定居中方阵
            float deltaX = (mScreenWidth - mWidth) / 2;
            float deltaY = (mScreenHeight - mHeight) / 2;
            dx = deltaX; dy = deltaY > 0 ? deltaY : 0;

            rectF.left = dx;
            rectF.right = dx + mWidth;
            rectF.top = dy;
            rectF.bottom = rectF.top + mHeight;

            lastRectF.set(rectF);
            normalRectF.set(rectF);
        }
    }


    float downX, downY, downX1, downY1, curX, curY, curX1, curY1, cx, cy, cx1, cy1;
    boolean moved = false;
    long clickTime = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        updateHandler.removeCallbacks(updateRectRunnable);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (touchEventCountThread.touchCount == 0) {
                    postDelayed(touchEventCountThread, 300);
                    touchEventCountThread.flag = true;
                } else if (touchEventCountThread.touchCount >= 2) {
                    touchEventCountThread.touchCount = 0;
                    touchEventCountThread.pointFS.clear();
                    touchEventCountThread.flag = true;
                }
                touchEventCountThread.pointFS.add(new PointF(event.getX(), event.getY()));

                moved = false;
                downX = event.getX();
                downY = event.getY();
                curMode = MODE_TRANSLATE;
                clickTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                moved = true;
                touchEventCountThread.flag = false;

                downX1 = event.getX(1);
                downY1 = event.getY(1);
                cx = 0.5f * (downX + downX1);
                cy = 0.5f * (downY + downY1);
                curMode = MODE_SCALE;
                break;
            case MotionEvent.ACTION_MOVE:
                curX = event.getX(0);
                curY = event.getY(0);
                if (Math.abs(curX - downX) >= 5 || Math.abs(curY - downY) >= 5) {
                    moved = true;
                    touchEventCountThread.flag = false;

                    if (event.getPointerCount() > 1) {
                        // 缩放
                        curX1 = event.getX(1);
                        curY1 = event.getY(1);
                        float scale = distance(curX, curY, curX1, curY1) /
                                distance(downX, downY, downX1, downY1);

                        cx1 = 0.5f * (curX + curX1);
                        cy1 = 0.5f * (curY + curY1);

                        if (scale * (lastRectF.right - lastRectF.left) < dragMinScale * (normalRectF.right - normalRectF.left)) {
                            scale = dragMinScale * (normalRectF.right - normalRectF.left) / (lastRectF.right - lastRectF.left);
                        }

                        if (scale * (lastRectF.right - lastRectF.left) > dragMaxScale * (normalRectF.right - normalRectF.left)) {
                            scale = dragMaxScale * (normalRectF.right - normalRectF.left) / (lastRectF.right - lastRectF.left);
                        }

                        setScale(scale, cx, cy, cx1, cy1);
                    } else {
                        // 拖动
                        if (curMode == MODE_TRANSLATE) {
                            float dx = curX - downX;
                            float dy = curY - downY;
                            setTranslation(dx, dy);
                            downX = curX; downY = curY;
                        }
                    }
                }

                break;
            case MotionEvent.ACTION_POINTER_UP:
                lastRectF.set(rectF);
                if (curMode == MODE_SCALE) {
                    if (event.getActionIndex() == event.getPointerId(1)) {
                        downX = curX; downY = curY;
                    } else if (event.getActionIndex() == event.getPointerId(0)) {
                        downX = curX1; downY = curY1;
                    }

                }
                curMode = MODE_TRANSLATE;

                invalidate();

                break;
            case MotionEvent.ACTION_UP:
                if (!moved) {
                    touchEventCountThread.touchCount++;
                    if (touchEventCountThread.isLongClick) {
                        touchEventCountThread.touchCount = 0;
                        touchEventCountThread.isLongClick = false;
                    }
                } else {
                    setCenter();
                }

                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }


    /**
     * 缩放图片
     *
     * @param scale 相对于默认缩放倍数的倍数
     * @param cx 按下触摸点的中点X
     * @param cy 按下触摸点的中点Y
     * @param cx1 移动触摸点的中点X
     * @param cy1 移动触摸点的中点Y
     */
    private void setScale(float scale, float cx, float cy, float cx1, float cy1) {
        float left2 = cx1 - scale * (cx - lastRectF.left);
        float right2 = cx1 - scale * (cx - lastRectF.right);
        float top2 = cy1 - scale * (cy - lastRectF.top);
        float bottom2 = cy1 - scale * (cy - lastRectF.bottom);
        rectF.left = left2;
        rectF.right = right2;
        rectF.top = top2;
        rectF.bottom = bottom2;
        invalidate();
    }

    private void setScaleWithAnimation(float scale, float cx, float cy, float cx1, float cy1) {
        float left2 = cx1 - scale * (cx - lastRectF.left);
        float right2 = cx1 - scale * (cx - lastRectF.right);
        float top2 = cy1 - scale * (cy - lastRectF.top);
        float bottom2 = cy1 - scale * (cy - lastRectF.bottom);

        RectF rF = new RectF(left2, top2, right2, bottom2);

        float toX = rF.left, toY = rF.top;
        float deltaX = (mScreenWidth - rF.right + rF.left) / 2;
        if (deltaX < 0) {
            // 过长
            if (rF.left >= 0) {
                toX = 0;
            } else if (rF.right < mScreenWidth) {
                toX = rF.left + mScreenWidth - rF.right;
            }
        } else {
            toX = deltaX;
        }

        float deltaY = (mScreenHeight - rF.bottom + rF.top) / 2;
        if (deltaY < 0) {
            // 过长
            if (rF.top >= 0) {
                toY = 0;
            } else if (rF.bottom < mScreenHeight) {
                toY = mScreenHeight - rF.bottom + rF.top;
            }
        } else {
            toY = deltaY;
        }


        rF.right -= (rF.left - toX);
        rF.left = toX;
        rF.bottom -= (rF.top - toY);
        rF.top = toY;

        int duration = 150;
        ObjectAnimator animatorLeft = ObjectAnimator.ofFloat(rectF, "left", rectF.left, rF.left);
        ObjectAnimator animatorRight = ObjectAnimator.ofFloat(rectF, "right", rectF.right, rF.right);
        ObjectAnimator animatorTop = ObjectAnimator.ofFloat(rectF, "top", rectF.top, rF.top);
        ObjectAnimator animatorBottom = ObjectAnimator.ofFloat(rectF, "bottom", rectF.bottom, rF.bottom);

        animatorLeft.setDuration(duration);
        animatorRight.setDuration(duration);
        animatorTop.setDuration(duration);
        animatorBottom.setDuration(duration);

        animatorLeft.start();
        animatorRight.start();
        animatorTop.start();
        animatorBottom.start();

        updateHandler.postDelayed(updateRectRunnable, duration + 10);
    }

    private void setTranslation(float dx, float dy) {
        rectF.top += dy;
        rectF.bottom += dy;
        rectF.left += dx;
        rectF.right += dx;

        invalidate();
    }

    public void setScale(float scale) {
        this.setScale(scale, cx, cy, cx1, cy1);
    }

    public AnimRectF getScaledRectF(float scale, RectF curRectF) {
        AnimRectF rectF = new AnimRectF();
        float left2 = cx1 - scale * (cx - curRectF.left);
        float right2 = cx1 - scale * (cx - curRectF.right);
        float top2 = cy1 - scale * (cy - curRectF.top);
        float bottom2 = cy1 - scale * (cy - curRectF.bottom);
        rectF.left = left2;
        rectF.right = right2;
        rectF.top = top2;
        rectF.bottom = bottom2;
        return rectF;
    }

    private void setCenter() {
        int duration = 150; // MS

        lastRectF.set(rectF);
        // 拖动还原动画
        float fromX = rectF.left, toX = rectF.left, fromY = rectF.top, toY = rectF.top;
            float deltaX = (mScreenWidth - rectF.right + rectF.left) / 2;
            if (deltaX < 0) {
                // 过长
                if (rectF.left >= 0) {
                    toX = 0;
                } else if (rectF.right < mScreenWidth) {
                    toX = rectF.left + mScreenWidth - rectF.right;
                }
            } else {
                toX = deltaX;
            }

            float deltaY = (mScreenHeight - rectF.bottom + rectF.top) / 2;
            if (deltaY < 0) {
                // 过长
                if (rectF.top >= 0) {
                    toY = 0;
                } else if (rectF.bottom < mScreenHeight) {
                    toY = mScreenHeight - rectF.bottom + rectF.top;
                }
            } else {
                toY = deltaY;
            }



        float dx = (toX - fromX);
        float dy = (toY - fromY);

        ObjectAnimator animatorLeft = ObjectAnimator.ofFloat(rectF, "left", lastRectF.left, lastRectF.left + dx);
        animatorLeft.setDuration(duration);

        ObjectAnimator animatorRight = ObjectAnimator.ofFloat(rectF, "right", lastRectF.right, lastRectF.right + dx);
        animatorRight.setDuration(duration);

        ObjectAnimator animatorTop = ObjectAnimator.ofFloat(rectF, "top", lastRectF.top, lastRectF.top + dy);
        animatorTop.setDuration(duration);

        ObjectAnimator animatorBottom = ObjectAnimator.ofFloat(rectF, "bottom", lastRectF.bottom, lastRectF.bottom + dy);
        animatorBottom.setDuration(duration);

        animatorLeft.start();
        animatorRight.start();
        animatorTop.start();
        animatorBottom.start();


        updateHandler.postDelayed(updateRectRunnable, duration + 10);


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null)
            canvas.drawBitmap(bitmap, null, rectF, paint);
    }

    private float distance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private float distance(PointF p1, PointF p2) {
        return distance(p1.x, p1.y, p2.x, p2.y);
    }

    private float getInterpolatorValue(float dx) {
        float res = dragThreshold - Math.abs(dx);
        return res >= 0 ? res : 0;
    }

    public void autoZoom(float cx, float cy) {
        if ((lastRectF.right - lastRectF.left) + 0.5f < dragMaxScale * (normalRectF.right - normalRectF.left)) {
            setScaleWithAnimation(dragMaxScale * (normalRectF.right - normalRectF.left) / (lastRectF.right - lastRectF.left), cx, cy, cx, cy);
        } else if ((lastRectF.right - lastRectF.left) > dragMinScale * (normalRectF.right - normalRectF.left) + 0.5f) {
            setScaleWithAnimation(dragMinScale * (normalRectF.right - normalRectF.left) / (lastRectF.right - lastRectF.left), cx, cy, cx, cy);
        }
    }

    private void longClick() {
        if (onLongClickListener != null) onLongClickListener.onLongClick(this);
    }

    private void click() {
        if (onClickListener != null) onClickListener.onClick(this);
    }

    private void doubleClick(PointF p1, PointF p2) {
        autoZoom(0.5f * (p1.x + p2.x), 0.5f * (p1.y + p2.y));
//        autoZoom(200, 200);
    }

    class AnimRectF extends RectF {
        public void setLeft(float left) {
            this.left = left;
            invalidate();
        }

        public void setRight(float right) {
            this.right = right;
            invalidate();
        }

        public void setTop(float top) {
            this.top = top;
            invalidate();
        }

        public void setBottom(float bottom) {
            this.bottom = bottom;
            invalidate();
        }
    }

    class TouchEventCountThread implements Runnable {
        int touchCount = 0;
        boolean isLongClick = false;
        boolean flag = true;
        List<PointF> pointFS = new ArrayList<>();

        @Override
        public void run() {
            if (flag) {

                if (touchCount == 0) {
                    isLongClick = true;
                    longClick();
                } else {
                    if (touchCount == 1) {
                        click();
                    } else {
                        doubleClick(pointFS.get(0), pointFS.get(1));
                    }
                    touchCount = 0;
                }

                pointFS.clear();

            } else {

            }
        }

    }

    public interface OnClickListener {
        void onClick(View view);
    }

    public interface OnLongClickListener {
        void onLongClick(View view);
    }

}
