package cn.edu.sdu.online.isdu.ui.design.snackbar;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;

public class MSnackBar {

    private Context mContext;
    private int backgroundColor;
    private int textColor;
    private String message;
    private Snackbar snackbar;

    public static final int TYPE_INFORMATION = 0;
    public static final int TYPE_WARNING = 1;
    public static final int TYPE_ERROR = 2;

    private MSnackBar(Context context) {
        mContext = context;
    }

    public static MSnackBar with(Context context) {
        MSnackBar mSnackBar = new MSnackBar(context);
        return mSnackBar;
    }

    public MSnackBar setType(int type) {
        switch (type) {
            default:
            case TYPE_INFORMATION:
                backgroundColor = 0xEE717DEB;
                textColor = 0xFFFFFF;
                break;
            case TYPE_WARNING:
                backgroundColor = 0xEEE83747;
                textColor = 0xFFFFFF;
                break;
            case TYPE_ERROR:
                backgroundColor = 0xEEE83747;
                textColor = 0xFFD200;
                break;
        }
        return this;
    }

    public void build() {
//        snackbar =
    }

}
