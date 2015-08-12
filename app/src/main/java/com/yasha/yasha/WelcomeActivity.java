package com.yasha.yasha;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Typeface headerTypeface = Typeface.createFromAsset(getAssets(), "fonts/Oxygen-Bold.otf");
        TextView titleView1 = (TextView)findViewById(R.id.title_textview_1);
        TextView titleView2 = (TextView)findViewById(R.id.title_textview_2);
        titleView1.setTypeface(headerTypeface);
        titleView2.setTypeface(headerTypeface);
    }

    public void onClickRegister(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void onClickLogin(View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }
}
