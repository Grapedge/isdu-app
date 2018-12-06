package cn.edu.sdu.online.isdu.util.download;

import java.io.File;
import java.text.DecimalFormat;

import cn.edu.sdu.online.isdu.util.Logger;

public class FileLengthDetector implements Runnable {

    private String filePath; // 文件路径
    private long lastLength; // 上一次探测的长度
    private long speed = 0; // 传输速度

    private static int sleepTime = 1; // 每1s探测一次

    private boolean stop = false; // 终止标志符

    public FileLengthDetector(String filePath) {
        this.filePath = filePath;
        File file = new File(filePath);
        if (file.exists()) {
            lastLength = file.length();
        }
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                Thread.sleep(sleepTime * 1000);
                long len = new File(filePath).length();
                speed = (len - lastLength) / sleepTime;

                lastLength = len;
            } catch (Exception e) {
                Logger.log(e);
            }
        }
    }

    public String getSpeed() {
        if (speed < 1024) {
            // 速度小于1024字节（1KB/s）
            return speed + " B/s";
        } else if (speed < 1024 * 1024) {
            // 速度小于1024千字节(1MB/s)
            double spd = ((double) speed) / (1024.0);
            return new DecimalFormat(".00").format(spd) + " KB/s";
        } else {
            double spd = ((double) speed) / (1024.0 * 1024.0);
            return new DecimalFormat(".00").format(spd) + " MB/s";
        }
    }

    public void stop() {
        stop = true;
    }
}
