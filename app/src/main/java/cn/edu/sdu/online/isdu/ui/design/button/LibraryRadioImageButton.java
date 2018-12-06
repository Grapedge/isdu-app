package cn.edu.sdu.online.isdu.ui.design.button;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.edu.sdu.online.isdu.R;

public class LibraryRadioImageButton extends LinearLayout {

    private ImageView imageView;
    private TextView textView;
    private LinearLayout buttonLayout;
    private boolean selected = false;

    public LibraryRadioImageButton(Context context) {
        super(context);
    }

    public LibraryRadioImageButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.design_library_radio_image_button, this);

        imageView = findViewById(R.id.image_view);
        textView = findViewById(R.id.text_view);
        buttonLayout = findViewById(R.id.button_layout);

        // Get attributes
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.RadioImageButton);

        for (int i = 0; i < arr.getIndexCount(); i++) {
            int ai = arr.getIndex(i);

            switch (ai) {
                case R.styleable.RadioImageButton_radioImageButtonSrc:
                    setImageResource(arr.getResourceId(ai, R.mipmap.ic_launcher));
                    break;
                case R.styleable.RadioImageButton_radioImageButtonText:
                    setText(arr.getText(ai).toString());
                    break;
            }
        }

        // Release TypedArray Resource
        arr.recycle();

        setSelected(false);
    }

    public LibraryRadioImageButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setImageResource(int resId) {
        imageView.setImageResource(resId);
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public String getText() {
        return textView.getText().toString();
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            textView.setTextColor(getResources().getColor(R.color.colorPurpleDark));
            buttonLayout.setBackgroundColor(getResources().getColor(R.color.colorButtonPressed));
        } else {
            textView.setTextColor(getResources().getColor(R.color.colorPrimaryText));
            buttonLayout.setBackgroundColor(getResources().getColor(R.color.colorButtonNormal));
        }
    }

}
