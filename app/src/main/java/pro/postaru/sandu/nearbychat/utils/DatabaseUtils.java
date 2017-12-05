package pro.postaru.sandu.nearbychat.utils;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import pro.postaru.sandu.nearbychat.constants.Constant;
import pro.postaru.sandu.nearbychat.constants.Database;
import pro.postaru.sandu.nearbychat.interfaces.OnBitmapLoadedListener;

import static pro.postaru.sandu.nearbychat.constants.Constant.FIREBASE_STORAGE_REFERENCE;
import static pro.postaru.sandu.nearbychat.constants.Database.userLocation;

public class DatabaseUtils {


    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static DatabaseReference getCurrentDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference();

    }

    public static GeoFire getNewLocationDatabase() {
        return new GeoFire(getCurrentDatabaseReference().child(userLocation));
    }

    /**
     * @return null if there is no User
     */
    public static String getCurrentUUID() {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        }
        return null;
    }

    public static StorageReference getStorageReference() {

        return getStorageReferenceForId(getCurrentUUID());
    }

    public static StorageReference getStorageReferenceForId(String id) {

        return getStorageDatabase().getReference("profile/" + id + ".jpeg");
    }

    public static FirebaseStorage getStorageDatabase() {
        return FirebaseStorage.getInstance(FIREBASE_STORAGE_REFERENCE);
    }

    public static void savePicture(Bitmap bitmap) {
        StorageReference reference = DatabaseUtils.getStorageReference();

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

    public static void loadProfileImage(String id, OnBitmapLoadedListener onBitmapLoadedListener) {
        final long ONE_MEGABYTE = 1024 * 1024;
        StorageReference storageReferenceForId = getStorageReferenceForId(id);
        Task<byte[]> task = storageReferenceForId.getBytes(ONE_MEGABYTE);
        task.addOnSuccessListener(bytes -> {
            Log.d(Constant.NEARBY_CHAT, "loadProfileImage() called with: id = [" + id + "], onBitmapLoadedListener = [" + onBitmapLoadedListener + "]");

            // Data for "profile" is returns, use this as needed
            Bitmap avatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            onBitmapLoadedListener.OnBitmapLoaded(avatar);

        }).addOnFailureListener(exception -> {
            // Handle any errors
            Log.d(Constant.NEARBY_CHAT, "loadProfileImage() called with: id = [" + id + "], onBitmapLoadedListener = [" + onBitmapLoadedListener + "]");
            Log.w(Constant.NEARBY_CHAT, "loadProfileImage: ", exception);
        });
    }

    public static DatabaseReference getUserProfileReferenceById(String userId) {
        return getCurrentDatabaseReference().child(Database.userProfiles).child(userId);
    }

    public static DatabaseReference getConversationsReferenceById(String userId) {
        return getCurrentDatabaseReference().child(Database.userConversations).child(userId).child("conversations");
    }

    public static DatabaseReference getMessagesByConversationId(String conversationId) {
        return getCurrentDatabaseReference().child(Database.userMessages).child(conversationId).child("messages");
    }

}
