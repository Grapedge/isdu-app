package cn.edu.sdu.online.isdu.util.download;

import android.os.AsyncTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

import cn.edu.sdu.online.isdu.interfaces.IDownloadListener;
import cn.edu.sdu.online.isdu.util.Settings;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadAsyncTask extends AsyncTask<Integer, Integer, Integer> {

    private IDownloadListener listener;

    private boolean isCanceled = false;
    private boolean isPaused = false;

    public DownloadAsyncTask(IDownloadListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (listener != null)
            switch (integer) {
                case IDownloadItem.TYPE_SUCCESS:
                    listener.onSuccess();
                    break;
                case IDownloadItem.TYPE_FAILED:
                    listener.onFailed();
                    break;
                case IDownloadItem.TYPE_CANCELED:
                    listener.onCanceled();
                    break;
                case IDownloadItem.TYPE_PAUSED:
                    listener.onPaused();
                    break;
            }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (listener != null) listener.onProgress(values[0]);
    }

    public void cancelDownload() {
        isCanceled = true;
    }

    public void pauseDownload() {
        isPaused = true;
    }

    @Override
    protected Integer doInBackground(Integer... notifyIds) {
        int notifyId = notifyIds[0];

        InputStream is = null;
        RandomAccessFile randomAccessFile = null;
        try {
            File downloadFile =
                    new File(Settings.DEFAULT_DOWNLOAD_LOCATION + Download.get(notifyId).getFileName());

            long contentLength = getContentLength(Download.get(notifyId).getDownloadUrl());
            long downloadedLength = 0;

            if (contentLength == 0) {
                return IDownloadItem.TYPE_FAILED;
            }

            if (!downloadFile.exists()) {
                if (!downloadFile.getParentFile().exists()) downloadFile.getParentFile().mkdirs();
                downloadFile.createNewFile();
            } else {
                downloadedLength = downloadFile.length();
                if (downloadedLength == contentLength) {
                    return IDownloadItem.TYPE_SUCCESS;
                }
            }

            randomAccessFile = new RandomAccessFile(downloadFile, "rw");

            // 构建网络连接

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build();
            Request request = new Request.Builder()
                    .addHeader("RANGE", "bytes=" + downloadedLength + "-")
                    .url(Download.get(notifyId).getDownloadUrl())
                    .build();
            Response response = client.newCall(request).execute();

            if (response != null) {

                is = response.body().byteStream();
                if (is == null) {
                    return IDownloadItem.TYPE_FAILED;
                } else {
                    randomAccessFile.seek(downloadedLength);

                    int len = 0;
                    byte[] b = new byte[4096];

                    while((len = is.read(b)) != -1) {
                        if (isPaused) {
                            return IDownloadItem.TYPE_PAUSED;
                        } else if (isCanceled) {
                            return IDownloadItem.TYPE_CANCELED;
                        } else {
                            randomAccessFile.write(b, 0, len);
                            downloadedLength += len;
                            int progress = (int) (downloadedLength * 100 / contentLength);
                            onProgressUpdate(progress);
                        }
                    }
                }

                response.body().close();
                return IDownloadItem.TYPE_SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) listener.onFailed();
            return IDownloadItem.TYPE_FAILED;
        } finally {
            try {
                if (is != null) is.close();
                if (randomAccessFile != null) randomAccessFile.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return IDownloadItem.TYPE_FAILED;
    }


    private long getContentLength(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            response.close();
            return contentLength;
        }
        return 0;
    }
}
