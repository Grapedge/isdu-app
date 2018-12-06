package cn.edu.sdu.online.isdu.util.download;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.edu.sdu.online.isdu.R;
import cn.edu.sdu.online.isdu.ui.activity.DownloadActivity;
import cn.edu.sdu.online.isdu.util.FileUtil;
import cn.edu.sdu.online.isdu.util.Logger;
import cn.edu.sdu.online.isdu.util.NotificationUtil;
import cn.edu.sdu.online.isdu.util.Settings;
import cn.edu.sdu.online.isdu.util.broadcast.DownloadBroadcastReceiver;

import static cn.edu.sdu.online.isdu.util.download.IDownloadItem.TYPE_CANCELED;
import static cn.edu.sdu.online.isdu.util.download.IDownloadItem.TYPE_DOWNLOADING;
import static cn.edu.sdu.online.isdu.util.download.IDownloadItem.TYPE_FAILED;
import static cn.edu.sdu.online.isdu.util.download.IDownloadItem.TYPE_NEW_INSTANCE;
import static cn.edu.sdu.online.isdu.util.download.IDownloadItem.TYPE_PAUSED;
import static cn.edu.sdu.online.isdu.util.download.IDownloadItem.TYPE_SUCCESS;

public class Download {

    private static final int ID_ADD = 20; // 通知ID增加量
//    private static final int MAX_DOWNLOAD_COUNT = 10; // 最大并行下载数量

    public static Activity activity;

    /**
     * 全局下载任务队列
     * 一旦进入，则在应用运行期间不再退出
     * 在执行{@link Download#init(Activity)}方法时进行筛选
     *
     * 按照下载任务在队列中的位置获取NotifyID
     */
    public static List<IDownloadItem> downloadList = new ArrayList<>();

    /**
     * 全局初始化操作，用来加载本地下载列表
     *
     */
    public static void init(Activity ac) {
        activity = ac;

        String fileName = Settings.DEFAULT_DOWNLOAD_LOCATION + ".download/list.json";
        File file = new File(fileName);
        if (!file.exists()) {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                Logger.log(e);
            }
        }

        String json = FileUtil.getStringFromFile(fileName);
        if (!"".equals(json)) {

            downloadList.clear();
            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    IDownloadItem downloadItem = new IDownloadItem();

                    downloadItem.setDownloadUrl(obj.getString("download_url"));
                    downloadItem.setFileName(obj.getString("file_name"));
                    downloadItem.setStatus(obj.getInt("status"));
                    downloadItem.setProgress(obj.getInt("progress"));

                    downloadList.add(downloadItem);
                }

