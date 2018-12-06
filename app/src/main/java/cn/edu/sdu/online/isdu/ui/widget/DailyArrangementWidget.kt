package cn.edu.sdu.online.isdu.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews

import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.MyApplication
import cn.edu.sdu.online.isdu.bean.Schedule
import cn.edu.sdu.online.isdu.bean.User
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.util.EnvVariables
import cn.edu.sdu.online.isdu.util.FileUtil
import cn.edu.sdu.online.isdu.util.Logger
import com.alibaba.fastjson.JSON
import org.json.JSONException
import org.json.JSONObject

/**
 * Implementation of App Widget functionality.
 */
class DailyArrangementWidget : AppWidgetProvider() {

    companion object {
        val todoList = ArrayList<Schedule>()
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them

        if (User.isLogin())
            NetworkAccess.cache(ServerInfo.getScheduleUrl(User.staticUser.uid)) { success, cachePath ->
                if (success) {
                    val jsonString = FileUtil.getStringFromFile(cachePath)
                    try {
                        Schedule.localScheduleList =
                                Schedule.loadCourse(JSONObject(jsonString).getJSONArray("obj"))

                        if (EnvVariables.currentWeek <= 0)
                            EnvVariables.currentWeek = EnvVariables.calculateWeekIndex(System.currentTimeMillis())

                        // 去重
                        for (schedule in Schedule.localScheduleList[EnvVariables.currentWeek - 1][EnvVariables.getCurrentDay() - 1]) {
                            if (!todoList.contains(schedule)) todoList.add(schedule)
                        }
//                    todoList.addAll(Schedule.localScheduleList[EnvVariables.currentWeek - 1][EnvVariables.getCurrentDay() - 1])

                        // 排序
                        for (i in 0 until todoList.size - 1) {
                            for (j in i + 1 until todoList.size) {
                                if (todoList[i].startTime.laterThan(todoList[j].startTime)) {
                                    todoList[i].swap(todoList[j])
                                }
                            }
                        }

                        // 去掉本周不上的课
                        val copyTodoList: MutableList<Schedule> = ArrayList()
                        copyTodoList.addAll(todoList)
                        todoList.clear()
                        for (schedule in copyTodoList) {
                            if (schedule.repeatWeeks.contains(EnvVariables.currentWeek))
                                todoList.add(schedule)
                        }

                        MyApplication.getContext().getSharedPreferences("widget_todo_list",
                                Context.MODE_PRIVATE)
                                .edit()
                                .putString("json", JSON.toJSONString(todoList))
                                .apply()

                        for (id in appWidgetIds) {
                            val theWidget = ComponentName(context, DailyArrangementWidget::class.java)
                            val remoteViews = RemoteViews(context.packageName, R.layout.daily_arrangement_widget)

                            val intent = Intent(context, ScheduleListService::class.java)
                                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)

                            remoteViews.setViewVisibility(R.id.text_view, if (todoList.isEmpty()) View.VISIBLE else View.GONE)
                            remoteViews.setViewVisibility(R.id.list_view, if (todoList.isNotEmpty()) View.VISIBLE else View.GONE)

                            remoteViews.setRemoteAdapter(R.id.list_view, intent)
                            appWidgetManager.updateAppWidget(theWidget, remoteViews)
                        }
                    } catch (e: JSONException) {
                        Logger.log(e)
                    }
                }
            }

//        val list = JSON.parseArray(context.getSharedPreferences("schedule", Context.MODE_PRIVATE)
//                                        .getString("schedule", "[]"))

//        for (id in appWidgetIds) {
//            val theWidget = ComponentName(context, DailyArrangementWidget::class.java)
//            val remoteViews = RemoteViews(context.packageName, R.layout.daily_arrangement_widget)
//
//            val intent = Intent(context, ScheduleListService::class.java)
//            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
//
//            remoteViews.setViewVisibility(R.id.text_view, if (list.isEmpty()) View.VISIBLE else View.GONE)
//            remoteViews.setViewVisibility(R.id.list_view, if (list.isNotEmpty()) View.VISIBLE else View.GONE)
//
//            remoteViews.setRemoteAdapter(R.id.list_view, intent)
//            appWidgetManager.updateAppWidget(theWidget, remoteViews)
//        }

        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        val mgr = AppWidgetManager.getInstance(context)
        val cn = ComponentName(context!!, DailyArrangementWidget::class.java)
        mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn),
                R.id.list_view)
        onUpdate(context, mgr, mgr.getAppWidgetIds(cn))
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
        context.startService(Intent(context, ScheduleListService::class.java))
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
        context.stopService(Intent(context, ScheduleListService::class.java))
    }

}

