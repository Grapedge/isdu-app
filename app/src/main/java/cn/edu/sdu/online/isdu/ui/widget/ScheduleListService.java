package cn.edu.sdu.online.isdu.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

import cn.edu.sdu.online.isdu.R;
import cn.edu.sdu.online.isdu.app.MyApplication;
import cn.edu.sdu.online.isdu.bean.Schedule;

public class ScheduleListService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(MyApplication.getContext(), intent);
    }

    public class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private Intent mIntent;
        private Context mContext;
        private List<Schedule> mList = new ArrayList<>();

        ListRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
            mIntent = intent;
        }

        @Override
        public void onCreate() {
            String str = mContext.getSharedPreferences("widget_todo_list", MODE_PRIVATE)
                    .getString("json", "[]");
            mList.addAll(JSON.parseArray(str, Schedule.class));
//            mList.addAll(DailyArrangementWidget.Companion.getTodoList());
        }

        @Override
        public void onDataSetChanged() {
            mList.clear();
            String str = mContext.getSharedPreferences("widget_todo_list", MODE_PRIVATE)
                    .getString("json", "[]");
            mList.addAll(JSON.parseArray(str, Schedule.class));
//            mList.addAll(DailyArrangementWidget.Companion.getTodoList());
        }

        @Override
        public void onDestroy() {
            mList.clear();
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews views = new RemoteViews(mContext.getPackageName(),
                        R.layout.todo_item);

            Schedule schedule = mList.get(position);
            views.setTextViewText(R.id.todo_index, position + 1 + "");
            views.setTextViewText(R.id.todo_name, schedule.getScheduleName());
            views.setTextViewText(R.id.todo_location, schedule.getScheduleLocation());
            views.setTextViewText(R.id.todo_time, schedule.getStartTime().toString() + "~" +
                    schedule.getEndTime().toString());

            views.setEmptyView(R.layout.todo_item, R.layout.todo_item);
            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