                setNotifyId();
            } catch (Exception e) {
                Logger.log(e);
            }

        }
    }

    /**
     * 按照顺序编排NotifyID
     */
    private static void setNotifyId() {
        for (int i = 0; i < downloadList.size(); i++) {
            if (downloadList.get(i) != null) {
                downloadList.get(i).setNotifyId(i + ID_ADD);
            }
        }
    }

    /**
     * 获取下载中的任务列表
     *
     * @return 下载中的任务的ID号
     */
    public static List<Integer> getDownloadingIdList() {
        List<Integer> itemIds = new ArrayList<>();
        for (int i = 0; i < downloadList.size(); i++) {
            if (downloadList.get(i) != null)
                if (downloadList.get(i).getStatus() != TYPE_SUCCESS)
                    itemIds.add(downloadList.get(i).getNotifyId());
        }
        return itemIds;
    }

    /**
     * 获取已下载完毕的任务列表
     *
     * @return 已下载任务的ID号
     */
    public static List<Integer> getDownloadedIdList() {
        List<Integer> itemIds = new ArrayList<>();
        for (int i = 0; i < downloadList.size(); i++) {
            if (downloadList.get(i) != null)
                if (downloadList.get(i).getStatus() == TYPE_SUCCESS) {
                    itemIds.add(downloadList.get(i).getNotifyId());
                }
        }
        return itemIds;
    }

    /**
     * 获取NotifyID的任务
     *
     * @param notifyId 指定的NotifyID
     * @return 对应的下载任务
     */
    public static IDownloadItem get(int notifyId) {
        return downloadList.get(notifyId - ID_ADD);
    }

    /**
     * 添加下载项
     *
     * @param downloadItem 待添加的下载项
     */
    public static void add(IDownloadItem downloadItem) {
        downloadList.add(downloadItem);
        setNotifyId();
        save();
    }

    public static void remove(int notifyId) {
        downloadList.set(notifyId - ID_ADD, null);
        save();
    }

    /**
     * 保存
     */
    public static void save() {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < downloadList.size(); i++) {
            if (downloadList.get(i) != null) {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("download_url", downloadList.get(i).getDownloadUrl());
                    obj.put("status", downloadList.get(i).getStatus());
                    obj.put("progress", downloadList.get(i).getProgress());
                    obj.put("file_name", downloadList.get(i).getFileName());
                    jsonArray.put(obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        String filePath = Settings.DEFAULT_DOWNLOAD_LOCATION + ".download/list.json";
        File file = new File(filePath);
        if (!file.exists()) {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(jsonArray.toString());
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void buildNotification(IDownloadItem downloadItem) {
        switch (downloadItem.getStatus()) {
            case TYPE_NEW_INSTANCE:
                NotificationUtil.cancel(downloadItem.getNotifyId());
                new NotificationUtil.Builder(activity)
                        .setMessage("准备下载")
                        .setNotifyId(downloadItem.getNotifyId())
                        .setTitle(downloadItem.getFileName())
                        .setPriority(NotificationUtil.PRIORITY_LOW)
                        .setIntent(PendingIntent.getActivity(activity, downloadItem.getNotifyId(),
                                new Intent(activity, DownloadActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                        .addAction(R.mipmap.ic_alpha_not, "暂停下载",
                                PendingIntent.getBroadcast(activity, downloadItem.getNotifyId(),
                                        new Intent(activity, DownloadBroadcastReceiver.class)
                                                .putExtra("notify_id", downloadItem.getNotifyId())
                                                .setAction("download_pause"),
                                        PendingIntent.FLAG_UPDATE_CURRENT))
                        .addAction(R.mipmap.ic_alpha_not, "取消下载",
                                PendingIntent.getBroadcast(activity, downloadItem.getNotifyId(),
                                        new Intent(activity, DownloadBroadcastReceiver.class)
                                                .putExtra("notify_id", downloadItem.getNotifyId())
                                                .setAction("download_cancel"),
                                        PendingIntent.FLAG_UPDATE_CURRENT))
                        .show();
                break;
            case TYPE_DOWNLOADING:
                new NotificationUtil.Builder(activity)
                        .setMessage("下载中 " + downloadItem.getProgress() + "% " +
                                (downloadItem.fileLengthDetector != null ? downloadItem.fileLengthDetector.getSpeed() : ""))
                        .setProgress(100, downloadItem.getProgress(), false)
                        .setTitle(downloadItem.getFileName())
                        .setNotifyId(downloadItem.getNotifyId())
                        .setOnGoing(true)
                        .setPriority(NotificationUtil.PRIORITY_LOW)
                        .setIntent(PendingIntent.getActivity(activity, downloadItem.getNotifyId(),
                                new Intent(activity, DownloadActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                        .addAction(R.mipmap.ic_alpha_not, "暂停下载",
                                PendingIntent.getBroadcast(activity, downloadItem.getNotifyId(),
                                        new Intent(activity, DownloadBroadcastReceiver.class)
                                                .putExtra("notify_id", downloadItem.getNotifyId())
                                                .setAction("download_pause"),
                                        PendingIntent.FLAG_UPDATE_CURRENT))
                        .addAction(R.mipmap.ic_alpha_not, "取消下载",
                                PendingIntent.getBroadcast(activity, downloadItem.getNotifyId(),
                                        new Intent(activity, DownloadBroadcastReceiver.class)
                                                .putExtra("notify_id", downloadItem.getNotifyId())
                                                .setAction("download_cancel"),
                                        PendingIntent.FLAG_UPDATE_CURRENT))
                        .show();
                break;
            case TYPE_SUCCESS:
                NotificationUtil.cancel(downloadItem.getNotifyId());
                new NotificationUtil.Builder(activity)
                        .setMessage("下载完成")
                        .setNotifyId(downloadItem.getNotifyId())
                        .setTitle(downloadItem.getFileName())
                        .setPriority(NotificationUtil.PRIORITY_DEFAULT)
                        .setIntent(PendingIntent.getActivity(activity, downloadItem.getNotifyId(),
                                new Intent(activity, DownloadActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                        .addAction(R.mipmap.ic_alpha_not, "打开",
                                PendingIntent.getBroadcast(activity, downloadItem.getNotifyId(),
                                        new Intent(activity, DownloadBroadcastReceiver.class)
                                                .putExtra("notify_id", downloadItem.getNotifyId())
                                                .setAction("download_open"),
                                        PendingIntent.FLAG_UPDATE_CURRENT))
                        .show();
                break;
            case TYPE_CANCELED:
                NotificationUtil.cancel(downloadItem.getNotifyId());
                new NotificationUtil.Builder(activity)
                        .setMessage("下载取消")
                        .setNotifyId(downloadItem.getNotifyId())
                        .setTitle(downloadItem.getFileName())
                        .setPriority(NotificationUtil.PRIORITY_LOW)
                        .setIntent(PendingIntent.getActivity(activity, downloadItem.getNotifyId(),
                                new Intent(activity, DownloadActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                        .addAction(R.mipmap.ic_alpha_not, "重新下载",
                                PendingIntent.getBroadcast(activity, downloadItem.getNotifyId(),
                                        new Intent(activity, DownloadBroadcastReceiver.class)
                                                .putExtra("notify_id", downloadItem.getNotifyId())
                                                .setAction("download_start"),
                                        PendingIntent.FLAG_UPDATE_CURRENT))
                        .show();
                break;
            case TYPE_FAILED:
                NotificationUtil.cancel(downloadItem.getNotifyId());
                new NotificationUtil.Builder(activity)
                        .setMessage("下载失败")
                        .setNotifyId(downloadItem.getNotifyId())
                        .setTitle(downloadItem.getFileName())
                        .setPriority(NotificationUtil.PRIORITY_DEFAULT)
                        .setIntent(PendingIntent.getActivity(activity, downloadItem.getNotifyId(),
                                new Intent(activity, DownloadActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                        .addAction(R.mipmap.ic_alpha_not, "重试",
                                PendingIntent.getBroadcast(activity, downloadItem.getNotifyId(),
                                        new Intent(activity, DownloadBroadcastReceiver.class)
                                                .putExtra("notify_id", downloadItem.getNotifyId())
                                                .setAction("download_start"),
                                        PendingIntent.FLAG_UPDATE_CURRENT))
                        .addAction(R.mipmap.ic_alpha_not, "取消下载",
                                PendingIntent.getBroadcast(activity, downloadItem.getNotifyId(),
                                        new Intent(activity, DownloadBroadcastReceiver.class)
                                                .putExtra("notify_id", downloadItem.getNotifyId())
                                                .setAction("download_cancel"),
                                        PendingIntent.FLAG_UPDATE_CURRENT))
                        .show();
                break;
            case TYPE_PAUSED:
                NotificationUtil.cancel(downloadItem.getNotifyId());
                new NotificationUtil.Builder(activity)
                        .setMessage("下载暂停")
                        .setNotifyId(downloadItem.getNotifyId())
                        .setTitle(downloadItem.getFileName())
                        .setPriority(NotificationUtil.PRIORITY_LOW)
                        .setIntent(PendingIntent.getActivity(activity, downloadItem.getNotifyId(),
                                new Intent(activity, DownloadActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                        .addAction(R.mipmap.ic_alpha_not, "继续下载",
                                PendingIntent.getBroadcast(activity, downloadItem.getNotifyId(),
                                        new Intent(activity, DownloadBroadcastReceiver.class)
                                                .putExtra("notify_id", downloadItem.getNotifyId())
                                                .setAction("download_start"),
                                        PendingIntent.FLAG_UPDATE_CURRENT))
                        .addAction(R.mipmap.ic_alpha_not, "取消下载",
                                PendingIntent.getBroadcast(activity, downloadItem.getNotifyId(),
                                        new Intent(activity, DownloadBroadcastReceiver.class)
                                                .putExtra("notify_id", downloadItem.getNotifyId())
                                                .setAction("download_cancel"),
                                        PendingIntent.FLAG_UPDATE_CURRENT))
                        .show();
                break;
        }
    }
}
