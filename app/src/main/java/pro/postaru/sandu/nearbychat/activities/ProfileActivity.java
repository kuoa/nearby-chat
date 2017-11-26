package pro.postaru.sandu.nearbychat.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import pro.postaru.sandu.nearbychat.R;
import pro.postaru.sandu.nearbychat.constants.Constant;
import pro.postaru.sandu.nearbychat.constants.Database;
import pro.postaru.sandu.nearbychat.models.UserProfile;
import pro.postaru.sandu.nearbychat.utils.DataValidator;

public class ProfileActivity extends AppCompatActivity {

    public static final String USER_INFO_PREFS = "pro.postaru.sandu.nearbychat.USER_INFO_PREFS";

    public static final String USER_NAME_KEY = "pro.postaru.sandu.nearbychat.USER_NAME";
    public static final String USER_BIO_KEY = "pro.postaru.sandu.nearbychat.BIO";
    public static final String USER_AVATAR_KEY = "pro.postaru.sandu.nearbychat.AVATAR";

    private static final int RESULT_LOAD_IMAGE = 1;

    private static final String[] READ_STORAGE_PERMISSION =
            {Manifest.permission.READ_EXTERNAL_STORAGE};

    private final Activity activity = this;
    private AutoCompleteTextView userNameView;
    private EditText userBioView;
    private Button updateProfileButton;
    private ImageView profileImage;
    private SharedPreferences profile;
    private String picturePath = "";

    private DatabaseReference database;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private UserProfile userProfile;

    private final ValueEventListener userProfileListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(Constant.NEARBY_CHAT, "onDataChange: dataSnapshot = [" + dataSnapshot + "]");
            UserProfile userProfileLocal = dataSnapshot.getValue(UserProfile.class);

            if (userProfileLocal != null) {
                userProfile = userProfileLocal;
                Log.w(Constant.NEARBY_CHAT, "Online profile loaded for id " + ProfileActivity.this.userProfile.getId());
                saveProfileOffline();
                initProfileView();
            } else {
                Log.w(Constant.NEARBY_CHAT, "Error while loading the online profile");
            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(Constant.NEARBY_CHAT, "Error database");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        // hide keyboard by default
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();
        userProfile = new UserProfile();
        profile = getSharedPreferences(ProfileActivity.USER_INFO_PREFS, 0);

        userNameView = (AutoCompleteTextView) findViewById(R.id.username);
        userBioView = (EditText) findViewById(R.id.bio);

        updateProfileButton = (Button) findViewById(R.id.update_profile_button);
        updateProfileButton.setOnClickListener(v -> {
            saveProfileData();
            Toast.makeText(activity, getString(R.string.profile_updated_text), Toast.LENGTH_LONG).show();
        });

        profileImage = (ImageView) findViewById(R.id.profile_image);
        profileImage.setOnClickListener(v -> {

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (!hasReadPermission()) {
                    ActivityCompat.requestPermissions(activity, READ_STORAGE_PERMISSION
                            , 1);
                } else {
                    pickProfileImage();
                }
            }
        });


        loadProfileData();
    }


    private boolean hasReadPermission() {
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
    private void pickProfileImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = null;
            if (selectedImage != null) {
                cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
            }
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                picturePath = cursor.getString(columnIndex);
                cursor.close();
            }

            profileImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }
    }

    /**
     * Loads the user profile and fills the image and user information
     */
    private void loadProfileData() {


        if (isNetworkAvailable()) {
            //load online information
            loadProfileOnline();
        } else {
            loadProfileOffline();

            initProfileView();
        }


    }


    private void initProfileView() {
        userNameView.setText(userProfile.getUserName());
        userBioView.setText(userProfile.getBio());

        if (!picturePath.isEmpty()) {
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

        View errorView = null;

        // Check for a valid bio, if the user entered one.
        if (!TextUtils.isEmpty(userBio) && !DataValidator.isBioValid(userBio)) {
            userBioView.setError(getString(R.string.error_invalid_bio));
            errorView = userBioView;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(userName)) {
            userNameView.setError(getString(R.string.error_field_required));
            errorView = userNameView;
        } else if (!DataValidator.isUsernameValid(userName)) {
            userNameView.setError(getString(R.string.error_invalid_username));
            errorView = userNameView;
        }

        if (errorView != null) {
            errorView.requestFocus();
        } else {
            userProfile.setId(firebaseUser.getUid());
            userProfile.setUserName(userName);
            userProfile.setBio(userBio);
            //for the moment we don't store the bitmap
            //userProfile.setAvatar(profileImage.getDrawingCache());

            if (isNetworkAvailable()) {
                saveProfileOnline();
            }
            saveProfileOffline();
        }

    }

    /**
     * Save on the device the current userProfile of the current firebaseUser online
     */
    private void saveProfileOffline() {
        Log.d(Constant.NEARBY_CHAT, "Save profile offline id " + firebaseUser.getUid());
        SharedPreferences.Editor editor = profile.edit();
        editor.putString(ProfileActivity.USER_NAME_KEY, userProfile.getUserName());
        editor.putString(ProfileActivity.USER_BIO_KEY, userProfile.getBio());
        editor.putString(ProfileActivity.USER_AVATAR_KEY, picturePath);

        editor.apply();
    }

    /**
     * Save the current userProfile of the current firebaseUser online
     */
    private void saveProfileOnline() {
        Log.d(Constant.NEARBY_CHAT, "Save profile online for id  " + firebaseUser.getUid());
        database.child(Database.userProfiles).child(firebaseUser.getUid()).setValue(userProfile);
    }

    /**
     * Load userProfile for the current firebaseUser with the online version
     * Async task
     */
    private void loadProfileOnline() {
        Log.d(Constant.NEARBY_CHAT, "Load profile offline and add listener for id " + firebaseUser.getUid());
        database.child(Database.userProfiles).child(firebaseUser.getUid()).addListenerForSingleValueEvent(userProfileListener);
    }

    private void loadProfileOffline() {
        //load offline information
        userProfile.setUserName(profile.getString(ProfileActivity.USER_NAME_KEY, "User name (default)"));
        userProfile.setBio(profile.getString(ProfileActivity.USER_BIO_KEY, "User bio (default)"));
        picturePath = profile.getString(ProfileActivity.USER_AVATAR_KEY, "");
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null;
    }
}

