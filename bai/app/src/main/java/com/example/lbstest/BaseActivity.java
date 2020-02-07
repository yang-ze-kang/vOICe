package com.example.lbstest;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import com.example.lbstest.util.rx.RxTimerUtil;
public class BaseActivity extends Activity {
    @Override
    protected void onDestroy() {
        RxTimerUtil.cancel();
        super.onDestroy();
    }
}
