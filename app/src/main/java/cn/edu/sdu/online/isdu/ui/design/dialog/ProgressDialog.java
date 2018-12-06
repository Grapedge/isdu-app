package cn.edu.sdu.online.isdu.ui.design.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.edu.sdu.online.isdu.R;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/12
 *
 * 进度条对话框
 ****************************************************
 */

public class ProgressDialog extends AbstractDialog {

    private int progress = 0;
    private String message;
    private Context context;

    private ProgressBar progressBar;
    private TextView btn;
    private TextView txtMsg;
    private View blankView;
    private View separateLine;

    private String btnMsg;
    private View.OnClickListener onClickListener;

    private boolean isProgress;

    public ProgressDialog(Context context, boolean isProgress) {
        super(context, R.style.DialogTheme);
        this.context = context;
        this.isProgress = isProgress;
    }

    public ProgressDialog(@NonNull Context context) {
        super(context);
    }

    public ProgressDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.design_progress_dialog);

        txtMsg = findViewById(R.id.txt_message);
        progressBar = findViewById(R.id.progress_bar);
        btn = findViewById(R.id.btn_negative);
        blankView = findViewById(R.id.blank_view);
        separateLine = findViewById(R.id.separate_line);

        setButton(btnMsg, onClickListener);
        setMessage(message);

        setCancelable(false);
    }


    public void setButton(String text, View.OnClickListener onClickListener) {
        btnMsg = text;
        this.onClickListener = onClickListener;

        if (btnMsg == null) {
            if (btn != null) {
                btn.setVisibility(View.GONE);
                separateLine.setVisibility(View.GONE);
            }
            return;
        }

        if (btn != null) {
            btn.setText(btnMsg);
            btn.setOnClickListener(onClickListener);
        }
    }

    public void setMessage(String text) {
        this.message = text;

        if (txtMsg != null) txtMsg.setText(message);
    }

}
