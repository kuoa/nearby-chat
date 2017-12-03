package pro.postaru.sandu.nearbychat.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import pro.postaru.sandu.nearbychat.R;
import pro.postaru.sandu.nearbychat.constants.Constant;
import pro.postaru.sandu.nearbychat.constants.Database;
import pro.postaru.sandu.nearbychat.models.UserProfile;
import pro.postaru.sandu.nearbychat.utils.DataValidator;
import pro.postaru.sandu.nearbychat.utils.Network;

import static pro.postaru.sandu.nearbychat.constants.Constant.FIREBASE_STORAGE_REFERENCE;

public class ProfileActivity extends AppCompatActivity {


    private static final int RESULT_LOAD_IMAGE = 1;
    private static final String[] READ_STORAGE_PERMISSION =
            {Manifest.permission.READ_EXTERNAL_STORAGE};
    private final Activity activity = this;

    private AutoCompleteTextView userNameView;
    private EditText userBioView;
    private Button updateProfileButton;
    private ImageView profileImage;
    private ProgressBar progressBar;


    private String picturePath = "";
    private FirebaseStorage firebaseStorage;
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


        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance(FIREBASE_STORAGE_REFERENCE);

        userProfile = new UserProfile();

        userNameView = (AutoCompleteTextView) findViewById(R.id.username);
        userNameView.requestFocus();
        userBioView = (EditText) findViewById(R.id.bio);

        progressBar = (ProgressBar) findViewById(R.id.profile_spinner);

        updateProfileButton = (Button) findViewById(R.id.update_profile_button);
        updateProfileButton.setOnClickListener(v -> {
            saveProfileData();
            Toast.makeText(activity, getString(R.string.profile_updated_text), Toast.LENGTH_LONG).show();
        });

        profileImage = (ImageView) findViewById(R.id.profile_image);
        profileImage.setDrawingCacheEnabled(true);
        profileImage.buildDrawingCache();

        profileImage.setOnClickListener(v -> {
            Log.d(Constant.NEARBY_CHAT, "profileImage setOnClickListener: ");
            boolean isAndroidVersionNew = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
            if (isAndroidVersionNew) {
                if (!hasReadPermission()) {
                    ActivityCompat.requestPermissions(activity, READ_STORAGE_PERMISSION
                            , 1);
                }
            }
            if (!isAndroidVersionNew || hasReadPermission()) {
                pickProfileImage();
            }
        });


        loadProfileData();
    }


    private boolean hasReadPermission() {
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
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
        Log.d(Constant.NEARBY_CHAT, "pickProfileImage: ");
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
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Network.isAvailable(connectivityManager)) {
            //load online information
            loadProfileOnline();
        } else {
            initProfileView();
        }

        progressBar.setVisibility(View.GONE);
    }


    private void initProfileView() {
        userNameView.setText(userProfile.getUserName());
        userBioView.setText(userProfile.getBio());
        if (userProfile.getAvatar() != null) {
            profileImage.setImageBitmap(userProfile.getAvatar());
        }
    }

    private void saveProfileData() {

        progressBar.setVisibility(View.VISIBLE);

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
            profileImage.setDrawingCacheEnabled(true);
            profileImage.buildDrawingCache();
            userProfile.setAvatar(profileImage.getDrawingCache());

            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            if (Network.isAvailable(connectivityManager)) {
                saveProfileOnline();
            }
        }

        progressBar.setVisibility(View.GONE);

    }


    /**
     * Save the current userProfile of the current firebaseUser online
     */
    private void saveProfileOnline() {
        Log.d(Constant.NEARBY_CHAT, "Save profile online for id  " + firebaseUser.getUid());
        database.child(Database.userProfiles).child(firebaseUser.getUid()).setValue(userProfile);
        StorageReference reference = getStorageReference();
        profileImage.setDrawingCacheEnabled(true);
        profileImage.buildDrawingCache();

        Bitmap bitmap = profileImage.getDrawingCache();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        UploadTask uploadTask = reference.putBytes(data);
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Log.w(Constant.NEARBY_CHAT, "saveProfileOnline: ", exception);
        }).addOnSuccessListener(taskSnapshot -> {
            Log.d(Constant.NEARBY_CHAT, "saveProfileOnline profile image: Medatada= " + taskSnapshot.getMetadata());

            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
            Uri downloadUrl = taskSnapshot.getDownloadUrl();
            Log.d(Constant.NEARBY_CHAT, "saveProfileOnline profile image: uri= " + downloadUrl);
        });
    }

    /**
     * Load userProfile for the current firebaseUser with the online version
     * Async task
     */
    private void loadProfileOnline() {
        Log.d(Constant.NEARBY_CHAT, "Load profile online and add listener for id " + firebaseUser.getUid());
        database.child(Database.userProfiles).child(firebaseUser.getUid()).addListenerForSingleValueEvent(userProfileListener);
        loadProfileImage();
    }

    private void loadProfileImage() {
        StorageReference reference = getStorageReference();

        final long ONE_MEGABYTE = 1024 * 1024;
        reference.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            // Data for "profile" is returns, use this as needed
            Bitmap avatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            userProfile.setAvatar(avatar);
            profileImage.setImageBitmap(avatar);
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Log.w(Constant.NEARBY_CHAT, "loadProfileOnline: ", exception);
        });
    }

    @NonNull
    private StorageReference getStorageReference() {
        return firebaseStorage.getReference("profile/" + firebaseUser.getUid() + ".jpeg");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.child(Database.userProfiles).child(firebaseUser.getUid()).removeEventListener(userProfileListener);
    }
}

