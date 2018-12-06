package cn.edu.sdu.online.isdu.app;

import android.os.Bundle;
import android.support.annotation.Nullable;

import cn.edu.sdu.online.isdu.R;

public class AlphaActivity extends NormActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.alpha_0_100, R.anim.alpha_100_0);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.alpha_0_100, R.anim.alpha_100_0);
    }

}
