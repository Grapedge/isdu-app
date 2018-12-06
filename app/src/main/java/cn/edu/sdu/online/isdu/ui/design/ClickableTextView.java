package cn.edu.sdu.online.isdu.ui.design;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import cn.edu.sdu.online.isdu.R;

public class ClickableTextView extends TextView {

    private int normalColor = Color.rgb(113, 125, 235);
    private int pressedColor = Color.rgb(60, 77, 234);

    public ClickableTextView(Context context) {
        super(context);
    }

    public ClickableTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // Get attributes
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.ClickableTextView);

        for (int i = 0; i < arr.getIndexCount(); i++) {
            int ai = arr.getIndex(i);

            switch (ai) {
                case R.styleable.ClickableTextView_normalTextColor:
                    normalColor = (arr.getColor(ai, Color.rgb(113, 125, 235)));
                    break;
                case R.styleable.ClickableTextView_pressedTextColor:
                    pressedColor = (arr.getColor(ai, Color.rgb(60, 77, 234)));
                    break;
            }
        }

        // Release TypedArray Resource
        arr.recycle();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setTextColor(pressedColor);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setTextColor(normalColor);
                break;
        }
        return super.onTouchEvent(event);
    }
}
