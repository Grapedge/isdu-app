package cn.edu.sdu.online.isdu.ui.design;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.edu.sdu.online.isdu.bean.Schedule;
import cn.edu.sdu.online.isdu.util.EnvVariables;
import cn.edu.sdu.online.isdu.util.ScheduleTime;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/5/30
 *
 * 可滑动日程表的自定义View
 * 规定每日时间从0:00 ~ 23:59
 ****************************************************
 */

public class ScheduleTable extends View {
    private final int DEF_LEFT_COL_SIZE = 120;
    private final int DEF_TOP_ROW_SIZE = 150;

    private final float MIN_SCALE = 1f;
    private final float MAX_SCALE = 3f;

    private String[] weekDaysInChs = {"一", "二", "三", "四", "五", "六", "日"};
    private String[] weekDaysInEng = {"Mon.", "Tue.", "Wed.", "Thu.", "Fri.", "Sat.", "Sun."};
    private int currentWeekIndex = 1; // 当前周
    private int mWidth; // View占用宽度
    private int mHeight; // View占用高度
    private int backgroundColor = 0xFFF2F2F2; // View背景颜色
    private int invalidColor = 0xFFDCDCDC; // 非当前周的卡片背景颜色
    private int invalidTextColor = 0xFF7D7D7D; // 非当前周的卡片字体颜色
    private int leftColumnSize = DEF_LEFT_COL_SIZE; // 左侧栏的宽度
    private int topRowSize = DEF_TOP_ROW_SIZE; // 顶栏宽度
    private int rectWidth; // 卡片宽度
    private int rectHeight; // 卡片高度
    private Paint mPaint = new Paint();
    private Paint mWordPaint = new Paint();
    private int primaryTextSize = 40; // 一级文字大小
    private int leftColumnTextSize = 35; // 二级文字大小
    private ScheduleTime startTime = new ScheduleTime(8, 0); // 开始时间
    private ScheduleTime endTime = new ScheduleTime(20, 0); // 结束时间
    private int breakTimeHourStart = 0; // 休息时间开始（小时）
    private int breakTimeHourDuration = 0; // 休息持续时间（小时）
    private int[] hourIndex = new int[24]; // 每个小时对应的左侧序号
    private List<List<Schedule>> scheduleList; // 日程表
    private RectF cardRectF = new RectF();
    private int rectMargin = 5; // 卡片外边距
    private BlurMaskFilter cardMaskFilter = new BlurMaskFilter(6, BlurMaskFilter.Blur.SOLID);
    private int cardTextSize = 35; // 卡片主字体大小
    private int cardPadding = 10; // 卡片内边距
    private int cardTextLineSpacing = 10; // 卡片文字间距
    private int realWidth; // 内容布局实际宽度
    private int realHeight; // 内容布局实际高度
    private int totalRows;
    private int minWidth = 0;
    private int minHeight = 0;
    private boolean showTimeLine = true; // 显示时间轴

    private float scale = MIN_SCALE; // 缩放倍数

    private OnItemClickListener onItemClickListener;
    private List<OrdinateSchedule> ordinateSchedules = new ArrayList<>();

    private int downX, downY;
    private int offsetX = 0, offsetY = 0; // 布局的整体XY偏移量
    private int viewOffsetY = 0; // 控件整体下移量
    private int clickDownX = 0, clickDownY = 0;
    private boolean isMoved = false;

    private ScheduleTime currentTime = new ScheduleTime();

