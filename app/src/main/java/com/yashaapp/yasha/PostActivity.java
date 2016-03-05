package com.yashaapp.yasha;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class PostActivity extends AppCompatActivity {

    private ToggleButton toggleC;
    private ToggleButton toggleT;
    private ToggleButton toggleE;
    private ToggleButton toggleO;

    private String category;

    private CompoundButton.OnCheckedChangeListener categoryListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        final ActionBar actionBar = getSupportActionBar();
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        actionBar.setCustomView(getLayoutInflater().inflate(R.layout.actionbar_post, null), params);
        actionBar.setDisplayShowCustomEnabled(true);

        toggleC = (ToggleButton) findViewById(R.id.toggle_confession);
        toggleT = (ToggleButton) findViewById(R.id.toggle_testimony);
        toggleE = (ToggleButton) findViewById(R.id.toggle_encouragement);
        toggleO = (ToggleButton) findViewById(R.id.toggle_other);

        categoryListener =
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!isChecked) {
                            buttonView.setOnCheckedChangeListener(null);
                            buttonView.setChecked(true);
                            buttonView.setOnCheckedChangeListener(categoryListener);
                            return;
                        }

                        category = buttonView.getText().toString();

                        View actionBarView = getSupportActionBar().getCustomView();
                        TextView categoryView = (TextView) actionBarView.findViewById(R.id.current_category_hint);

                        EditText errorView = (EditText) findViewById(R.id.no_category_label);
                        errorView.setError(null);

                        if (category.equals("C")) {
                            toggleC.setChecked(true);
                            categoryView.setText("Confession");
                        } else {
                            toggleC.setOnCheckedChangeListener(null);
                            toggleC.setChecked(false);
                            toggleC.setOnCheckedChangeListener(categoryListener);
                        }
                        if (category.equals("T")) {
                            toggleT.setChecked(true);
                            categoryView.setText("Testimony");
                        } else {
                            toggleT.setOnCheckedChangeListener(null);
                            toggleT.setChecked(false);
                            toggleT.setOnCheckedChangeListener(categoryListener);
                        }
                        if (category.equals("E")) {
                            toggleE.setChecked(true);
                            categoryView.setText("Encouragement");
                        } else {
                            toggleE.setOnCheckedChangeListener(null);
                            toggleE.setChecked(false);
                            toggleE.setOnCheckedChangeListener(categoryListener);
                        }
                        if (category.equals("O")) {
                            toggleO.setChecked(true);
                            categoryView.setText("Other");
                        } else {
                            toggleO.setOnCheckedChangeListener(null);
                            toggleO.setChecked(false);
                            toggleO.setOnCheckedChangeListener(categoryListener);
                        }
                    }
                };

        toggleC.setOnCheckedChangeListener(categoryListener);
        toggleT.setOnCheckedChangeListener(categoryListener);
        toggleE.setOnCheckedChangeListener(categoryListener);
        toggleO.setOnCheckedChangeListener(categoryListener);
    }

    public void onClickHistory(View view) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit().putString("user_id_history", ParseUser.getCurrentUser().getObjectId()).apply();
        startActivity(new Intent(this, HistoryActivity.class));
    }

    public void onClickPost(View view) {
        EditText messageField = (EditText) findViewById(R.id.message_field);
        String message = messageField.getText().toString().trim();

        if (category == null) {
            EditText errorView = (EditText) findViewById(R.id.no_category_label);
            errorView.setError("Please select a tag from above");
            Toast.makeText(this, "Please select a tag from above", Toast.LENGTH_LONG).show();
            return;
        }
        if (message.isEmpty()) {
            messageField.setError("Enter your message");
            return;
        }
        findViewById(R.id.loading).setVisibility(View.VISIBLE);

        ParseUser user = ParseUser.getCurrentUser();
        final ParseObject post = new ParseObject("Post");

        post.put("category", category);
        post.put("author", user);
        post.put("message", message);

        user.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject user, ParseException e) {
                findViewById(R.id.loading).setVisibility(View.GONE);

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
