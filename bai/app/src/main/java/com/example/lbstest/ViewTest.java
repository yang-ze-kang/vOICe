package com.example.lbstest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import com.example.lbstest.view.CircleImageView;
public class ViewTest extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.yang);
        CircleImageView circleImageView =(CircleImageView) findViewById(R.id.circle_image_view);
        circleImageView.playAnim();
    }
}
