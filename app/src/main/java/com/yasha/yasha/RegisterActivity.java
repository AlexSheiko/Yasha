package com.yasha.yasha;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.squareup.picasso.Picasso;
import com.yasha.yasha.services.Constants;
import com.yasha.yasha.services.FetchAddressIntentService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RegisterActivity extends AppCompatActivity
        implements ConnectionCallbacks, OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private AddressResultReceiver mResultReceiver;

    private boolean mResolvingError = false;
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final String DIALOG_ERROR = "dialog_error";
    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    private static final String TAG = "RegisterActivity";

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_FROM_GALLERY = 102;
    private ParseFile mAvatarFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

        mResultReceiver = new AddressResultReceiver(new Handler());


        ImageView avatarView = (ImageView) findViewById(R.id.avatar_picker);
        Picasso.with(this)
                .load(R.drawable.avatar_placeholder)
                .transform(new CircleTransform())
                .noFade()
                .into(avatarView);

        EditText passwordField = (EditText) findViewById(R.id.password_field);
        passwordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView editText, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    onClickRegister(editText);
                }
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    public void onClickRegister(View view) {
        EditText usernameField = (EditText) findViewById(R.id.username_field);
        EditText emailField = (EditText) findViewById(R.id.email_field);
        EditText passwordField = (EditText) findViewById(R.id.password_field);

        String username = usernameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        boolean hasEmptyFields = false;

        if (password.isEmpty()) {
            passwordField.setError("Password cannot be empty");
            passwordField.requestFocus();
            hasEmptyFields = true;
        }
        if (email.isEmpty()) {
            emailField.setError("Email is required");
            emailField.requestFocus();
            hasEmptyFields = true;
        }
        if (username.isEmpty()) {
            usernameField.setError("Username is required");
            usernameField.requestFocus();
            hasEmptyFields = true;
        }
        if (hasEmptyFields) return;
        findViewById(R.id.loading).setVisibility(View.VISIBLE);

        ParseUser user = new ParseUser();

        user.setUsername(usernameField.getText().toString());
        user.setEmail(emailField.getText().toString());
        user.setPassword(passwordField.getText().toString());

        if (mAvatarFile != null) {
            user.put("avatar", mAvatarFile);
        }

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                findViewById(R.id.loading).setVisibility(View.GONE);

                if (e == null) {
                    // connect to location services and get user location
                    if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(RegisterActivity.this) == ConnectionResult.SUCCESS) {
                        mGoogleApiClient.connect();
                    } else {
                        Log.w(TAG, "Play services not available");

                        ParseUser user = ParseUser.getCurrentUser();
                        user.put("city", "No city");
                        user.saveEventually();

                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    }
                } else {
                    String errorMessage = e.getMessage();
                    if (e.getMessage().contains(": ")) {
                        errorMessage = errorMessage.split(": ")[1];
                    } else if (e.getMessage().equals("i/o failure")) {
                        errorMessage = "Network lost. Check your connection and try again";
                    }
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (lastLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Log.w(TAG, getString(R.string.no_geocoder_available));
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                return;
            }

            startIntentService(lastLocation);
        } else {
            Log.w(TAG, "Last location is null");

            ParseUser user = ParseUser.getCurrentUser();
            user.put("city", "No city");
            user.saveEventually();

            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        }
    }

    protected void startIntentService(Location location) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((RegisterActivity) getActivity()).onDialogDismissed();
        }
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            String addressOutput = resultData.getString(Constants.RESULT_DATA_KEY);

            if (resultCode == Constants.SUCCESS_RESULT) {
                Log.d(TAG, "Geocoded address: " + addressOutput);

                ParseUser user = ParseUser.getCurrentUser();
                user.put("city", addressOutput);
                user.saveEventually();

                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            }
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
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }

        if (resultCode == RESULT_OK) {
            Uri imageUri = null;

            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imageUri = getImageUri(photo);
            } else if (requestCode == REQUEST_FROM_GALLERY) {
                imageUri = data.getData();
            }

            ImageView avatarView = (ImageView) findViewById(R.id.avatar_picker);

            Picasso.with(this)
                    .load(imageUri)
                    .transform(new CircleTransform())
                    .noFade()
                    .into(avatarView);

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 51, stream);
            byte[] bitmapdata = stream.toByteArray();

            mAvatarFile = new ParseFile(bitmapdata, "image/png");
            mAvatarFile.saveInBackground();
        }
    }

    public Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 51, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
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
