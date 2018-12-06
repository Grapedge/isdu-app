package cn.edu.sdu.online.isdu.interfaces;

public interface Collectible {

    /**
     * 进行收藏
     */
    void onCollect();


    /**
     * 进行浏览
     * 可以写入浏览记录
     */
    void onScan();

}
