package com.yasha.yasha;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.LogInCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_FROM_GALLERY = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setElevation(0);

        TextView nameView = (TextView) findViewById(R.id.name_textview);
        TextView cityView = (TextView) findViewById(R.id.city_textview);
        final ImageView avatarView = (ImageView) findViewById(R.id.avatar_picker);

        ParseUser user = ParseUser.getCurrentUser();
        nameView.setText(user.getUsername());
        cityView.setText(user.getString("city"));
        cityView.setTypeface(null, Typeface.ITALIC);

        ParseFile avatarFile = user.getParseFile("avatar");
        if (avatarFile != null) {
            avatarFile.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] bytes, ParseException e) {
                    if (e == null) {
                        File tempFile = null;
                        try {
                            tempFile = File.createTempFile("abc", "cba", null);
                            FileOutputStream fos = new FileOutputStream(tempFile);
                            fos.write(bytes);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                        Picasso.with(SettingsActivity.this)
                                .load(tempFile)
                                .transform(new CircleTransform())
                                .into(avatarView);
                    }
                }
            });
        } else {
            Picasso.with(this)
                    .load(R.drawable.avatar_placeholder)
                    .transform(new CircleTransform())
                    .into(avatarView);
        }
    }

    public void onAvatarClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose image from...");
        builder.setSingleChoiceItems(
                new String[]{"Gallery", "Take photo"},
                -1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        switch (position) {
                            case 0:
                                dispatchGalleryPickerIntent();
                                dialog.cancel();
                                break;
                            case 1:
                                dispatchTakePictureIntent();
                                dialog.cancel();
                                break;
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void dispatchTakePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Install camera app to take a picture", Toast.LENGTH_LONG).show();
        }
    }

    private void dispatchGalleryPickerIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_FROM_GALLERY);
        } else {
            Toast.makeText(this, "Install gallery app to choose an image", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ImageView avatarView = (ImageView) findViewById(R.id.avatar_picker);

        Bitmap bitmap = null;
        Drawable drawable;

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                bitmap = (Bitmap) extras.get("data");

            } else if (requestCode == REQUEST_FROM_GALLERY) {
                Uri imageUri = data.getData();
                String imagePath = getPath(imageUri);
                drawable = Drawable.createFromPath(imagePath);

                bitmap = ((BitmapDrawable) drawable).getBitmap();
            }

            Uri imageUri = data.getData();
            Picasso.with(this)
                    .load(imageUri)
                    .transform(new CircleTransform())
                    .into(avatarView);


            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 51, stream);
            byte[] bitmapdata = stream.toByteArray();

            final ParseFile avatarFile = new ParseFile(bitmapdata, "image/png");
            avatarFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        ParseUser user = ParseUser.getCurrentUser();
                        user.put("avatar", avatarFile);
                        user.saveInBackground();
                    }
                }
            });
        }
    }

    public String getPath(Uri uri) {
        if (uri == null) {
            return null;
        }
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }

    public void onInviteClick(View view) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                "Hey check out the app 'Yasha' in the Google Play Store!\n"
                        + "https://play.google.com/store/apps/details?id="
                        + getApplicationContext().getPackageName());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Invite using..."));
    }

    public void onDeleteAccountPressed(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete your account? There's no undo.");

        final EditText passwordField = new EditText(this);
        passwordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
        passwordField.setHint("Your password");
        builder.setView(passwordField, convertToPixels(20), convertToPixels(12), convertToPixels(20), convertToPixels(4));

        builder.setPositiveButton("Delete", null);
        builder.setNegativeButton("Return", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordField.getText().toString().trim();

                if (password.isEmpty()) {
                    passwordField.setError("Password is required");
                    passwordField.requestFocus();
                    return;
                }

                ParseUser.logInInBackground(ParseUser.getCurrentUser().getUsername(), password, new LogInCallback() {
                    @Override
                    public void done(final ParseUser user, ParseException e) {
                        if (e == null) {

                            ParseQuery<ParseObject> postsQuery = ParseQuery.getQuery("Post");
                            postsQuery.whereEqualTo("author", user);
                            postsQuery.findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> posts, ParseException e) {
                                    if (e == null) {
                                        for (ParseObject post : posts) {
                                            post.deleteEventually();
                                        }

                                        ParseQuery<ParseObject> commentQuery = ParseQuery.getQuery("Comment");
                                        commentQuery.whereEqualTo("author", user);
                                        commentQuery.findInBackground(new FindCallback<ParseObject>() {
                                            @Override
                                            public void done(List<ParseObject> comments, ParseException e) {
                                                if (e == null) {
                                                    for (ParseObject comment : comments) {
                                                        comment.deleteEventually();
                                                    }

                                                    user.deleteInBackground(new DeleteCallback() {
                                                        @Override
                                                        public void done(ParseException e) {
                                                            if (e == null) {
                                                                Toast.makeText(SettingsActivity.this, "Account was removed. Perhaps you'd like to create a new one?", Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(SettingsActivity.this, RegisterActivity.class);
                                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                startActivity(intent);
                                                                finish();
                                                            } else {
                                                                Toast.makeText(SettingsActivity.this,
                                                                        "Failed to delete account: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                }
                            });

                        } else {
                            passwordField.setError("Password entered incorrectly");
                            passwordField.selectAll();
                            passwordField.requestFocus();
                        }
                    }
                });
            }
        });
    }

    public void onChangePasswordClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New password");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText oldPasswordField = new EditText(this);
        oldPasswordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
        oldPasswordField.setHint("Old password entry line");

        final EditText newPasswordField = new EditText(this);
        newPasswordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
        newPasswordField.setHint("New password entry line");

        layout.addView(oldPasswordField);
        layout.addView(newPasswordField);

        builder.setView(layout, convertToPixels(20), convertToPixels(12), convertToPixels(20), convertToPixels(4));

        builder.setPositiveButton("Save", null);

        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = oldPasswordField.getText().toString().trim();
                final String newPassword = newPasswordField.getText().toString().trim();

                boolean hasEmptyFields = false;

                if (newPassword.isEmpty()) {
                    newPasswordField.setError("Cannot be empty");
                    newPasswordField.requestFocus();
                    hasEmptyFields = true;
                }
                if (oldPassword.isEmpty()) {
                    oldPasswordField.setError("Cannot be empty");
                    oldPasswordField.requestFocus();
                    hasEmptyFields = true;
                }
                if (hasEmptyFields) return;


                ParseUser.logInInBackground(ParseUser.getCurrentUser().getUsername(), oldPassword, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e == null) {

                            user.setPassword(newPassword);
                            user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        ParseUser.logOutInBackground(new LogOutCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    dialog.cancel();
                                                    startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                                                    Toast.makeText(SettingsActivity.this, "Now you can login using your new password", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(SettingsActivity.this,
                                                e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                        } else {
                            oldPasswordField.setError("Old password entered incorrectly");
                            oldPasswordField.selectAll();
                            oldPasswordField.requestFocus();
                        }
                    }
                });
            }
        });
    }

    private int convertToPixels(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }

    public void onChangeUsernameClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Change “" + ParseUser.getCurrentUser().getUsername() + "” to:");


        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText usernameField = new EditText(this);
        usernameField.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        usernameField.setHint("New username");

        final EditText passwordField = new EditText(this);
        passwordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
        passwordField.setHint("Current password");

        layout.addView(usernameField);
        layout.addView(passwordField);

        builder.setView(layout, convertToPixels(20), convertToPixels(12), convertToPixels(20), convertToPixels(4));

        builder.setPositiveButton("Save", null);

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = usernameField.getText().toString().trim();
                String password = passwordField.getText().toString().trim();

                boolean hasEmptyFields = false;

                if (password.isEmpty()) {
                    passwordField.setError("Cannot be empty");
                    passwordField.requestFocus();
                    hasEmptyFields = true;
                }
                if (username.isEmpty()) {
                    usernameField.setError("Cannot be empty");
                    usernameField.requestFocus();
                    hasEmptyFields = true;
                }
                if (hasEmptyFields) return;


                ParseUser.logInInBackground(ParseUser.getCurrentUser().getUsername(),
                        password, new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if (e == null) {
                                    user.setUsername(username);
                                    user.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                ParseUser.logOutInBackground(new LogOutCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        if (e == null) {
                                                            dialog.cancel();
                                                            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                                                            Toast.makeText(SettingsActivity.this, "Now you can login using your new username", Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            } else {
                                                usernameField.setError("Username already taken");
                                                usernameField.selectAll();
                                                usernameField.requestFocus();
                                            }
                                        }
                                    });
                                } else {
                                    passwordField.setError("Password entered incorrectly");
                                    passwordField.selectAll();
                                    passwordField.requestFocus();
                                }
                            }
                        });
            }
        });
    }

    public void onChangeEmailClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Change “" + ParseUser.getCurrentUser().getEmail() + "” to:");


        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText emailField = new EditText(this);
        emailField.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailField.setHint("New email");

        final EditText passwordField = new EditText(this);
        passwordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
        passwordField.setHint("Current password");

        layout.addView(emailField);
        layout.addView(passwordField);

        builder.setView(layout, convertToPixels(20), convertToPixels(12), convertToPixels(20), convertToPixels(4));

        builder.setPositiveButton("Save", null);

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = emailField.getText().toString();
                String password = passwordField.getText().toString();

                boolean hasEmptyFields = false;

                if (password.isEmpty()) {
                    passwordField.setError("Cannot be empty");
                    passwordField.requestFocus();
                    hasEmptyFields = true;
                }
                if (email.isEmpty()) {
                    emailField.setError("Cannot be empty");
                    emailField.requestFocus();
                    hasEmptyFields = true;
                }
                if (hasEmptyFields) return;


                ParseUser.logInInBackground(ParseUser.getCurrentUser().getUsername(),
                        password, new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if (e == null) {
                                    user.setEmail(email);
                                    user.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                ParseUser.logOutInBackground(new LogOutCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        if (e == null) {
                                                            dialog.cancel();
                                                            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                                                            Toast.makeText(SettingsActivity.this, "Now you can login using your new email", Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            } else {
                                                emailField.setError("Invalid email address");
                                                emailField.selectAll();
                                                emailField.requestFocus();
                                            }
                                        }
                                    });
                                } else {
                                    passwordField.setError("Password entered incorrectly");
                                    passwordField.selectAll();
                                    passwordField.requestFocus();
                                }
                            }
                        });

            }
        });
    }

    public void onLogoutClick(View view) {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    finish();

                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });
    }
}
