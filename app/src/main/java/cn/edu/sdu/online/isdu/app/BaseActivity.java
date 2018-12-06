package cn.edu.sdu.online.isdu.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;

import cn.edu.sdu.online.isdu.R;
import cn.edu.sdu.online.isdu.util.Logger;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

public abstract class BaseActivity extends AppCompatActivity
        implements Thread.UncaughtExceptionHandler {

    protected MyBroadcastReceiver myBroadcastReceiver;

    protected abstract void prepareBroadcastReceiver();

    protected abstract void unRegBroadcastReceiver();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.setStatusBarDarkMode(this);

        Thread.setDefaultUncaughtExceptionHandler(this);
        prepareBroadcastReceiver();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    @Override
    public void finish() {
        super.finish();

    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Logger.log(e);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegBroadcastReceiver();
    }

    protected static class MyBroadcastReceiver extends BroadcastReceiver {

        private Activity activity;

        protected MyBroadcastReceiver(Activity activity) {this.activity = activity;}

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    protected void decorateWindow() {
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                     View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }
}
