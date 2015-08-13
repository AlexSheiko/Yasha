package com.yasha.yasha;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_FROM_GALLERY = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TextView nameView = (TextView) findViewById(R.id.name_textview);
        TextView cityView = (TextView) findViewById(R.id.city_textview);
        final ImageView avatarView = (ImageView) findViewById(R.id.avatar_picker);

        ParseUser user = ParseUser.getCurrentUser();
        nameView.setText(user.getUsername());
        cityView.setText(user.getString("city"));

        ParseFile avatarFile = user.getParseFile("avatar");
        if (avatarFile != null) {
            avatarFile.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] bytes, ParseException e) {
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
                            .noFade()
                            .into(avatarView);
                }
            });
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

//                drawable = new BitmapDrawable(getResources(), bitmap);
//                avatarView.setBackground(drawable);

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
                    .noFade()
                    .into(avatarView);

//            avatarView.setBackground(roundedDrawable);
//            avatarView.setImageBitmap(null);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
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
}
