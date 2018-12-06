package cn.edu.sdu.online.isdu.ui.design.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.WindowManager;

public abstract class AbstractDialog extends Dialog implements DialogInterface {
    public AbstractDialog(@NonNull Context context) {
        super(context);
    }

    public AbstractDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected AbstractDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

}
