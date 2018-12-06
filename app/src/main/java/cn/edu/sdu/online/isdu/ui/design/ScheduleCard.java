package cn.edu.sdu.online.isdu.ui.design;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import cn.edu.sdu.online.isdu.R;

public class ScheduleCard extends FrameLayout {

    private int cardColor = 0xFFF2F2F2;
    private int invalidColor = 0xFFDCDCDC; // 非当前周的卡片背景颜色
    private String cardText;
    private boolean isCurrentWeek = true;

    private ScheduleBack scheduleBack;
    private TextView scheduleText;

    public ScheduleCard(@NonNull Context context) {
        this(context, null);
    }

    public ScheduleCard(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.schedult_card_item, this);

        scheduleBack = view.findViewById(R.id.schedule_back);
        scheduleText = view.findViewById(R.id.schedule_text);

        // Get attributes
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.ScheduleCard);

        for (int i = 0; i < arr.getIndexCount(); i++) {
            int ai = arr.getIndex(i);
            switch (ai) {
                case R.styleable.ScheduleCard_cardColor:
                    cardColor = arr.getColor(ai, Color.parseColor("#FFF2F2F2"));
                    break;
                case R.styleable.ScheduleCard_cardText:
                    cardText = arr.getString(ai);
                    break;
                case R.styleable.ScheduleCard_currentWeek:
                    isCurrentWeek = arr.getBoolean(ai, true);
                    break;
            }
        }

        // Release TypedArray Resource
        arr.recycle();

        setCurrentWeek(isCurrentWeek);
        setCardText(cardText);
        setCardColor(cardColor);
    }

    public ScheduleCard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCardColor(int cardColor) {
        this.cardColor = cardColor;
        if (scheduleBack != null)
            scheduleBack.setCardColor(cardColor);
    }

    public void setCardText(String cardText) {
        this.cardText = cardText;
        if (scheduleText != null) {
            scheduleText.setText(cardText);
        }
    }

    public void setCurrentWeek(boolean currentWeek) {
        isCurrentWeek = currentWeek;
        if (scheduleBack != null && scheduleText != null) {
            if (isCurrentWeek) {
                scheduleBack.setCardColor(cardColor);
                scheduleText.setTextColor(Color.parseColor("#ffffffff"));
            } else {
                scheduleBack.setCardColor(invalidColor);
                scheduleText.setTextColor(Color.parseColor("#ff7d7d7d"));
            }
        }
    }

    public int getCardColor() {
        return cardColor;
    }

    public String getCardText() {
        return cardText;
    }

    public boolean isCurrentWeek() {
        return isCurrentWeek;
    }
}
