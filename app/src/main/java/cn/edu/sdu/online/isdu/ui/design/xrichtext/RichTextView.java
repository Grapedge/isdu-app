package cn.edu.sdu.online.isdu.ui.design.xrichtext;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
//import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.edu.sdu.online.isdu.GlideApp;
import cn.edu.sdu.online.isdu.R;
import cn.edu.sdu.online.isdu.app.MyApplication;
import cn.edu.sdu.online.isdu.net.NetworkAccess;
import cn.edu.sdu.online.isdu.util.ImageManager;
import cn.edu.sdu.online.isdu.util.Logger;

/**
 * Created by sendtion on 2016/6/24.
 * 显示富文本
 */
public class RichTextView extends ScrollView {
    private Activity activity;
    private static final int EDIT_PADDING = 10; // edittext常规padding是10dp
    //private static final int EDIT_FIRST_PADDING_TOP = 10; // 第一个EditText的paddingTop值

    private int viewTagIndex = 1; // 新生的view都会打一个tag，对每个view来说，这个tag是唯一的。
    private LinearLayout allLayout; // 这个是所有子view的容器，scrollView内部的唯一一个ViewGroup
    private LayoutInflater inflater;
    private TextView lastFocusText; // 最近被聚焦的TextView
    private LayoutTransition mTransitioner; // 只在图片View添加或remove时，触发transition动画
    private int editNormalPadding = 0; //
    private int disappearingImageIndex = 0;
    //private Bitmap bmp;
    private OnClickListener btnListener;//图片点击事件
    private ArrayList<String> imagePaths;//图片地址集合

    private OnRtImageClickListener onRtImageClickListener;

    /** 自定义属性 **/
    //插入的图片显示高度
    private int rtImageHeight = 0; //为0显示原始高度
    //两张相邻图片间距
    private int rtImageBottom = 10;
    //文字相关属性，初始提示信息，文字大小和颜色
    private String rtTextInitHint = "没有内容";
    //getResources().getDimensionPixelSize(R.dimen.text_size_16)
    private int rtTextSize = 16; //相当于16sp
    private int rtTextColor = Color.parseColor("#757575");

    public RichTextView(Context context) {
        this(context, null);
    }

    public RichTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RichTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //获取自定义属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RichTextView);
        rtImageHeight = ta.getInteger(R.styleable.RichTextView_rt_view_image_height, 0);
        rtImageBottom = ta.getInteger(R.styleable.RichTextView_rt_view_image_bottom, 10);
        //rtTextSize = ta.getDimensionPixelSize(R.styleable.RichTextView_rt_view_text_size, getResources().getDimensionPixelSize(R.dimen.text_size_16));
        rtTextSize = ta.getInteger(R.styleable.RichTextView_rt_view_text_size, 16);
        rtTextColor = ta.getColor(R.styleable.RichTextView_rt_view_text_color, Color.parseColor("#757575"));
        rtTextInitHint = ta.getString(R.styleable.RichTextView_rt_view_text_init_hint);

        ta.recycle();

        activity = (Activity) context;
        imagePaths = new ArrayList<>();

        inflater = LayoutInflater.from(context);

        // 1. 初始化allLayout
        allLayout = new LinearLayout(context);
        allLayout.setOrientation(LinearLayout.VERTICAL);
        //allLayout.setBackgroundColor(Color.WHITE);//去掉背景
        //setupLayoutTransitions();//禁止载入动画
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        allLayout.setPadding(50,15,50,15);//设置间距，防止生成图片时文字太靠边
        addView(allLayout, layoutParams);

        btnListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v instanceof DataImageView){
                    DataImageView imageView = (DataImageView) v;
                    //int currentItem = imagePaths.indexOf(imageView.getAbsolutePath());
                    //Toast.makeText(getContext(),"点击图片："+currentItem+"："+imageView.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    // 开放图片点击接口
                    if (onRtImageClickListener != null){
                        onRtImageClickListener.onRtImageClick(imageView.getAbsolutePath());
                    }

                    //点击图片预览
//                    PhotoPreview.builder()
//                            .setPhotos(imagePaths)
//                            .setCurrentItem(currentItem)
//                            .setShowDeleteButton(false)
//                            .start(activity);
                }
            }
        };

        LinearLayout.LayoutParams firstEditParam = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        //editNormalPadding = dip2px(EDIT_PADDING);
        TextView firstText = createTextView(rtTextInitHint, dip2px(context, EDIT_PADDING));
        allLayout.addView(firstText, firstEditParam);
        lastFocusText = firstText;
    }

    private int dip2px(Context context, float dipValue) {
        float m = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * m + 0.5f);
    }

    public interface OnRtImageClickListener{
        void onRtImageClick(String imagePath);
    }

    public void setOnRtImageClickListener(OnRtImageClickListener onRtImageClickListener) {
        this.onRtImageClickListener = onRtImageClickListener;
    }

    /**
     * 清除所有的view
     */
    public void clearAllLayout(){
        allLayout.removeAllViews();
    }

    /**
     * 获得最后一个子view的位置
     * @return
     */
    public int getLastIndex(){
        int lastEditIndex = allLayout.getChildCount();
        return lastEditIndex;
    }

    /**
     * 生成文本输入框
     */
    public TextView createTextView(String hint, int paddingTop) {
        TextView textView = (TextView) inflater.inflate(R.layout.rich_textview, null);
        textView.setTag(viewTagIndex++);
        textView.setPadding(editNormalPadding, paddingTop, editNormalPadding, paddingTop);
        textView.setHint(hint);
        //textView.setTextSize(getResources().getDimensionPixelSize(R.dimen.text_size_16));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, rtTextSize);
        textView.setTextColor(rtTextColor);
        return textView;
    }

    /**
     * 生成图片View
     */
    private RelativeLayout createImageLayout() {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(
                R.layout.edit_imageview, null);
        layout.setTag(viewTagIndex++);
        View closeView = layout.findViewById(R.id.image_close);
        closeView.setVisibility(GONE);
        DataImageView imageView = layout.findViewById(R.id.edit_imageView);
        //imageView.setTag(layout.getTag());
		imageView.setOnClickListener(btnListener);
        return layout;
    }

    /**
     * 在特定位置插入EditText
     *
     * @param index
     *            位置
     * @param editStr
     *            EditText显示的文字
     */
    public void addTextViewAtIndex(final int index, CharSequence editStr) {
        TextView textView = createTextView("", EDIT_PADDING);
        textView.setText(editStr);

        // 请注意此处，EditText添加、或删除不触动Transition动画
        //allLayout.setLayoutTransition(null);
        allLayout.addView(textView, index);
        //allLayout.setLayoutTransition(mTransitioner); // remove之后恢复transition动画
    }

    /**
     * 在特定位置添加ImageView
     */
    public void addImageViewAtIndex(final int index, final String imagePath) {
        imagePaths.add(imagePath);
        final RelativeLayout imageLayout = createImageLayout();
        final DataImageView imageView = imageLayout.findViewById(R.id.edit_imageView);

        final Bitmap zhanweiBmp = BitmapFactory.decodeResource(activity.getResources(),
                R.drawable.img_blank_logo);
        final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                (allLayout.getWidth() - allLayout.getPaddingLeft() - allLayout.getPaddingRight())
                        * zhanweiBmp.getHeight() / zhanweiBmp.getWidth());//固定图片高度，记得设置裁剪剧中
        layoutParams.bottomMargin = rtImageBottom;
        imageView.setLayoutParams(layoutParams);
        imageView.setAbsolutePath(imagePath);

        //如果是网络图片
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            imageView.setImageBitmap(zhanweiBmp);

            NetworkAccess.cache(imagePath, new NetworkAccess.OnCacheFinishListener() {
                @Override
                public void onFinish(boolean success, final String cachePath) {
                    if (success) {
                        if (ImageManager.isGif(new File(cachePath))) {
                            Drawable drawable = pl.droidsonroids.gif.GifDrawable.createFromPath(cachePath);
                            // Load as GIF
                            //调整imageView的高度，根据宽度等比获得高度
                            int imageHeight = 0; //解决连续加载多张图片导致后续图片都跟第一张高度相同的问题
                            if (rtImageHeight > 0) {
                                imageHeight = rtImageHeight;
                            } else {
//                                imageHeight = allLayout.getWidth() * resource.getHeight() / resource.getWidth();
                                // 修正图片宽高
                                try {
                                    while (drawable == null) Thread.sleep(100);
                                    imageHeight = (allLayout.getWidth() - allLayout.getPaddingLeft() - allLayout.getPaddingRight())
                                            * drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth();
                                } catch (Exception e) {
                                    Logger.log(e);
                                }
                            }
                            final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                                    LayoutParams.MATCH_PARENT, imageHeight);//固定图片高度，记得设置裁剪剧中
                            lp.bottomMargin = rtImageBottom;

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    imageView.setLayoutParams(lp);

                                    if (rtImageHeight > 0) {
                                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    } else {
                                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                    }

                                    Glide.with(MyApplication.getContext()).asGif().load(cachePath)
                                            .apply(RequestOptions.placeholderOf(R.drawable.img_blank_image))
                                            .apply(RequestOptions.errorOf(R.drawable.img_load_fail))
                                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                                            .apply(RequestOptions.skipMemoryCacheOf(false))
                                            .into(imageView);

                                    zhanweiBmp.recycle();
                                }
                            });

                        } else {
                            final Bitmap bmp = BitmapFactory.decodeFile(cachePath);
                            // Load as Bitmap
                            if (bmp != null) {
                                //调整imageView的高度，根据宽度等比获得高度
                                int imageHeight ; //解决连续加载多张图片导致后续图片都跟第一张高度相同的问题
                                if (rtImageHeight > 0) {
                                    imageHeight = rtImageHeight;
                                } else {
//                                imageHeight = allLayout.getWidth() * resource.getHeight() / resource.getWidth();
                                    // 修正图片宽高
                                    imageHeight = (allLayout.getWidth() - allLayout.getPaddingLeft() - allLayout.getPaddingRight())
                                            * bmp.getHeight() / bmp.getWidth();
                                }

                                final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                                        LayoutParams.MATCH_PARENT, imageHeight);//固定图片高度，记得设置裁剪剧中
                                lp.bottomMargin = rtImageBottom;

                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        imageView.setLayoutParams(lp);

                                        if (rtImageHeight > 0) {
                                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                        } else {
                                            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                        }
                                        GlideApp.with(MyApplication.getContext()).load(bmp)
                                                .apply(RequestOptions.placeholderOf(R.drawable.img_blank_image))
                                                .apply(RequestOptions.errorOf(R.drawable.img_load_fail))
                                                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                                                .apply(RequestOptions.skipMemoryCacheOf(false))
                                                .thumbnail(0.5f)
                                                .into(imageView);
                                    }
                                });
                            }

