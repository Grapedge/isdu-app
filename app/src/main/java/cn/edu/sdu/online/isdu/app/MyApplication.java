package cn.edu.sdu.online.isdu.app;

import android.app.Application;
import android.content.Context;


import org.litepal.LitePal;

import java.lang.ref.WeakReference;

import cn.edu.sdu.online.isdu.ui.design.snake.Snake;
import cn.edu.sdu.online.isdu.util.Settings;
import cn.edu.sdu.online.isdu.util.download.Download;

public class MyApplication extends Application {

    private static WeakReference<Context> contextWeakReference;

    @Override
    public void onCreate() {
        super.onCreate();
        contextWeakReference = new WeakReference<>(getApplicationContext());
        LitePal.initialize(getContext());
        Snake.init(this);

        Settings.load(getContext());
    }

    public static Context getContext() {
        return contextWeakReference.get();
    }
}
