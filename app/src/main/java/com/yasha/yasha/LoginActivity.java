package com.yasha.yasha;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onClickLogin(View view) {
        EditText usernameField = (EditText) findViewById(R.id.username_field);
        EditText passwordField = (EditText) findViewById(R.id.password_field);

        final String username = usernameField.getText().toString().trim();
        final String password = passwordField.getText().toString().trim();

        boolean hasEmptyFields = false;

        if (password.isEmpty()) {
            passwordField.setError("Password cannot be empty");
            passwordField.requestFocus();
            hasEmptyFields = true;
        }
        if (username.isEmpty()) {
            usernameField.setError("Username is required");
            usernameField.requestFocus();
            hasEmptyFields = true;
        }
        if (hasEmptyFields) return;
        findViewById(R.id.loading).setVisibility(View.VISIBLE);

        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                findViewById(R.id.loading).setVisibility(View.GONE);

                if (user != null) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {

                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.whereEqualTo("email", username);
                    query.getFirstInBackground(new GetCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (e == null) {

                                ParseUser.logInInBackground(user.getUsername(), password, new LogInCallback() {
                                    @Override
                                    public void done(ParseUser parseUser, ParseException e) {
                                        if (parseUser != null) {
                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                            } else {
                                String errorMessage = e.getMessage();
                                if (e.getMessage().contains(": ")) {
                                    errorMessage = errorMessage.split(": ")[1];
                                } else if (e.getMessage().equals("i/o failure")) {
                                    errorMessage = "Network lost. Check your connection and try again";
                                } else if (e.getMessage().equals("invalid session token")
                                        || e.getMessage().equals("no results found for query")) {
                                    errorMessage = "User not exists";
                                }
                                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }

    public void onForgotPasswordClick(View view) {
        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void startActivity(Intent intent) {
        if (!intent.hasExtra("category")) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
        }
        super.startActivity(intent);
    }

    public void onClickTerms(View view) {
        Intent intent = new Intent(this, AgreementActivity.class);
        intent.putExtra("category", "Terms");
        startActivity(intent);
    }

    public void onClickPolicy(View view) {
        Intent intent = new Intent(this, AgreementActivity.class);
        intent.putExtra("category", "Privacy");
        startActivity(intent);
    }
}