//                            imageView.setImageBitmap(bmp);
                        }
                    }
                }
            });
        } else { //如果是本地图片

            // 调整imageView的高度，根据宽度等比获得高度
            Bitmap bmp = BitmapFactory.decodeFile(imagePath);
            int imageHeight ; //解决连续加载多张图片导致后续图片都跟第一张高度相同的问题
            if (rtImageHeight > 0) {
                imageHeight = rtImageHeight;
            } else {
//                imageHeight = allLayout.getWidth() * bmp.getHeight() / bmp.getWidth();
                imageHeight = (allLayout.getWidth() - allLayout.getPaddingLeft() - allLayout.getPaddingRight())
                        * bmp.getHeight() / bmp.getWidth();
            }
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, imageHeight);//固定图片高度，记得设置裁剪剧中
            lp.bottomMargin = rtImageBottom;
            imageView.setLayoutParams(lp);

            if (rtImageHeight > 0){
                GlideApp.with(MyApplication.getContext()).load(imagePath).centerCrop()
                        .placeholder(R.drawable.img_load_fail).error(R.drawable.img_load_fail).into(imageView);
            } else {
                GlideApp.with(MyApplication.getContext()).load(imagePath)
                        .placeholder(R.drawable.img_load_fail).error(R.drawable.img_load_fail).into(imageView);
            }
        }

        // onActivityResult无法触发动画，此处post处理
        allLayout.addView(imageLayout, index);
    }

    /**
     * 根据view的宽度，动态缩放bitmap尺寸
     *
     * @param width
     *            view的宽度
     */
    public Bitmap getScaledBitmap(String filePath, int width) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        int sampleSize = options.outWidth > width ? options.outWidth / width
                + 1 : 1;
        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public void setData(final List<RichTextEditor.EditData> dataList) {
        post(new Runnable() {
            @Override
            public void run() {
                allLayout.removeAllViews();
                for (int i = 0; i < dataList.size(); i++) {
                    RichTextEditor.EditData data = dataList.get(i);
                    if (data.imageName != null &&
                            !data.imageName.equals("")) {
                        addImageViewAtIndex(i, data.imageName);
                    } else {
                        addTextViewAtIndex(i, data.inputStr);
                    }
                }
            }
        });
    }

    /**
     * 初始化transition动画
     */
    private void setupLayoutTransitions() {
        mTransitioner = new LayoutTransition();
        //allLayout.setLayoutTransition(mTransitioner);
        mTransitioner.addTransitionListener(new LayoutTransition.TransitionListener() {

            @Override
            public void startTransition(LayoutTransition transition,
                                        ViewGroup container, View view, int transitionType) {

            }

            @Override
            public void endTransition(LayoutTransition transition,
                                      ViewGroup container, View view, int transitionType) {
                if (!transition.isRunning()
                        && transitionType == LayoutTransition.CHANGE_DISAPPEARING) {
                    // transition动画结束，合并EditText
                    // mergeEditText();
                }
            }
        });
        mTransitioner.setDuration(300);
    }

    public int getRtImageHeight() {
        return rtImageHeight;
    }

    public void setRtImageHeight(int rtImageHeight) {
        this.rtImageHeight = rtImageHeight;
    }

    public int getRtImageBottom() {
        return rtImageBottom;
    }

    public void setRtImageBottom(int rtImageBottom) {
        this.rtImageBottom = rtImageBottom;
    }

    public String getRtTextInitHint() {
        return rtTextInitHint;
    }

    public void setRtTextInitHint(String rtTextInitHint) {
        this.rtTextInitHint = rtTextInitHint;
    }

    public int getRtTextSize() {
        return rtTextSize;
    }

    public void setRtTextSize(int rtTextSize) {
        this.rtTextSize = rtTextSize;
    }

    public int getRtTextColor() {
        return rtTextColor;
    }

    public void setRtTextColor(int rtTextColor) {
        this.rtTextColor = rtTextColor;
    }
}
