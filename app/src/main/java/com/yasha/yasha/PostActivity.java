package com.yasha.yasha;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class PostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
    }

    public void onClickHistory(View view) {
        startActivity(new Intent(this, HistoryActivity.class));
    }

    public void onClickPost(View view) {
        EditText messageField = (EditText) findViewById(R.id.message_field);
        String message = messageField.getText().toString().trim();

        message = "Johnny, forgive me for my misbehaviour the other day. You are a dear friend that I would like to keep close.";

        if (message.isEmpty()) {
            messageField.setError("Enter your message");
            return;
        }

        ParseObject post = new ParseObject("Post");
        post.put("category", "T");
        post.put("author", ParseUser.getCurrentUser());
        post.put("message", message);
        post.put("city", "New York");
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
