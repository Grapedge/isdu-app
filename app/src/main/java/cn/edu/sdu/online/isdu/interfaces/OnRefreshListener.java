package cn.edu.sdu.online.isdu.interfaces;

/**
 * Created by jshaz on 2018/5/25.
 */

public interface OnRefreshListener {
    int RESULT_SUCCESS = 0;
    int RESULT_FAIL = 1;
    void onRefresh(int result, Object data);
}
