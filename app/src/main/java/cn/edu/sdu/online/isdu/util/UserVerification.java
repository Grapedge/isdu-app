package cn.edu.sdu.online.isdu.util;

import android.view.ViewGroup;
import android.widget.TextView;

import com.qmuiteam.qmui.layout.QMUIFrameLayout;

import java.util.ArrayList;
import java.util.List;

import cn.edu.sdu.online.isdu.R;
import kotlin.Unit;

public class UserVerification {

    private static final int[] colors = {
            0xFFFF0000
    };

    private static final String[] names = {
            "管理员"
    };

    public static List<Integer> getColor(int value) {
        List<Integer> list = new ArrayList<>();
        if ((value & 0x01) == 0x01) {
            list.add(colors[0]);
        }
        return list;
    }

    public static List<String> getName(int value) {
        List<String> list = new ArrayList<>();
        if ((value & 0x01) == 0x01) {
            list.add(names[0]);
        }
        return list;
    }

    public static void setCards(ViewGroup container, int value) {
        container.removeAllViewsInLayout();

        List<Integer> colors = getColor(value);
        List<String> names = getName(value);

        for (int i = 0; i < colors.size(); i++) {
            QMUIFrameLayout layout = new QMUIFrameLayout(container.getContext());
            layout.setBorderWidth(0);
            layout.setShadowAlpha(0);
            layout.setBackgroundColor(colors.get(i));
            layout.setRadius(20);
            layout.setPadding(10, 0, 10, 0);

            TextView textView = new TextView(container.getContext());
            textView.setTextColor(0xFFFFFFFF);
            textView.setText(names.get(i));
            textView.setTextSize(12);

            layout.addView(textView);

            container.addView(layout);
        }
    }

}
