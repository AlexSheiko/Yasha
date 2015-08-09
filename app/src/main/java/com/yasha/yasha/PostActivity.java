package com.yasha.yasha;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class PostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        final ToggleButton toggleF = (ToggleButton) findViewById(R.id.toggle_forgiveness);
        final ToggleButton toggleC = (ToggleButton) findViewById(R.id.toggle_confession);
        final ToggleButton toggleW = (ToggleButton) findViewById(R.id.toggle_witness);
        final ToggleButton toggleT = (ToggleButton) findViewById(R.id.toggle_testimony);

        CompoundButton.OnCheckedChangeListener categoryListener =
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleView, boolean isChecked) {
                toggleF.setChecked(false);
                toggleC.setChecked(false);
                toggleW.setChecked(false);
                toggleT.setChecked(false);

                toggleView.setChecked(true);
            }
        };

        toggleF.setOnCheckedChangeListener(categoryListener);
        toggleC.setOnCheckedChangeListener(categoryListener);
        toggleW.setOnCheckedChangeListener(categoryListener);
        toggleT.setOnCheckedChangeListener(categoryListener);
    }

    public void onClickHistory(View view) {
        startActivity(new Intent(this, HistoryActivity.class));
    }

    public void onClickPost(View view) {
        EditText messageField = (EditText) findViewById(R.id.message_field);
        String message = messageField.getText().toString().trim();

        if (message.isEmpty()) {
            messageField.setError("Enter your message");
            return;
        }

        ParseUser user = ParseUser.getCurrentUser();
        ParseObject post = new ParseObject("Post");

        post.put("category", "T");
        post.put("author", user);
        post.put("message", message);

        try {
            user.fetch();
            post.put("city", user.getString("city"));
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    finish();
                } else {
                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
