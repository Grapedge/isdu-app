package cn.edu.sdu.online.isdu.ui.design.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import cn.edu.sdu.online.isdu.R;
import cn.edu.sdu.online.isdu.bean.Schedule;
import cn.edu.sdu.online.isdu.util.ScheduleTime;

public class TimeDialog extends AbstractDialog {

    private Context mContext;
    private TimePicker timePicker;

    private TextView btnPositive;
    private TextView btnNegative;
    private View blankView;

    private View.OnClickListener onPositiveButtonClickListener;
    private View.OnClickListener onNegativeButtonClickListener;

    private String pos;
    private String neg;

    private ScheduleTime scheduleTime;

    public TimeDialog(Context context, ScheduleTime time) {
        this(context, R.style.DialogTheme);
        scheduleTime = time;
    }

    public TimeDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    public TimeDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.design_time_dialog);

        timePicker = findViewById(R.id.time_picker);
        btnPositive = findViewById(R.id.btn_positive);
        btnNegative = findViewById(R.id.btn_negative);
        blankView = findViewById(R.id.blank_view);

        btnPositive.setText(pos);
        btnPositive.setOnClickListener(onPositiveButtonClickListener);
        btnNegative.setText(neg);
        btnNegative.setOnClickListener(onNegativeButtonClickListener);

        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(scheduleTime.getHour());
        timePicker.setCurrentMinute(scheduleTime.getMinute());

        setCancelOnTouchOutside(true);
    }


    public void setPositiveButton(String text, View.OnClickListener listener) {
        pos = text;
        onPositiveButtonClickListener = listener;

        if (btnPositive != null) {
            btnPositive.setText(pos);
            btnPositive.setOnClickListener(onPositiveButtonClickListener);
        }
    }

    public void setNegativeButton(String text, View.OnClickListener listener) {
        neg = text;
        onNegativeButtonClickListener = listener;

        if (btnPositive != null) {
            btnNegative.setText(neg);
            btnNegative.setOnClickListener(onNegativeButtonClickListener);
        }
    }

    private void setCancelOnTouchOutside(boolean b) {
        if (b) {
            blankView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        } else {
            blankView.setOnClickListener(null);
        }
    }

    public ScheduleTime getTime() {
        return new ScheduleTime(timePicker.getCurrentHour(), timePicker.getCurrentMinute());
    }
}