    public ScheduleTable(Context context) {
        super(context);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public ScheduleTable(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public ScheduleTable(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

        totalRows = (endTime.getHour() - startTime.getHour() - breakTimeHourDuration + 1);

        rectWidth = (mWidth - leftColumnSize) / 7;
        rectHeight = totalRows != 0 ? (mHeight - topRowSize) / totalRows : mHeight;

        // 获取实际高度和宽度
//        minWidth = (int) (rectWidth * 7 / 5.2);
        minWidth = (int) (rectWidth * scale);
        minHeight = minWidth;

        rectWidth = Math.max(minWidth, rectWidth);
        rectHeight = Math.max(minHeight, rectHeight);

//        rectWidth = (int) (rectWidth * scale);
//        rectHeight = (int) (realHeight * scale);

        realWidth = rectWidth * 7;
        realHeight = totalRows != 0 ? rectHeight * totalRows : mHeight;

    }

    @Override
    protected void onDraw(Canvas canvas) {

        drawBackground(canvas);

        drawScheduleCards(canvas);

        drawLeftColumn(canvas);

        if (showTimeLine) {
            drawTimeIndicator(canvas);
        }

        drawHead(canvas);

        drawRectAtLeftTop(canvas);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getRawX();
                downY = (int) event.getRawY();
                clickDownX = (int) event.getX();
                clickDownY = (int) event.getY();
                isMoved = false;
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                int curX = (int) event.getRawX();
                int curY = (int) event.getRawY();
                int deltaX = downX - curX;
                int deltaY = downY - curY;

                if (Math.abs(event.getX() - clickDownX) > 10 && Math.abs(event.getY() - clickDownY) > 10)
                    isMoved = true;
                else {
                    if (!isMoved) isMoved = false;
                }

                if (realWidth <= offsetX + deltaX + mWidth - leftColumnSize) {
                    offsetX = realWidth - mWidth + leftColumnSize;
                } else if (offsetX + deltaX < 0) {
                    offsetX = 0;
                } else {
                    offsetX += deltaX;
                }

                downX = curX;

                if (realHeight <= offsetY + deltaY + mHeight - topRowSize) {
                    offsetY = realHeight - mHeight + topRowSize;
                } else if (offsetY + deltaY < 0) {
                    offsetY = 0;
                } else {
                    offsetY += deltaY;
                }

                downY = curY;
                invalidate();

                return true;
            case MotionEvent.ACTION_UP:
                if (!isMoved) {
                    int x = (int) event.getX() + offsetX;
                    int y = (int) event.getY() + offsetY;

                    for (int i = ordinateSchedules.size() - 1; i >= 0; i--) {
                        OrdinateSchedule schedule = ordinateSchedules.get(i);
                        if (x >= schedule.getLeft() && x <= schedule.getRight() &&
                                y >= schedule.getTop() && y <= schedule.getBottom()) {
                            if (onItemClickListener != null)
                                onItemClickListener.onClick(schedule);
                            break;
                        }
                    }
                }

                isMoved = false;
                return true;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 绘制背景
     */
    private void drawBackground(Canvas canvas) {
        // 绘制背景
        mPaint.setColor(backgroundColor);
        canvas.drawRect(leftColumnSize, topRowSize, mWidth, mHeight, mPaint);
    }

    private void drawScheduleCards(Canvas canvas) {
        mWordPaint.setAntiAlias(true);
        mWordPaint.setStyle(Paint.Style.FILL);

        // 遍历周一到周日，绘制每个日程
        getHourIndex();
        ordinateSchedules.clear();
        if (scheduleList != null) {
            for (int i = 0; i < 7 && !scheduleList.isEmpty(); i++) {
                if (scheduleList.get(i) == null) continue;

                for (int j = 0; j < scheduleList.get(i).size(); j++) {
                    Schedule schedule = scheduleList.get(i).get(j);

                    OrdinateSchedule ordinateSchedule = new OrdinateSchedule(schedule);

                    // 绘制背景
                    if (schedule.getRepeatWeeks().contains(currentWeekIndex)) {
                        // 当前周
                        mPaint.setColor(schedule.getScheduleColor());
                        mPaint.setMaskFilter(cardMaskFilter);
                    } else {
                        // 非当前周
                        mPaint.setColor(invalidColor);
                        mPaint.setMaskFilter(cardMaskFilter);
                    }

                    ordinateSchedule.setLeft(getRectLeft(i + 1));
                    ordinateSchedule.setTop(getRectTop(schedule.getStartTime()));
                    ordinateSchedule.setRight(getRectRight(i + 1));
                    ordinateSchedule.setBottom(getRectBottom(schedule.getEndTime()));

                    ordinateSchedules.add(ordinateSchedule);

                    cardRectF.set(getRectLeft(i + 1) - offsetX,
                            getRectTop(schedule.getStartTime()) - offsetY,
                            getRectRight(i + 1) - offsetX,
                            getRectBottom(schedule.getEndTime()) - offsetY);

                    canvas.drawRoundRect(cardRectF, 8, 8, mPaint);
                    mPaint.setMaskFilter(null);


                    // 绘制文字
                    int textBottom = getRectTop(schedule.getStartTime()) + 5 + cardTextSize - offsetY;
                    if (schedule.getRepeatWeeks().contains(currentWeekIndex)) {
                        // 当前周
                        mWordPaint.setColor(schedule.getScheduleTextColor());
                    } else {
                        // 非当前周
                        mWordPaint.setColor(invalidTextColor);
                    }
                    mWordPaint.setTextSize(cardTextSize);

                    // 绘制日程名称
                    List<List<String>> strList;
                    if (!schedule.getRepeatWeeks().contains(currentWeekIndex)) {
                        strList = calculateFixedHeight(schedule, schedule.getScheduleName(),
                                "[非本周]",
                                "@" + schedule.getScheduleLocation());
                    } else strList = calculateFixedHeight(schedule, schedule.getScheduleName(),
                            "@" + schedule.getScheduleLocation());

                    List<String> scheduleName = strList.get(0);
                    for (int k = 0; k < scheduleName.size(); k++) {
                        canvas.drawText(scheduleName.get(k), getRectLeft(i + 1) + cardPadding - offsetX,
                                textBottom,
                                mWordPaint);

                        // 非最后一项
                        if (k != scheduleName.size() - 1) {
                            textBottom += (cardTextSize + cardTextLineSpacing);
                        }
                    }

                    if (!schedule.getRepeatWeeks().contains(currentWeekIndex)) {
                        // 非本周
                        mWordPaint.setTextSize(cardTextSize);
                        textBottom += (cardTextSize + cardTextLineSpacing);
                        List<String> notCurWeek = strList.get(1);
                        for (int k = 0; k < notCurWeek.size(); k++) {
                            canvas.drawText(notCurWeek.get(k), getRectLeft(i + 1) + cardPadding - offsetX,
                                    textBottom,
                                    mWordPaint);

                            // 非最后一项
                            if (k != notCurWeek.size() - 1) {
                                textBottom += (cardTextSize + cardTextLineSpacing);
                            }
                        }
                    }

                    // 绘制日程地点
                    mWordPaint.setTextSize(cardTextSize);
                    textBottom += (cardTextSize + cardTextLineSpacing);
//                List<String> scheduleLocation = getScheduleNameInCard("@" + schedule.getScheduleLocation(), mWordPaint);
                    List<String> scheduleLocation = strList.get((schedule.getRepeatWeeks().contains(currentWeekIndex)) ? 1 : 2);
                    for (int k = 0; k < scheduleLocation.size(); k++) {
                        canvas.drawText(scheduleLocation.get(k), getRectLeft(i + 1) + cardPadding - offsetX,
                                textBottom,
                                mWordPaint);

                        // 非最后一项
                        if (k != scheduleLocation.size() - 1) {
                            textBottom += (cardTextSize + cardTextLineSpacing);
                        }
                    }


                }

            }
        }
    }

    private void drawHead(Canvas canvas) {
        // 头部7天
        mPaint.setColor(backgroundColor);
        mWordPaint.setTextSize(primaryTextSize);
        int marginTopBottom = (topRowSize - 2 * primaryTextSize - 20) / 2;
        for (int i = 0; i < 7; i++) {
            mWordPaint.setColor(0xFF808080);
            mWordPaint.setFakeBoldText(false);
            if (currentWeekIndex == EnvVariables.currentWeek && i == EnvVariables.getCurrentDay() - 1) {
                mWordPaint.setColor(0xFF717DEB);
                mWordPaint.setFakeBoldText(true);
            }
            canvas.drawRect(leftColumnSize + rectWidth * i - offsetX, 0, rectWidth + leftColumnSize + rectWidth * (i + 1) - offsetX, topRowSize, mPaint);
            canvas.drawText(weekDaysInEng[i], leftColumnSize + rectWidth * i + getInsideLeftSize(weekDaysInEng[i], rectWidth) - offsetX,
                    marginTopBottom + primaryTextSize, mWordPaint);
            canvas.drawText(weekDaysInChs[i], leftColumnSize + rectWidth * i + getInsideLeftSize(weekDaysInChs[i], rectWidth) - offsetX,
                    topRowSize - marginTopBottom, mWordPaint);
            mWordPaint.setFakeBoldText(false);
        }
    }

    private void drawLeftColumn(Canvas canvas) {
        // 遍历每一行，画出左边框
        int currentTimeHour = startTime.getHour();
        mWordPaint.setColor(0xFF131313);
        mWordPaint.setTextSize(leftColumnTextSize);
        int marginTopBottom;
        marginTopBottom = (rectHeight - 2 * leftColumnTextSize - 15) / 2;
        for (int i = 0; i <= (endTime.getHour() - startTime.getHour() - breakTimeHourDuration); i++) {
            mPaint.setColor(backgroundColor);
            canvas.drawRect(0, topRowSize + rectHeight * i - offsetY, leftColumnSize, topRowSize + rectHeight * (i + 1) - offsetY, mPaint);
            mPaint.setColor(0xFF7D7D7D);
            canvas.drawRect(0,
                    topRowSize + rectHeight * i - offsetY,
                    leftColumnSize,
                    topRowSize + rectHeight * i - offsetY + 1, mPaint);

//            if (currentTimeHour == breakTimeHourStart) currentTimeHour += breakTimeHourDuration;
            canvas.drawText((currentTimeHour + ":00"),
                    getInsideLeftSize((currentTimeHour + ":00"), leftColumnSize),
                    topRowSize + rectHeight * i + 0.5f * marginTopBottom + leftColumnTextSize - offsetY,
                    mWordPaint);
            canvas.drawText((i + 1 + ""),
                    getInsideLeftSize((i + 1 + ""), leftColumnSize),
                    topRowSize + rectHeight * i + rectHeight - 1.5f * marginTopBottom - offsetY,
                    mWordPaint);

            currentTimeHour++;
        }
    }

    private void drawRectAtLeftTop(Canvas canvas) {
        // 左上角小矩形
        mPaint.setColor(backgroundColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, leftColumnSize, topRowSize, mPaint);
    }

    private void drawTimeIndicator(Canvas canvas) {
        // 画当前时间轴
        currentTime.setHour(Integer.parseInt(new SimpleDateFormat("HH").format(System.currentTimeMillis())));
        currentTime.setMinute(Integer.parseInt(new SimpleDateFormat("mm").format(System.currentTimeMillis())));

        if (currentTime.getHour() < startTime.getHour() || currentTime.getHour() > endTime.getHour()) return;

        int lineTop = getRectTop(currentTime);
        mPaint.setColor(0xFFFF0000);
        mPaint.setMaskFilter(cardMaskFilter);
        canvas.drawRect(0, lineTop - 2f - offsetY, mWidth, lineTop + 2f - offsetY, mPaint);
        mPaint.setMaskFilter(null);
        // 画指引三角标
        Path triPath = new Path();
        triPath.moveTo(0, lineTop - 20 - offsetY);
        triPath.lineTo(0, lineTop + 20 - offsetY);
        triPath.lineTo(20, lineTop - offsetY);
        triPath.close();
        canvas.drawPath(triPath, mPaint);
    }

    private int getInsideLeftSize(String words, int rectWidth) {
        float mWidths[] = new float[words.length()];
        mWordPaint.getTextWidths(words, 0, words.length(), mWidths);
        float res = 0;
        for (int i = 0; i < words.length(); i++) res += mWidths[i];
        return (rectWidth - (int) res) / 2;
    }

    private int getRectLeft(int col) {
        return leftColumnSize + (col - 1) * rectWidth + rectMargin;
    }

    private int getRectRight(int col) {
        return leftColumnSize + col * rectWidth - rectMargin;
    }

    private int getRectTop(int row) {
        return topRowSize + (row - 1) * rectHeight;
    }

    private int getRectBottom(int row) {
        return topRowSize + row * rectHeight;
    }

    private int getRectTop(ScheduleTime time) {
        if (time.getHour() < startTime.getHour()) return 0;
        if (time.getHour() > endTime.getHour()) return mHeight;
//        if (time.getHour() >= breakTimeHourStart && time.getHour() < (breakTimeHourStart + breakTimeHourDuration)) {
//            // 起始时间在BreakTime中，不进行绘制
//            return 0;
//        } else {
        float minuteScale = ((float) time.getMinute()) / 60f;
        return topRowSize + hourIndex[time.getHour()] * rectHeight + (int) (minuteScale * rectHeight) + rectMargin;
//        }
    }

    private int getRectBottom(ScheduleTime time) {
        if (time.getHour() < startTime.getHour()) return 0;
        if (time.getHour() > endTime.getHour()) return mHeight - rectMargin;

//        if (time.getHour() >= breakTimeHourStart && time.getHour() < (breakTimeHourStart + breakTimeHourDuration)) {
//            // 结束时间在BreakTime中
//            return topRowSize + (hourIndex[breakTimeHourStart + breakTimeHourDuration] - 1) * rectHeight - rectMargin;
//        } else {
        float minuteScale = ((float) time.getMinute()) / 60f;
        return topRowSize + hourIndex[time.getHour()] * rectHeight + (int) (minuteScale * rectHeight) - rectMargin;
//        }
    }

    private List<String> getScheduleNameInCard(String text, Paint mPaint) {
        List<String> list = new ArrayList<>();
        int s = 0;
        int e = 1;

        while (e <= text.length()) {
            float[] width = new float[e - s];
            float res = 0;
            mPaint.getTextWidths(text.substring(s, e), width);
            for (int i = 0; i < width.length; i++) res += width[i];
            if (res > rectWidth - 2 * rectMargin - 2 * cardPadding) {
                list.add(text.substring(s, e - 1));
                s = e - 1;
            } else {
                e++;
            }
        }
        list.add(text.substring(s, e - 1));

        return list;
    }

    public void setScheduleList(List<List<Schedule>> scheduleList) {
        this.scheduleList = scheduleList;
        invalidate();
    }

    public void setCurrentWeekIndex(int currentWeekIndex) {
        this.currentWeekIndex = currentWeekIndex;
        invalidate();
    }

    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
        invalidate();
    }

    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
        invalidate();
    }

    private void getHourIndex() {
        int currentHour = startTime.getHour();
        for (int i = 0; i <= (endTime.getHour() - startTime.getHour() - breakTimeHourDuration); i++) {
            hourIndex[currentHour] = i;
            currentHour++;
        }
    }

    private List<List<String>> calculateFixedHeight(Schedule schedule, String... texts) {
        List<List<String>> str = new ArrayList<>();

        int cardHeight = getRectBottom(schedule.getEndTime()) - getRectTop(schedule.getStartTime());

        if (cardHeight - 1.5 * cardPadding < primaryTextSize) {
            // 卡片高度不足一行文字
            str.add(new ArrayList<String>());
            str.add(new ArrayList<String>());
            str.add(new ArrayList<String>());
            return str;
        } else if (cardHeight - 1.5 * cardPadding >= cardTextSize &&
                cardHeight - 1.5 * cardPadding < 2 * cardTextSize + cardTextLineSpacing) {
            // 卡片高度一行至两行
            List<String> list = new ArrayList<>();
            List<String> list1 = getScheduleNameInCard(texts[0], mWordPaint);

            if (list1.size() > 1) {
                String s = list1.get(0);
                if (s.length() > 1) {
                    list.add(s.substring(0, s.length() - 1) + "...");
                } else list.add("...");
            } else list.add(list1.get(0));

            str.add(list);
            str.add(new ArrayList<String>());
            str.add(new ArrayList<String>());
        } else {
            // 卡片高度大于2行
            for (int i = 0; i < texts.length; i++) {
                str.add(getScheduleNameInCard(texts[i], mWordPaint));
            }

            int len = 0;
            for (int i = 0; i < str.size(); i++) len += str.get(i).size();
            int delta = len;

            while (len * cardTextSize + (len - 1) * cardTextLineSpacing >
                    cardHeight - 1.5 * cardPadding) {
                len--;
            }

            delta -= len;

            if (delta != 0)
                // 从头部开始减少文字
                if (delta >= str.get(0).size()) {
                    int originSize = str.get(0).size();

                    if (originSize > 1) {
                        List<String> t = str.get(0); // 获取名称排列列表
                        String s = t.get(0);
                        t.clear();
                        if (s.length() > 1) {
                            t.add(s.substring(0, s.length() - 1) + "...");
                        } else t.add("...");
                    }

                    delta -= (originSize - 1);

                    if (str.size() == 2) {
                        for (;delta > 0; delta--) {
                            str.get(1).remove(str.get(1).size() - 1);
                        }

                        if (!str.get(1).isEmpty()) {
                            StringBuilder finalS = new StringBuilder(str.get(1).get(str.get(1).size() - 1));
                            finalS.deleteCharAt(finalS.length() - 1);
                            finalS.append("..");
                            str.get(1).remove(str.get(1).size() - 1);
                            str.get(1).add(finalS.toString());
                        }

                    } else if (str.size() == 3) {
                        for (;delta > 0; delta--) {
                            str.get(2).remove(str.get(2).size() - 1);
                        }

                        if (!str.get(2).isEmpty()) {
                            StringBuilder finalS = new StringBuilder(str.get(2).get(str.get(2).size() - 1));
                            finalS.deleteCharAt(finalS.length() - 1);
                            finalS.append("..");
                            str.get(2).remove(str.get(2).size() - 1);
                            str.get(2).add(finalS.toString());
                        }
                    }
                } else {
                    for (; delta > 0; delta--)
                        str.get(0).remove(str.get(0).size() - 1);

                    String s = str.get(0).get(str.get(0).size() - 1);
                    str.get(0).remove(str.get(0).size() - 1);

                    if (s.length() > 1) {
                        str.get(0).add(s.substring(0, s.length() - 1) + "...");
                    } else str.get(0).add("...");
                }

        }
        return str;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    public interface OnItemClickListener {
        void onClick(Schedule schedule);
    }

    private static class OrdinateSchedule extends Schedule implements Parcelable {
        private int top;
        private int bottom;
        private int left;
        private int right;

        OrdinateSchedule() {
            super();
        }

        OrdinateSchedule(Schedule schedule) {
            setScheduleName(schedule.getScheduleName());
            setScheduleColor(schedule.getScheduleColor());
            setScheduleTextColor(schedule.getScheduleTextColor());
            setScheduleLocation(schedule.getScheduleLocation());
            setStartTime(schedule.getStartTime());
            setEndTime(schedule.getEndTime());
            setRepeatType(schedule.getRepeatType());
            setRepeatWeeks(schedule.getRepeatWeeks());
        }

        OrdinateSchedule(Parcel in) {
            top = in.readInt();
            bottom = in.readInt();
            left = in.readInt();
            right = in.readInt();
        }

        public static final Creator<OrdinateSchedule> CREATOR = new Creator<OrdinateSchedule>() {
            @Override
            public OrdinateSchedule createFromParcel(Parcel in) {
                return new OrdinateSchedule(in);
            }

            @Override
            public OrdinateSchedule[] newArray(int size) {
                return new OrdinateSchedule[size];
            }
        };

        public int getTop() {
            return top;
        }

        public void setTop(int top) {
            this.top = top;
        }

        public int getBottom() {
            return bottom;
        }

        public void setBottom(int bottom) {
            this.bottom = bottom;
        }

        public int getLeft() {
            return left;
        }

        public void setLeft(int left) {
            this.left = left;
        }

        public int getRight() {
            return right;
        }

        public void setRight(int right) {
            this.right = right;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(top);
            dest.writeInt(bottom);
            dest.writeInt(left);
            dest.writeInt(right);
        }
    }

}
