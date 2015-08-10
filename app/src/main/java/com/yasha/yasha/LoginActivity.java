package com.yasha.yasha;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
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

        String username = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

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

        // TODO: Allow login with email also
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                } else {
                    String errorMessage = e.getMessage();
                    if (e.getMessage().contains(": ")) {
                        errorMessage = errorMessage.split(": ")[1];
                    } else if (e.getMessage().equals("i/o failure")) {
                        errorMessage = "Network lost. Check your connection and try again";
                    }
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void onForgotPasswordClick(View view) {
        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
    }
}
