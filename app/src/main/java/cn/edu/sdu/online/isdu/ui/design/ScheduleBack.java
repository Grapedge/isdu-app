package cn.edu.sdu.online.isdu.ui.design;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import cn.edu.sdu.online.isdu.R;

public class ScheduleBack extends View {

    private int cardColor = 0xFFF2F2F2;
    private int invalidColor = 0xFFDCDCDC; // 非当前周的卡片背景颜色
    private Paint mPaint = new Paint();
    private int mWidth, mHeight;
    private RectF rect = new RectF();

    public ScheduleBack(Context context) {
        super(context);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public ScheduleBack(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        // Get attributes
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.ScheduleBack);

        for (int i = 0; i < arr.getIndexCount(); i++) {
            int ai = arr.getIndex(i);
            switch (ai) {
                case R.styleable.ScheduleBack_cardColor:
                    cardColor = (arr.getColor(ai, Color.parseColor("#FFF2F2F2")));
                    break;
            }
        }

        // Release TypedArray Resource
        arr.recycle();

        mPaint.setMaskFilter(new BlurMaskFilter(6, BlurMaskFilter.Blur.NORMAL));
        mPaint.setColor(cardColor);
    }

    public ScheduleBack(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        rect.set(5, 5, mWidth - 5, mHeight - 5);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setColor(cardColor);
        canvas.drawRoundRect(rect, 8, 8, mPaint);
    }

    public void setCardColor(int cardColor) {
        this.cardColor = cardColor;
        invalidate();
    }
}
