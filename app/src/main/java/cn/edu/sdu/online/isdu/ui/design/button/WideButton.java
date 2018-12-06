package cn.edu.sdu.online.isdu.ui.design.button;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.edu.sdu.online.isdu.R;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/6/15
 *
 * 长按钮
 ****************************************************
 */

public class WideButton extends LinearLayout {

    private TextView txtName;
    private TextView txtComment;
    private ImageView imgIndicator;
    private ImageView imgArrow;
    private SwitchCompat switchCompat;
    private String itemId;
    private boolean canSwitch = false;
    private boolean showArrow = true;

    private LinearLayout btnLayout;

    private OnItemClickListener onItemClickListener;
    private OnItemSwitchListener onItemSwitchListener;

    public WideButton(Context context) {
        super(context);
    }

    public WideButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.design_wide_button, this);

        txtName = findViewById(R.id.txt_item_name);
        txtComment = findViewById(R.id.txt_comment);
        imgIndicator = findViewById(R.id.ic_indicator);
        imgArrow = findViewById(R.id.img_arrow);
        switchCompat = findViewById(R.id.sw);
        btnLayout = findViewById(R.id.btn_layout);

        setTxtComment("");
        setImgIndicator(null);

        // Get attributes
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.WideButton);

        for (int i = 0; i < arr.getIndexCount(); i++) {
            int ai = arr.getIndex(i);

            switch (ai) {
                case R.styleable.WideButton_wideButtonSrc:
                    setImgIndicator(arr.getResourceId(ai, 0), context);
                    break;
                case R.styleable.WideButton_wideButtonCanSwitch:
                    setCanSwitch(arr.getBoolean(ai, false));
                    break;
                case R.styleable.WideButton_wideButtonItemName:
                    setTxtName(arr.getString(ai));
                    break;
                case R.styleable.WideButton_wideButtonComment:
                    setTxtComment(arr.getString(ai));
                    break;
                case R.styleable.WideButton_wideButtonItemId:
                    this.itemId = arr.getString(ai);
                    break;
                case R.styleable.WideButton_wideButtonShowArrow:
                    setShowArrow(arr.getBoolean(ai, true));
                    break;
            }
        }

        // Release TypedArray Resource
        arr.recycle();

        initSwitch();
        initBtn();
    }

    public WideButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initSwitch() {
        if (canSwitch) {
            switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (onItemSwitchListener != null) {
                        onItemSwitchListener.onSwitch(itemId, isChecked);
                    }
                }
            });
        } else {
            switchCompat.setOnCheckedChangeListener(null);
        }
    }

    private void initBtn() {
        btnLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canSwitch) {
                    boolean b = switchCompat.isChecked();
                    switchCompat.setChecked(!b);
                } else {
                    if (onItemClickListener != null) {
                        onItemClickListener.onClick(itemId);
                    }
                }
                Log.d("WideButton", "onClick\t" + itemId);
            }
        });
    }

    public void setTxtName(String txt) {
        txtName.setText(txt);
    }

    public void setTxtComment(String txt) {
        if (txt == null || txt.equals("")) {
            txtComment.setVisibility(View.GONE);
            txtComment.setText("");
        } else {
            txtComment.setVisibility(View.VISIBLE);
            txtComment.setText(txt);
        }
    }


    public void setImgIndicator(int resId, Context context) {
        if (resId != 0) {
            imgIndicator.setVisibility(View.VISIBLE);
            imgIndicator.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), resId));
        } else {
            imgIndicator.setVisibility(View.GONE);
            imgIndicator.setImageBitmap(null);
        }
    }

    public void setImgIndicator(Bitmap bmp) {
        if (bmp != null) {
            imgIndicator.setVisibility(View.VISIBLE);
            imgIndicator.setImageBitmap(bmp);
        } else {
            imgIndicator.setVisibility(View.GONE);
            imgIndicator.setImageBitmap(null);
        }
    }

    public void setCanSwitch(boolean canSwitch) {
        this.canSwitch = canSwitch;
        if (canSwitch) {
            imgArrow.setVisibility(View.GONE);
            switchCompat.setVisibility(View.VISIBLE);
        } else {
            switchCompat.setVisibility(View.GONE);
            if (showArrow) imgArrow.setVisibility(View.VISIBLE);
        }
    }

    public void setSwitch(boolean isSwitch) {
        if (canSwitch)
            switchCompat.setChecked(isSwitch);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        initBtn();
    }

    public void setOnItemSwitchListener(OnItemSwitchListener onItemSwitchListener) {
        this.onItemSwitchListener = onItemSwitchListener;
        initSwitch();
    }

    public void setShowArrow(boolean showArrow) {
        this.showArrow = showArrow;
        if (showArrow) {
            imgArrow.setVisibility(View.VISIBLE);
        } else {
            imgArrow.setVisibility(View.GONE);
        }
    }

    public interface OnItemClickListener {
        void onClick(String itemId);
    }

    public interface OnItemSwitchListener {
        void onSwitch(String itemId, boolean b);
    }
}
