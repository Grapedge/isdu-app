package cn.edu.sdu.online.isdu.ui.design.popupwindow;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

public class BasePopupWindow {
    protected Context context;
    protected View contentView;
    protected PopupWindow mInstance;

    public BasePopupWindow(Context context, int layoutRes, int w, int h) {
        this.context = context;
        contentView = LayoutInflater.from(context).inflate(layoutRes, null, false);
        initView();
        initEvent();
        mInstance = new PopupWindow(contentView, w, h, true);
        initWindow();
    }

    public View getContentView() {
        return contentView;
    }

    public PopupWindow getPopupWindow() {
        return mInstance;
    }

    protected void initView() {}
    protected void initEvent() {}

    protected void initWindow() {
        mInstance.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mInstance.setOutsideTouchable(true);
        mInstance.setTouchable(true);
    }

    public void showBashOfAnchor(View anchor, LayoutGravity gravity, int x, int y) {
        int[] offset = gravity.getOffset(anchor, mInstance);
        mInstance.showAsDropDown(anchor, offset[0] + x, offset[1] + y);
    }

    public void showAsDropDown(View anchor, int xoff, int yoff) {
        mInstance.showAsDropDown(anchor, xoff, yoff);
    }

    public void showAtLocation(View parent, int gravity, int x, int y) {
        mInstance.showAtLocation(parent, gravity, x, y);
    }
}
