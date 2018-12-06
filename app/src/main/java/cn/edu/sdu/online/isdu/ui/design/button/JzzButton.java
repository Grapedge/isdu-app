package cn.edu.sdu.online.isdu.ui.design.button;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import cn.edu.sdu.online.isdu.R;


public class JzzButton extends View {

    private int maxWidth;
    private int mScreenWidth;
    private int maxHeight;

    private int borderWidth = -1;
    private int borderColor = -1;
    private int textColor = -1;
    private int pressedBorderColor;
    private int pressedTextColor;
    private int cornerRadius;
    private String text;
    private int textSize = 20;
    private boolean halfHeightRadius = false; // 圆角为高度一半

    private int currentBorderColor;
    private int currentTextColor;
    private int currentBackgroundColor = 0x00000000;

    private Paint mPaint = new Paint();
    private TextPaint mTextPaint = new TextPaint();

    private int mWidth;
    private int mHeight;
    private RectF rectF = new RectF();

    private int defMargin = 20;
    private int defPadding = 16;

    public JzzButton(Context context) {
        super(context);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        setClickable(true);
        setFocusable(true);
    }

    public JzzButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        setClickable(true);
        setFocusable(true);

        mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;

        // Get attributes
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.JzzButton);

        for (int i = 0; i < arr.getIndexCount(); i++) {
            int ai = arr.getIndex(i);

            switch (ai) {
                case R.styleable.JzzButton_android_text:
                    text = arr.getText(ai).toString();
                    break;
                case R.styleable.JzzButton_borderWidth:
                    borderWidth = arr.getDimensionPixelSize(ai, 0);
                    break;
                case R.styleable.JzzButton_borderColor:
                    borderColor = arr.getColor(ai, context.getResources().getColor(R.color.colorAccent));
                    pressedBorderColor = Colors.brightnessAdjust(borderColor, 40);
                    break;
                case R.styleable.JzzButton_android_textColor:
                    textColor = arr.getColor(ai, context.getResources().getColor(R.color.colorAccent));
                    pressedTextColor = Colors.brightnessAdjust(textColor, 40);
                    break;
                case R.styleable.JzzButton_android_textSize:
                    textSize = arr.getDimensionPixelSize(ai, 40);
                    break;
                case R.styleable.JzzButton_cornerRaduis:
                    cornerRadius = arr.getDimensionPixelSize(ai, 0);
                    break;
                case R.styleable.JzzButton_halfHeightRadius:
                    halfHeightRadius = arr.getBoolean(ai, false);
                    break;
                case R.styleable.JzzButton_android_textStyle:
                    int type = arr.getInteger(ai, 0x0);
                    switch (type) {
                        case Typeface.BOLD:
                            mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
                            break;
                        case Typeface.ITALIC:
                            mTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
                            break;
                        case Typeface.BOLD_ITALIC:
                            mTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
                            break;
                        default:
                            mTextPaint.setTypeface(Typeface.DEFAULT);
                            break;
                    }
                    break;
            }
        }

        // Release TypedArray Resource
        arr.recycle();

        if (textColor == -1) {
            textColor = context.getResources().getColor(R.color.colorAccent);
            pressedTextColor = Colors.brightnessAdjust(textColor, 40);
        }

        if (borderColor == -1) {
            borderColor = context.getResources().getColor(R.color.colorAccent);
            pressedBorderColor = Colors.brightnessAdjust(borderColor, 40);
        }

        if (borderWidth == -1) {
            borderWidth = 4;
        }

        currentBorderColor = borderColor;
        currentTextColor = textColor;

        mTextPaint.setTextSize(textSize);
    }

    public JzzButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        setClickable(true);
        setFocusable(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int minimumWidth = getSuggestedMinimumWidth();
        final int minimumHeight = getSuggestedMinimumHeight();
        mWidth = measureWidth(minimumWidth, widthMeasureSpec);
        mHeight = measureHeight(minimumHeight, heightMeasureSpec);

        rectF.set(defMargin, defMargin, mWidth - defMargin, mHeight - defMargin);

        if (halfHeightRadius) cornerRadius = mHeight / 2;
        setMeasuredDimension(mWidth, mHeight);
    }

    private int measureWidth(int defWidth, int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.AT_MOST:
                maxWidth = mScreenWidth;
//                if (measureText() + 2 * borderWidth + 2 * defMargin + 2 * defPadding + 2 * cornerRadius
//                        > maxWidth) {
//                    defWidth = getTextWidth(mTextPaint) + 2 * borderWidth + 2 * defMargin + 2 * defPadding;
//                } else {
//                    defWidth = measureText() + 2 * borderWidth + 2 * defMargin + 2 * defPadding + 2 * cornerRadius;
//                }
                defWidth = measureText() + 2 * borderWidth + 2 * defMargin + 2 * defPadding + 2 * cornerRadius;
                maxWidth = Math.min(maxWidth, defWidth);
                defWidth = maxWidth;
                break;
            case MeasureSpec.EXACTLY:
                defWidth = specSize;
                maxWidth = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                defWidth = Math.max(defWidth, specSize);
                maxWidth = defWidth;
        }
        return defWidth;
    }

    private int measureHeight(int defHeight, int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.AT_MOST:
                defHeight = getTextHeight(mTextPaint) + 2 * borderWidth + 2 * defMargin + 2 * defPadding;
                break;
            case MeasureSpec.EXACTLY:
                defHeight = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                defHeight = Math.max(defHeight, specSize);
                break;
        }
        return defHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制文字
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(currentTextColor);
        drawTextCenter(canvas, mTextPaint);
        // 绘制边框
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(borderWidth);
        mPaint.setColor(currentBorderColor);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, mPaint);
        // 绘制背景
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(currentBackgroundColor);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
                currentTextColor = pressedTextColor;
                currentBorderColor = pressedBorderColor;
                currentBackgroundColor = 0x11000000;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                currentBorderColor = borderColor;
                currentTextColor = textColor;
                currentBackgroundColor = 0x00000000;
                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

    private void drawTextCenter(Canvas canvas, TextPaint textPaint) {
        StaticLayout staticLayout = new StaticLayout(text,
                textPaint, maxWidth - 2 * defPadding - 2 * defMargin - 2 * cornerRadius,
                Layout.Alignment.ALIGN_CENTER,
                1, 1, true);
        canvas.save();
        canvas.translate(defMargin + defPadding + cornerRadius, defMargin + defPadding);
        staticLayout.draw(canvas);
        canvas.restore();
    }

    private int getTextHeight(TextPaint textPaint) {
        int width = mWidth - 2 * defPadding - 2 * defMargin - 2 * cornerRadius;
        StaticLayout staticLayout = new StaticLayout(text,
                textPaint,
                width,
                Layout.Alignment.ALIGN_CENTER,
                1, 1, false);
        return staticLayout.getHeight();
    }

    private int getTextWidth(TextPaint textPaint) {
        StaticLayout staticLayout = new StaticLayout(text,
                textPaint,
                maxWidth - 2 * defPadding - 2 * defMargin - 2 * cornerRadius,
                Layout.Alignment.ALIGN_CENTER,
                1, 1, false);
        return staticLayout.getWidth();
    }

    private int measureText() {
        int res = 0;
        String[] list = text.split("\n");
        for (String aList : list) {
            res = Math.max(res, (int) mTextPaint.measureText(aList));
        }

        return res;
    }
}
