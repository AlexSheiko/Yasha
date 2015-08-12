package com.yasha.yasha;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class PostActivity extends AppCompatActivity {

    private ToggleButton toggleF;
    private ToggleButton toggleC;
    private ToggleButton toggleW;
    private ToggleButton toggleT;

    private String category = "F";

    private CompoundButton.OnCheckedChangeListener categoryListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        toggleF = (ToggleButton) findViewById(R.id.toggle_forgiveness);
        toggleC = (ToggleButton) findViewById(R.id.toggle_confession);
        toggleW = (ToggleButton) findViewById(R.id.toggle_witness);
        toggleT = (ToggleButton) findViewById(R.id.toggle_testimony);

        categoryListener =
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!isChecked) {
                            buttonView.setOnCheckedChangeListener(null);
                            buttonView.setChecked(false);
                            buttonView.setOnCheckedChangeListener(categoryListener);
                            return;
                        }


                        category = buttonView.getText().toString();

                        if (category.equals("F")) {
                            toggleF.setChecked(true);
                        } else {
                            toggleF.setOnCheckedChangeListener(null);
                            toggleF.setChecked(false);
                            toggleF.setOnCheckedChangeListener(categoryListener);
                        }
                        if (category.equals("C")) {
                            toggleC.setChecked(true);
                        } else {
                            toggleC.setOnCheckedChangeListener(null);
                            toggleC.setChecked(false);
                            toggleC.setOnCheckedChangeListener(categoryListener);
                        }
                        if (category.equals("W")) {
                            toggleW.setChecked(true);
                        } else {
                            toggleW.setOnCheckedChangeListener(null);
                            toggleW.setChecked(false);
                            toggleW.setOnCheckedChangeListener(categoryListener);
                        }
                        if (category.equals("T")) {
                            toggleT.setChecked(true);
                        } else {
                            toggleT.setOnCheckedChangeListener(null);
                            toggleT.setChecked(false);
                            toggleT.setOnCheckedChangeListener(categoryListener);
                        }
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
        final ParseObject post = new ParseObject("Post");

        post.put("category", category);
        post.put("author", user);
        post.put("message", message);

        user.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject user, ParseException e) {
                if (e == null) {
                    post.put("city", user.getString("city"));

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
        });
    }
}
