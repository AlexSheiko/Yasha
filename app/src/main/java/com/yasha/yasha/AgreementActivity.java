package com.yasha.yasha;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AgreementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);

        String category = getIntent().getStringExtra("category");
        getSupportActionBar().setTitle(category);


        InputStream is = null;

        if (category.equals("Terms")) {
            is = getResources().openRawResource(R.raw.terms);
        } else if (category.equals("Privacy")) {
            is = getResources().openRawResource(R.raw.privacy);
        }

        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        StringBuilder total = new StringBuilder();
        String line;

        try {
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        TextView agreementView = (TextView) findViewById(R.id.agreement_textview);
        agreementView.setText(Html.fromHtml(total.toString()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
