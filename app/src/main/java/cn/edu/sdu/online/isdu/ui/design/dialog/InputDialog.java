package cn.edu.sdu.online.isdu.ui.design.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.TimerTask;

import cn.edu.sdu.online.isdu.R;

public class InputDialog extends Dialog {

    private String title;
    private String pos, neg;
    private String hint;

    private View.OnClickListener onPositiveButtonClickListener;
    private View.OnClickListener onNegativeButtonClickListener;

    private TextView txtTitle;
    private TextView btnPositive;
    private TextView btnNegative;
    private EditText editText;

    private String text;


    public InputDialog(@NonNull Context context) {
        super(context, R.style.DialogTheme);
    }

    public InputDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.design_dialog_input);

        txtTitle = findViewById(R.id.txt_title);
        btnPositive = findViewById(R.id.btn_positive);
        btnNegative = findViewById(R.id.btn_negative);
        editText = findViewById(R.id.edit_text);

        setTitle(title);
        setPositiveButton(pos, onPositiveButtonClickListener);
        setNegativeButton(neg, onNegativeButtonClickListener);
        setHint(hint);
        setText(text);



    }

    public void setTitle(String title) {
        this.title = title;
        if (txtTitle != null) txtTitle.setText(title);
    }

    public void setPositiveButton(String text, View.OnClickListener onClickListener) {
        pos = text;
        onPositiveButtonClickListener = onClickListener;
        if (btnPositive != null) {
            btnPositive.setText(pos);
            btnPositive.setOnClickListener(onPositiveButtonClickListener);
        }
    }

    public void setNegativeButton(String text, View.OnClickListener onClickListener) {
        neg = text;
        onNegativeButtonClickListener = onClickListener;
        if (btnNegative != null) {
            btnNegative.setText(neg);
            btnNegative.setOnClickListener(onNegativeButtonClickListener);
        }
    }

    public void setHint(String hint) {
        this.hint = hint;
        if (editText != null) editText.setHint(hint);
    }

    public void setText(String text) {
        this.text = text;
        if (editText != null) editText.setText(text);
    }

    public String getText() {
        return editText.getText().toString();
    }

    private void showKeyboard() {
        if (editText != null) {
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
            editText.requestFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) editText.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(editText, 0);
        }
    }

}
