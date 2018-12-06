package cn.edu.sdu.online.isdu.ui.design.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import cn.edu.sdu.online.isdu.R;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/6/5
 *
 * 提示对话框，拥有是否两个选项
 ****************************************************
 */

public class AlertDialog extends AbstractDialog {

    private Context context;
    private TextView txtTitle;
    private TextView txtMessage;
    private TextView btnPositive;
    private TextView btnNegative;
    private View blankView;

    private String title;
    private String message;
    private String pos;
    private String neg;

    private View separateLine;

    private boolean touchOnOutside = true;

    private View.OnClickListener onPositiveButtonClickListener;
    private View.OnClickListener onNegativeButtonClickListener;

    public AlertDialog(@NonNull Context context) {
        super(context, R.style.DialogTheme);
        this.context = context;
    }

    public AlertDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.design_dialog_alert);

        initView();
        setCancelOnTouchOutside(true);
    }

    private void initView() {
        txtTitle = findViewById(R.id.txt_title);
        txtMessage = findViewById(R.id.txt_msg);
        btnPositive = findViewById(R.id.btn_positive);
        btnNegative = findViewById(R.id.btn_negative);
        blankView = findViewById(R.id.blank_view);
        separateLine = findViewById(R.id.separate_line);

        txtTitle.setText(title);
        txtMessage.setText(message);
        btnPositive.setText(pos);
        btnPositive.setOnClickListener(onPositiveButtonClickListener);
        btnNegative.setText(neg);
        btnNegative.setOnClickListener(onNegativeButtonClickListener);

        if (neg == null) {
            separateLine.setVisibility(View.GONE);
            btnNegative.setVisibility(View.GONE);
        }

        if (touchOnOutside) {
            blankView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        } else {
            blankView.setOnClickListener(null);
        }

    }

    public void setCancelOnTouchOutside(boolean b) {
        touchOnOutside = b;
    }

    public void setTitle(String title) {
        this.title = title;
        if (txtTitle != null) {
            txtTitle.setText(title);
        }
    }

    public void setMessage(String message) {
        this.message = message;
        if (txtMessage != null) {
            txtMessage.setText(message);
        }
    }

    public void setPositiveButton(String text, View.OnClickListener listener) {
        pos = text;
        onPositiveButtonClickListener = listener;

        if (btnPositive != null) {
            btnPositive.setText(pos);
            btnPositive.setOnClickListener(onPositiveButtonClickListener);
        }
    }

    public void setNegativeButton(String text, View.OnClickListener listener) {
        neg = text;
        onNegativeButtonClickListener = listener;

        if (btnPositive != null) {
            btnNegative.setText(neg);
            btnNegative.setOnClickListener(onNegativeButtonClickListener);
        }
    }

}
