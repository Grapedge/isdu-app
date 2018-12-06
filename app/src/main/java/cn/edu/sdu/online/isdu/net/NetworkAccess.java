package cn.edu.sdu.online.isdu.net;


import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.edu.sdu.online.isdu.app.MyApplication;
import cn.edu.sdu.online.isdu.net.pack.ServerInfo;
import cn.edu.sdu.online.isdu.util.Logger;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkAccess {

    public static Call buildRequest(String url, Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
        return call;
    }

    @Deprecated
    public static Call buildRequest(String url, String str, Callback callback) {
        MediaType mediaType = MediaType.parse("text/html; charset=utf-8");
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(mediaType, str))
                .build();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
        return call;
    }

    public static Call buildRequest(String url, List<String> key, List<String> value, Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        FormBody.Builder formBody = new FormBody.Builder();
        for (int i = 0; i < key.size(); i++) {
            formBody.add(key.get(i), value.get(i));
        }
        RequestBody requestBody = formBody.build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
        return call;
    }

    public static Call buildRequest(String url, String key, String value, Callback callback) {
        List<String> keys = new ArrayList<>(); keys.add(key);
        List<String> values = new ArrayList<>(); values.add(value);
        return buildRequest(url, keys, values, callback);
    }

    public static Call cache(String url, String key, String value, @Nullable final OnCacheFinishListener listener) {
        File cacheDir = MyApplication.getContext().getCacheDir();

        String s = url.substring(
                (url.startsWith("http")) ? url.indexOf("/", 8) : url.indexOf("/")
                , url.length());
        char[] chars = s.toLowerCase().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '/')
                chars[i] = '_';
            if (chars[i] == '?')
                chars[i] = '.';
            if (chars[i] == '&')
                chars[i] = '_';
        }


        final File cacheFile = new File(cacheDir.getAbsolutePath() + "/" + new String(chars));

        if (cacheFile.exists()) {
            if (listener != null)
                listener.onFinish(true, cacheFile.getAbsolutePath());
        } else try {
            cacheFile.createNewFile();
        } catch (IOException e) {
            Logger.log(e);
        }

        return buildRequest(url, key, value, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.log(e);
                if (listener != null)
                    listener.onFinish(false, null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    InputStream is = response.body().byteStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(cacheFile));

                    int len = 0;
                    byte[] b = new byte[2048];
                    while ((len = bis.read(b)) > 0) {
                        bos.write(b, 0, len);
                    }

                    bos.flush();
                    bos.close();
                    bis.close();
                    is.close();
                    if (listener != null)
                        listener.onFinish(true, cacheFile.getAbsolutePath());
                } catch (Exception e) {
                    Logger.log(e);
                }
            }
        });
    }

    public static Call cache(String url, List<String> keys, List<String> values, @Nullable final OnCacheFinishListener listener) {
        File cacheDir = MyApplication.getContext().getCacheDir();

        String s = url.substring(
                (url.startsWith("http")) ? url.indexOf("/", 8) : url.indexOf("/")
                , url.length());
        char[] chars = s.toLowerCase().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '/')
                chars[i] = '_';
            if (chars[i] == '?')
                chars[i] = '.';
            if (chars[i] == '&')
                chars[i] = '_';
        }


        final File cacheFile = new File(cacheDir.getAbsolutePath() + "/" + new String(chars));

        if (cacheFile.exists()) {
            if (listener != null)
                listener.onFinish(true, cacheFile.getAbsolutePath());
        } else try {
            cacheFile.createNewFile();
        } catch (IOException e) {
            Logger.log(e);
        }

        return buildRequest(url, keys, values, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.log(e);
                if (listener != null)
                    listener.onFinish(false, null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    InputStream is = response.body().byteStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(cacheFile));

                    int len = 0;
                    byte[] b = new byte[2048];
                    while ((len = bis.read(b)) > 0) {
                        bos.write(b, 0, len);
                    }

                    bos.flush();
                    bos.close();
                    bis.close();
                    is.close();
                    if (listener != null)
                        listener.onFinish(true, cacheFile.getAbsolutePath());
                } catch (Exception e) {
                    Logger.log(e);
                }
            }
        });
    }

    public static Call cache(String url, @Nullable final OnCacheFinishListener listener) {
        File cacheDir = MyApplication.getContext().getCacheDir();

        String s = url.substring(
                (url.startsWith("http")) ? url.indexOf("/", 8) : url.indexOf("/")
                , url.length());
        char[] chars = s.toLowerCase().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '/')
                chars[i] = '_';
            if (chars[i] == '?')
                chars[i] = '.';
            if (chars[i] == '&')
                chars[i] = '_';
        }


        final File cacheFile = new File(cacheDir.getAbsolutePath() + "/" + new String(chars));

        if (cacheFile.exists()) {
            if (listener != null)
                listener.onFinish(true, cacheFile.getAbsolutePath());
        } else try {
            cacheFile.createNewFile();
        } catch (IOException e) {
            Logger.log(e);
        }

        return buildRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.log(e);
                if (listener != null)
                    listener.onFinish(false, null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    InputStream is = response.body().byteStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(cacheFile));

                    int len = 0;
                    byte[] b = new byte[2048];
                    while ((len = bis.read(b)) > 0) {
                        bos.write(b, 0, len);
                    }

                    bos.flush();
                    bos.close();
                    bis.close();
                    is.close();
                    if (listener != null)
                        listener.onFinish(true, cacheFile.getAbsolutePath());
                } catch (Exception e) {
                    Logger.log(e);
                }
            }
        });

    }

    /**
     * 成绩专用
     * @param url
     * @param listener
     */
    public static Call cacheGrade(String url, @Nullable final OnCacheFinishListener listener) {
        File cacheDir = MyApplication.getContext().getCacheDir();

        String s = (url.substring((ServerInfo.url).length(), url.length()));
        char[] chars = s.toLowerCase().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '/')
                chars[i] = '_';
            if (chars[i] == '?')
                chars[i] = '.';
            if (chars[i] == '&')
                chars[i] = '_';
        }


        final File cacheFile = new File(cacheDir.getAbsolutePath() + "/" + new String(chars));

        if (cacheFile.exists()) {
            if (listener != null)
                listener.onFinish(true, cacheFile.getAbsolutePath());
        } else try {
            cacheFile.createNewFile();
            listener.onFinish(true, cacheFile.getAbsolutePath());
        } catch (IOException e) {
            Logger.log(e);
        }

        return buildRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.log(e);
                if (listener != null)
                    listener.onFinish(false, null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    InputStream is = response.body().byteStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(cacheFile));

                    int len = 0;
                    byte[] b = new byte[2048];
                    while ((len = bis.read(b)) > 0) {
                        bos.write(b, 0, len);
                    }

                    bos.flush();
                    bos.close();
                    bis.close();
                    is.close();
                    if (listener != null)
                        listener.onFinish(true, cacheFile.getAbsolutePath());
                } catch (Exception e) {
                    Logger.log(e);
                }
            }
        });

    }

    /**
     * 获取网页JSON中的某个Key的值并缓存这个值
     *
     * @param url URL地址
     * @param key 需要查询的Key
     * @param listener 返回监听器
     */
    public static Call cache(String url, final String key, @Nullable final OnCacheFinishListener listener) {
        File cacheDir = MyApplication.getContext().getCacheDir();

        String s = url.substring(
                (url.startsWith("http")) ? url.indexOf("/", 8) : url.indexOf("/")
                , url.length());
        char[] chars = s.toLowerCase().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '/')
                chars[i] = '_';
            if (chars[i] == '?')
                chars[i] = '.';
            if (chars[i] == '&')
                chars[i] = '_';
        }


        final File cacheFile = new File(cacheDir.getAbsolutePath() + "/" + new String(chars) + "#" + key);

        if (cacheFile.exists()) {
            if (listener != null)
                listener.onFinish(true, cacheFile.getAbsolutePath());
        } else try {
            cacheFile.createNewFile();
        } catch (IOException e) {
            Logger.log(e);
        }

        return buildRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.log(e);
                if (listener != null)
                    listener.onFinish(false, null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (key != null && !"".equals(key)) {
                        FileWriter fw = new FileWriter(cacheFile);
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        fw.write(jsonObject.getString(key));
                        fw.close();
                    } else {
                        InputStream is = response.body().byteStream();
                        BufferedInputStream bis = new BufferedInputStream(is);
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(cacheFile));

                        int len = 0;
                        byte[] b = new byte[2048];
                        while ((len = bis.read(b)) > 0) {
                            bos.write(b, 0, len);
                        }

                        bos.flush();
                        bos.close();
                        bis.close();
                        is.close();
                    }

                    if (listener != null)
                        listener.onFinish(true, cacheFile.getAbsolutePath());
                } catch (Exception e) {
                    Logger.log(e);
                }
            }
        });

    }

    public static Call cache(String url, int startPos, @Nullable final OnCacheFinishListener listener){
        File cacheDir = MyApplication.getContext().getCacheDir();

        String s = url.substring(
                (url.startsWith("http")) ? url.indexOf("/", 8) : url.indexOf("/")
                , url.length());
        if (startPos < 10) s += "00" + startPos;
        else if (startPos < 100) s += "0" + startPos;
        else s+=""+startPos;

        char[] chars = s.toLowerCase().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '/')
                chars[i] = '_';
            if (chars[i] == '?')
                chars[i] = '.';
            if (chars[i] == '&')
                chars[i] = '_';
        }


        final File cacheFile = new File(cacheDir.getAbsolutePath() + "/" + new String(chars));

        if (cacheFile.exists()) {
            if (listener != null)
                listener.onFinish(true, cacheFile.getAbsolutePath());
        } else try {
            cacheFile.createNewFile();
        } catch (IOException e) {
            Logger.log(e);
        }

        // 构建缓存文件结束
        List<String> keys = new ArrayList<>();
        keys.add("startPos");
        List<String> values = new ArrayList<>();
        values.add(startPos + "");

        return buildRequest(url, keys, values, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.log(e);
                if (listener != null)
                    listener.onFinish(false, null);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    FileWriter fw = new FileWriter(cacheFile);
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    fw.write(jsonObject.getString("耿dalao看着办"));
                    fw.close();
                    if (listener != null)
                        listener.onFinish(true, cacheFile.getAbsolutePath());
                } catch (Exception e) {
                    Logger.log(e);
                }
            }
        });
    }

    public interface OnCacheFinishListener {
        void onFinish(boolean success, String cachePath);
    }

}
