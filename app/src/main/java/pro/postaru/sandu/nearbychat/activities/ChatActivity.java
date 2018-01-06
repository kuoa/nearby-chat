package pro.postaru.sandu.nearbychat.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import pro.postaru.sandu.nearbychat.R;
import pro.postaru.sandu.nearbychat.adapters.ChatAdapter;
import pro.postaru.sandu.nearbychat.constants.Constant;
import pro.postaru.sandu.nearbychat.models.Message;
import pro.postaru.sandu.nearbychat.models.UserProfile;
import pro.postaru.sandu.nearbychat.utils.DatabaseUtils;


public class ChatActivity extends AppCompatActivity {

    public static final String PARTNER_USER_PROFILE = "PARTNER_USER_PROFILE";

    private static final int CAMERA = 1;
    private static final int GALLERY = 2;

    private static final String[] WRITE_EXTERNAL_PERMISSION = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};

    private String conversationId;

    private List<Message> messages;

    private ChatAdapter chatAdapter;

    private EditText messageEditView;
    private ImageButton messageSendButton;
    private final TextWatcher editMessageTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if (s.length() == 0) {
                messageSendButton.setEnabled(false);
            } else {
                messageSendButton.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    private String imagePath;
    private String imageUrl;
    private Uri imageUri;
    private Bitmap resizedImage;
    private ImageButton messageAtachImageButton;
    private ListView messageListView;
    private ProgressBar progressBar;

    private final ChildEventListener messageListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            Message message = dataSnapshot.getValue(Message.class);

            if (message != null) {
                chatAdapter.add(message);
                messageListView.setSelection(messages.size() - 1);
            } else {
                Log.w(Constant.NEARBY_CHAT, "No messages");
            }

            if (progressBar.getVisibility() == View.VISIBLE) {
                hideProgressBar();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(Constant.NEARBY_CHAT, "loadPost:onCancelled", databaseError.toException());
        }
    };

    private UserProfile conversationPartner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // spinner

        progressBar = (ProgressBar) findViewById(R.id.chat_spinner);

        conversationPartner = (UserProfile) getIntent().getSerializableExtra(PARTNER_USER_PROFILE);

        messageEditView = (EditText) findViewById(R.id.message_edit);
        messageEditView.addTextChangedListener(editMessageTextWatcher);

        messageSendButton = (ImageButton) findViewById(R.id.message_send);
        messageSendButton.setEnabled(false);
        messageSendButton.setOnClickListener(v -> sendMessage());

        messageAtachImageButton = (ImageButton) findViewById(R.id.message_attach_image);
        messageAtachImageButton.setOnClickListener(v -> showImageAttachementDialog());

        messages = new ArrayList<>();

        conversationId = getConversationId(conversationPartner.getId());

        DatabaseUtils.getMessagesByConversationId(conversationId)
                .addChildEventListener(messageListener);

        chatAdapter = new ChatAdapter(this, messages);
        messageListView = (ListView) findViewById(R.id.message_list);
        messageListView.setVisibility(View.GONE);

        messageListView.setAdapter(chatAdapter);

        // set conversation title
        setTitle(conversationPartner.getUserName());

        // hide keyboard by default
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }


    private void sendMessage() {
        Message newMessage = new Message();

        // text message
        if (imageUrl == null) {

            String textContent = messageEditView.getText().toString();
            newMessage.setType(Message.Type.TEXT);
            newMessage.setContent(textContent);

            messageEditView.setText("");
        }
        // image message
        else {

            newMessage.setType(Message.Type.IMAGE);
            newMessage.setContent(imageUrl);

            imageUrl = null;
        }

        newMessage.setDate(new Date());
        newMessage.setSenderId(DatabaseUtils.getCurrentUUID());

        String id = DatabaseUtils.getMessagesByConversationId(conversationId)
                .push()
                .getKey();

        newMessage.setId(id);

        DatabaseUtils.getMessagesByConversationId(conversationId)
                .child(id)
                .setValue(newMessage);
    }

    private void sendImage(Bitmap image) {

        StorageReference storageReference = DatabaseUtils.getStorageDatabase().getReference(imagePath);
        DatabaseUtils.savePictureOnline(image, storageReference, taskSnapshot -> {
            Log.w(Constant.NEARBY_CHAT, "Image uploaded, now sending message");
            // send a image message
            storageReference.getDownloadUrl().addOnSuccessListener(e -> {
                imageUrl = e.toString();
                sendMessage();
            });

        }, e -> {
            Log.w(Constant.NEARBY_CHAT, e.getMessage());
        });
    }

    private void showImageAttachementDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery",
                "Capture photo from camera"};
        pictureDialog.setItems(pictureDialogItems,
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            choosePhotoFromGallery();
                            break;
                        case 1:
                            takePhotoFromCamera();
                            break;
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallery() {

        boolean isAndroidVersionNew = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
        if (isAndroidVersionNew) {
            if (!hasWritePermission()) {
                ActivityCompat.requestPermissions(this, WRITE_EXTERNAL_PERMISSION, 1);
            }
        }

        if (!isAndroidVersionNew || hasWritePermission()) {

            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(galleryIntent, GALLERY);
        }
    }

    private void takePhotoFromCamera() {

        boolean isAndroidVersionNew = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
        if (isAndroidVersionNew) {
            if (!hasCameraPermission()) {
                ActivityCompat.requestPermissions(this, new String[]{CAMERA_PERMISSION[0], WRITE_EXTERNAL_PERMISSION[0]}, 1);
            }
        }

        if (!isAndroidVersionNew || hasCameraPermission()) {
            Intent takePhotoIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);


            imageUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".my.package.name.provider", createImageFile());

            takePhotoIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePhotoIntent, CAMERA);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }


        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    imagePath = saveImage(image);

                    sendImage(resizedImage);

                    Toast.makeText(ChatActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ChatActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {

            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imagePath = saveImage(image);
                Toast.makeText(ChatActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();

                sendImage(resizedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }

    private boolean hasWritePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasCameraPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case GALLERY: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    choosePhotoFromGallery();

                } else {
                    Toast.makeText(this, "GALLERY DENIED", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhotoFromCamera();

                } else {
                    Toast.makeText(this, "CAMERA DENIED", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private File createImageFile() {
        File path = Environment.getExternalStoragePublicDirectory(
                "NearbyChat");
        File file = new File(path, DatabaseUtils.getCurrentUUID() + "-" + Calendar.getInstance()
                .getTimeInMillis() + ".jpg");

        if (!path.exists()) {
            path.mkdirs();
        }
        try {
            file.createNewFile();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private ByteArrayOutputStream compressImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes);

        return bytes;
    }

    private Bitmap resizeImage(Bitmap myBitmap) {

        int width = myBitmap.getWidth();
        int height = myBitmap.getHeight();

        float max = Math.max(width, height);
        float ratio;

        if (max < 1024) {
            ratio = 1;
        } else {
            ratio = (1024 / max) - 0.05f;
            Log.w(Constant.NEARBY_CHAT, ratio + "");
        }

        Log.w(Constant.NEARBY_CHAT, width * ratio + "");
        Log.w(Constant.NEARBY_CHAT, height * ratio + "");

        return Bitmap.createScaledBitmap(myBitmap, (int) (width * ratio),
                (int) (height * ratio), true);
    }

    public String saveImage(Bitmap myBitmap) {

        File file = createImageFile();
        resizedImage = resizeImage(myBitmap);
        ByteArrayOutputStream bytes = compressImage(resizedImage);

        try (FileOutputStream fo = new FileOutputStream(file)) {
            fo.write(bytes.toByteArray());

            MediaScannerConnection.scanFile(this,
                    new String[]{file.getPath()},
                    new String[]{"image/jpeg"}, null);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("TAG", "File Saved::--->" + file.getAbsolutePath());

        return file.getAbsolutePath();
    }


    private String getConversationId(String partnerId) {
        String myId = DatabaseUtils.getCurrentUUID();

        if (myId.compareTo(partnerId) < 0) {
            return myId + "-" + partnerId;
        } else {
            return partnerId + "-" + myId;
        }
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);

        messageListView.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseUtils.getMessagesByConversationId(conversationId).removeEventListener(messageListener);
    }
}
