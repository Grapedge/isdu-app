package cn.edu.sdu.online.isdu.util;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

import cn.edu.sdu.online.isdu.app.BuildConfig;

public class Logger {

    public static void log(String message) {
        if (BuildConfig.IS_TEST_VERSION) {
            String date = new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis());
            String time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(System.currentTimeMillis());
            File log =
                    new File(Environment.getExternalStorageDirectory() + "/iSDU/log/" + date + ".log");
            if (!log.exists()) {
                if (!log.getParentFile().exists()) log.getParentFile().mkdirs();
                try {
                    log.createNewFile();
                } catch (IOException e) {
//                e.printStackTrace();
                }
            }

            try {
                FileWriter fileWriter = new FileWriter(log);
                fileWriter.write("\n################################################\n");
                fileWriter.write(time + ":" + message);
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
//            e.printStackTrace();
            }
        }
    }

    public static void log(Throwable e) {
        if (BuildConfig.IS_TEST_VERSION) {
            String date = new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis());
            String time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(System.currentTimeMillis());
            File log =
                    new File(Environment.getExternalStorageDirectory() + "/iSDU/log/" + date + ".log");
            if (!log.exists()) {
                if (!log.getParentFile().exists()) log.getParentFile().mkdirs();
                try {
                    log.createNewFile();
                } catch (IOException e2) {
//                e2.printStackTrace();
                }
            }

            try {
                FileWriter fileWriter = new FileWriter(log, true);
                PrintWriter printWriter = new PrintWriter(fileWriter);
                fileWriter.write("################################################\n");
                fileWriter.write(time + ":");
                e.printStackTrace(printWriter);
                printWriter.flush();
                printWriter.close();
                fileWriter.flush();
                fileWriter.close();
            } catch (Exception e1) {
//            e1.printStackTrace();
            }
        }
    }
}
