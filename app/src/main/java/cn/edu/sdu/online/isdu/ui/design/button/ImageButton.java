package cn.edu.sdu.online.isdu.ui.design.button;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.edu.sdu.online.isdu.R;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/5/23
 *
 * 带有文字的图片按钮
 ****************************************************
 */

public class ImageButton extends LinearLayout {

    private ImageView btnImg; // 按钮图片
    private TextView btnTxt; // 按钮文字
    private long stickTimeMillis = 0;

    public ImageButton(Context context) {
        super(context);
    }

    public ImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.design_image_button, this);

        // Find views
        btnImg = findViewById(R.id.design_imgbtn_image);
        btnTxt = findViewById(R.id.design_imgbtn_text);


        // Get attributes
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.ImageButton);

        for (int i = 0; i < arr.getIndexCount(); i++) {
            int ai = arr.getIndex(i);

            switch (ai) {
                case R.styleable.ImageButton_imageButtonSrc:
                    setImageResource(arr.getResourceId(ai, R.mipmap.ic_launcher));
                    break;
                case R.styleable.ImageButton_imageButtonText:
                    setText(arr.getText(ai).toString());
                    break;
            }
        }

        // Release TypedArray Resource
        arr.recycle();
    }

    public ImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public ImageButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 设置图标RES
     *
     * @param resId Resource ID of the image
     */
    public void setImageResource(int resId) {
        btnImg.setImageResource(resId);
    }

    /**
     * 设置按钮文字
     *
     * @param text Text of the button
     */
    public void setText(String text) {
        btnTxt.setText(text);
    }


}
