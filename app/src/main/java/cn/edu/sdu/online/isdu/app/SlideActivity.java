package cn.edu.sdu.online.isdu.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;

import cn.edu.sdu.online.isdu.R;
import cn.edu.sdu.online.isdu.ui.design.snake.Snake;
import cn.edu.sdu.online.isdu.ui.design.snake.annotations.EnableDragToClose;

@EnableDragToClose()
public class SlideActivity extends NormActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        SlidrConfig config = new SlidrConfig.Builder()
//                .scrimColor(0x000000)
//                .scrimStartAlpha(0.8f)
//                .edge(true)
//                .edgeSize(0.1f)
//                .velocityThreshold(2400)
//                .distanceThreshold(0.1f)
//                .sensitivity(1.0f)
//                .build();
//        Slidr.attach(this, config);
        Snake.host(this);
        overridePendingTransition(R.anim.snake_slide_in_right, R.anim.snake_slide_out_left);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.snake_slide_in_left, R.anim.snake_slide_out_right);
    }

}
