package cn.edu.sdu.online.isdu.ui.design.button;

import android.support.v4.graphics.ColorUtils;

public class Colors {

    /**
     * RGB颜色亮度调整
     *
     * @param baseColor 原本颜色
     *                  RGB表示
     * @param amount 亮度调整量
     *               正数变亮，负数变暗
     *               [0, 100]的整数
     * @return 亮度调整后的颜色 RGB表示
     */
    public static int brightnessAdjust(int baseColor, int amount) {
        float[] outHSL = new float[3];
        ColorUtils.colorToHSL(baseColor, outHSL);

        float ratio = ((float) amount) / 100f;

        if (ratio > 1f) ratio = 1f;
        if (ratio < 0f) ratio = 0f;

        outHSL[2] = ratio;
        return ColorUtils.HSLToColor(outHSL);
    }
}
