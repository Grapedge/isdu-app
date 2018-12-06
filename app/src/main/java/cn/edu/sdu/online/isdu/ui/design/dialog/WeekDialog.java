package cn.edu.sdu.online.isdu.ui.design.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.edu.sdu.online.isdu.R;

public class WeekDialog extends AbstractDialog {
    private Context mContext;
    private int startWeek;
    private int endWeek;
    private TextView txtTitle;
    private TextView btnPositive;
    private TextView btnNegative;
    private View blankView;

    private String title;
    private String pos;
    private String neg;

    private View.OnClickListener onPositiveButtonClickListener;
    private View.OnClickListener onNegativeButtonClickListener;

    private List<Week> weeks = new ArrayList<>();
    private List<Day> days = new ArrayList<>();

    private RecyclerView weekRV, dayRV;

    private boolean singleOption = false; // 是否只能单选

    public WeekDialog(Context context, int startWeek, int endWeek) {
        this(context);
        this.startWeek = startWeek;
        this.endWeek = endWeek;
    }

    private WeekDialog(@NonNull Context context) {
        super(context, R.style.DialogTheme);
        mContext = context;
    }

    public WeekDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.design_week_dialog);

        weekRV = findViewById(R.id.recycler_view_weeks);
        dayRV = findViewById(R.id.recycler_view_days);
        txtTitle = findViewById(R.id.txt_title);
        btnPositive = findViewById(R.id.btn_positive);
        btnNegative = findViewById(R.id.btn_negative);
        blankView = findViewById(R.id.blank_view);

        txtTitle.setText(title);
        btnPositive.setText(pos);
        btnPositive.setOnClickListener(onPositiveButtonClickListener);
        btnNegative.setText(neg);
        btnNegative.setOnClickListener(onNegativeButtonClickListener);

        weekRV.setLayoutManager(new GridLayoutManager(mContext, 5));
        dayRV.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

        weekRV.setAdapter(new WeekAdapter(startWeek, endWeek));

//        days.add(new Day("一"));
//        days.add(new Day("二"));
//        days.add(new Day("三"));
//        days.add(new Day("四"));
//        days.add(new Day("五"));
//        days.add(new Day("六"));
//        days.add(new Day("日"));
        dayRV.setAdapter(new DayAdapter());

        setCancelOnTouchOutside(true);
    }

    public void setSingleOption(boolean singleOption) {
        this.singleOption = singleOption;
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

    public void setTitle(String title) {
        this.title = title;
        if (txtTitle != null) {
            txtTitle.setText(title);
        }
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

    public void setWeeks(List<Week> weeks) {
        this.weeks = weeks;
    }

    public void setDays(List<Day> days) {
        this.days = days;
    }

    public List<Integer> getWeeks() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i <= endWeek - startWeek; i++) {
            if (weeks.get(i).chosen) list.add(i + 1);
        }
        return list;
    }

    public List<Integer> getDays() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            if (days.get(i).chosen) list.add(i + 1);
        }

        return list;
    }

    class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.ViewHolder> {

        WeekAdapter(int start, int end) {
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            final Week week = weeks.get(position);
            holder.textView.setText(week.index + "");
            holder.textView.setBackgroundResource((week.chosen) ?
                    R.drawable.purple_circle : R.drawable.white_circle);
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean flag = weeks.get(position).chosen;
                    if (singleOption) flag = false;
                    if (singleOption) {
                        for (Week week1 : weeks) {
                            week1.chosen = false;
                        }
                    }
                    weeks.get(position).chosen = !flag;
                    notifyDataSetChanged();
                }
            });
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view =
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.circle_select_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return weeks.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView textView;
            ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.item_layout);
            }
        }
    }

    class DayAdapter extends RecyclerView.Adapter<DayAdapter.ViewHolder> {

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            Day day = days.get(position);
            holder.textView.setText(day.name);
            holder.textView.setBackgroundResource((day.chosen) ?
                    R.drawable.purple_circle : R.drawable.white_circle);
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean flag = days.get(position).chosen;
                    if (singleOption) flag = false;
                    if (singleOption) {
                        for (Day day1 : days) {
                            day1.chosen = false;
                        }
                    }
                    days.get(position).chosen = !flag;
                    notifyDataSetChanged();
                }
            });
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view =
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.circle_select_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return days.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView textView;
            ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.item_layout);
            }
        }
    }

    public static class Week {
        int index;
        public boolean chosen = false;
        public Week(int index) {this.index = index;}
    }

    public static class Day {
        String name;
        public boolean chosen = false;
        public Day(String name) {this.name = name;}
    }
}
