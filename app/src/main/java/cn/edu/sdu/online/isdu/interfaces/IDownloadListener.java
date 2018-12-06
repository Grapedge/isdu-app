package cn.edu.sdu.online.isdu.interfaces;

public interface IDownloadListener {

    void onProgress(int progress);

    void onSuccess();

    void onFailed();

    void onPaused();

    void onCanceled();

}
