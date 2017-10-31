package pro.postaru.sandu.nearbychat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.View;

import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {

    private static int RESULT_LOAD_IMAGE = 1;

    private final Activity activity = this;

    private AutoCompleteTextView userNameView;
    private EditText userBioView;
    private View updateProfileView;
    private Button updateProfileButton;
    private ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userNameView = (AutoCompleteTextView) findViewById(R.id.username);
        userBioView = (EditText) findViewById(R.id.bio);

        updateProfileButton = (Button) findViewById(R.id.update_profile_button);
        updateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileData();
            }
        });

        updateProfileView = findViewById(R.id.profile_form);

        profileImage = (ImageView) findViewById(R.id.profile_image);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1){
                    if(!hasReadPerimission()){
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                    else{
                        pickProfileImage();
                    }
                }
            }
        });

        populateUserProfile();
    }

    /**
     * Fills the image and user information
     */
    private void populateUserProfile() {
        //TODO populate user informations
    }


    private boolean hasReadPerimission() {
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickProfileImage();

                } else {
                    Toast.makeText(activity, R.string.profile_image_permission, Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    /**
     * Sets a new profile picture for the user
     */
    private void pickProfileImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();


            profileImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }
    }

    private void saveProfileData() {

        // Reset errors.
        userNameView.setError(null);
        userBioView.setError(null);

        // Store values at the time of the profile update attempt.
        String userName = userNameView.getText().toString();
        String userBio = userBioView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid bio, if the user entered one.
        if (!TextUtils.isEmpty(userBio) && !isBioValid(userBio)) {
            userBioView.setError(getString(R.string.error_invalid_bio));
            focusView = userBioView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(userName)) {
            userNameView.setError(getString(R.string.error_field_required));
            focusView = userNameView;
            cancel = true;
        } else if (!isUsernameValid(userName)) {
            userNameView.setError(getString(R.string.error_invalid_username));
            focusView = userNameView;
            cancel = true;
        }

        if(cancel){
            focusView.requestFocus();
        }
        else{
            //TODO update information
        }

    }

    private boolean isUsernameValid(String email) {
        return email.length() < 25;
    }

    private boolean isBioValid(String password) {
        return password.length() < 40;
    }

}

