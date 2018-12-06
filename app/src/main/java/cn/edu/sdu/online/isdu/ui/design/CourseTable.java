package cn.edu.sdu.online.isdu.ui.design;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import cn.edu.sdu.online.isdu.R;
import cn.edu.sdu.online.isdu.bean.Schedule;
import cn.edu.sdu.online.isdu.util.EnvVariables;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class CourseTable extends LinearLayout {
    private static final String[] topbarNames = {"一", "二", "三", "四", "五", "六", "日"};

    private List<List<Schedule>> scheduleList;
    private LinearLayout[][] layouts = new LinearLayout[7][5];
    private int weekIndex = 1;
    private View overlay;
    private TextView overlayText;

    private LinearLayout contentLayout;
    private LinearLayout topBar;

    public CourseTable(Context context) {
        super(context);
    }

    public CourseTable(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.course_table, this);

        // Init widget
        contentLayout = findViewById(R.id.course_content_layout);
        overlay = findViewById(R.id.course_table_overlay);
        overlayText = findViewById(R.id.course_table_overlay_text);
        topBar = findViewById(R.id.course_table_top_bar);

        overlay.setOnClickListener(v -> {
            overlay.setVisibility(View.GONE);
        });
    }

    public CourseTable(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initLayout() {
            contentLayout.removeAllViews();
            topBar.removeAllViews();
            // Add top bars
            for (int i = 0; i < 7; i++) {
                TextView textView = new TextView(getContext());
                textView.setText(topbarNames[i]);
                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(18);

                if (weekIndex == EnvVariables.currentWeek && i + 1 == EnvVariables.getCurrentDay()) {
                    textView.setTextColor(Color.WHITE);
                    textView.setBackgroundColor(Color.parseColor("#717DEB"));
                } else {
                    textView.setTextColor(Color.parseColor("#717DEB"));
                    textView.setBackgroundColor(Color.parseColor("#dcdcdc"));
                }

                topBar.addView(textView);
                LayoutParams lp = (LayoutParams) textView.getLayoutParams();
                lp.width = 0;
                lp.height = MATCH_PARENT;
                lp.weight = 1f;
                textView.setLayoutParams(lp);
            }

            for (int i = 0; i < 7; i++) {
                LinearLayout rowContainer = new LinearLayout(getContext());
                contentLayout.addView(rowContainer);
                rowContainer.setOrientation(VERTICAL);
                LayoutParams lp = (LayoutParams) rowContainer.getLayoutParams();
                lp.width = 0;
                lp.height = MATCH_PARENT;
                lp.weight = 1f;
                rowContainer.setLayoutParams(lp);

                for (int j = 0; j < 5; j++) {
                    layouts[i][j] = new LinearLayout(getContext());
                    rowContainer.addView(layouts[i][j]);
                    layouts[i][j].setOrientation(VERTICAL);
                    LayoutParams params = (LayoutParams) layouts[i][j].getLayoutParams();
                    params.width = MATCH_PARENT;
                    params.height = 0;
                    params.weight = 1f;
                    layouts[i][j].setLayoutParams(params);
                }

            }

    }

    private void addCard(ScheduleCard card, int week, int index) {
        if (layouts[week][index - 1] != null) {
            layouts[week][index - 1].addView(card);
            LinearLayout.LayoutParams lp = (LayoutParams) card.getLayoutParams();
            lp.width = MATCH_PARENT;
            lp.height = 0;
            lp.weight = 1f;
            card.setLayoutParams(lp);
        }
    }

    private void fillCourses() {
        if (contentLayout != null) {
            initLayout();
            if (scheduleList != null) {
                for (int i = 0; i < scheduleList.size(); i++) {
                    for (int j = 0; j < scheduleList.get(i).size(); j++) {
                        Schedule schedule = scheduleList.get(i).get(j);
                        ScheduleCard card = new ScheduleCard(getContext());
                        String cardText = schedule.getScheduleName() + "@" + schedule.getScheduleLocation();
                        card.setCardColor(schedule.getScheduleColor());
                        card.setCurrentWeek(schedule.getRepeatWeeks().contains(weekIndex));
                        card.setCardText(String.format("%s%s", cardText, !card.isCurrentWeek() ? "\n[非本周]" : ""));

                        addCard(card, i, schedule.getCourseOrder());
                        card.setOnClickListener(v -> {
                            overlayText.setText(String.format("%s%s", cardText, !card.isCurrentWeek() ? "\n[非本周]" : ""));
                            overlayText.setBackgroundColor(card.getCardColor());
                            overlay.setVisibility(View.VISIBLE);
                        });
                    }
                }
            }
        }
    }

    public void setScheduleList(List<List<Schedule>> list) {
        this.scheduleList = list;
        fillCourses();
    }

    public void setCurrentWeekIndex(int weekIndex) {
        this.weekIndex = weekIndex;
        fillCourses();
    }

    public boolean onBackPressed() {
        if (overlay.isShown()) {
            overlay.setVisibility(View.GONE);
            return true;
        } else return false;
    }
}
