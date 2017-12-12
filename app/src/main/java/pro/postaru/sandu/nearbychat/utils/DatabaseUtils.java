package pro.postaru.sandu.nearbychat.utils;


import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

    public static StorageReference getCurrentProfileStorageReference() {

        return getProfileStorageReferenceForId(getCurrentUUID());
    }

    public static StorageReference getProfileStorageReferenceForId(String id) {

        return getStorageDatabase().getReference("profile/" + id + ".jpeg");
    }

    public static FirebaseStorage getStorageDatabase() {
        return FirebaseStorage.getInstance(FIREBASE_STORAGE_REFERENCE);
    }

    public static void saveProfilePicture(Bitmap bitmap, OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener, OnFailureListener onFailureListener) {
        StorageReference reference = DatabaseUtils.getCurrentProfileStorageReference();
        savePictureOnline(bitmap, reference, onSuccessListener, onFailureListener);
    }

    /**
     * Store the provided image online and call the listener when it's done
     * The listeners are optional
     *
     * @param bitmap            the image
     * @param reference         the storage reference for the image
     * @param onSuccessListener listener when the upload is ok
     * @param onFailureListener listener when the upload is ko
     */
    public static void savePictureOnline(Bitmap bitmap, StorageReference reference, OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener, OnFailureListener onFailureListener) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        UploadTask uploadTask = reference.putBytes(data);

        if (onSuccessListener != null) {
            uploadTask.addOnSuccessListener(onSuccessListener);
        }
        if (onFailureListener != null) {
            uploadTask.addOnFailureListener(onFailureListener);
        }

        //debug listeners
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Log.w(Constant.NEARBY_CHAT, "save picture online: ko ", exception);
        }).addOnSuccessListener(taskSnapshot -> {
            Log.d(Constant.NEARBY_CHAT, "save picture: ok");
            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
            Uri downloadUrl = taskSnapshot.getDownloadUrl();
            Log.d(Constant.NEARBY_CHAT, "save picture online : uri= " + downloadUrl);
        });
    }

    public static void loadProfileImage(String id, OnSuccessListener<byte[]> onSuccessListener, OnFailureListener onFailureListener) {
        loadImage(getProfileStorageReferenceForId(id), onSuccessListener, onFailureListener);
    }

    public static void loadImage(StorageReference storageReference, OnSuccessListener<byte[]> onSuccessListener, OnFailureListener onFailureListener) {
        final long ONE_MEGABYTE = 1024 * 1024;

        Task<byte[]> task = storageReference.getBytes(ONE_MEGABYTE);
        //TODO BUG HERE
        if (onSuccessListener != null) {
            task.addOnSuccessListener(onSuccessListener);
        }
        if (onFailureListener != null) {
            task.addOnFailureListener(onFailureListener);

        }
        //debug listeners
        task.addOnSuccessListener(bytes -> {
            Log.d(Constant.NEARBY_CHAT, "loadImage() called with: storagReference = [" + storageReference + "], onSuccessListener = [" + onSuccessListener + "], onFailureListener = [" + onFailureListener + "]");
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Log.d(Constant.NEARBY_CHAT, "loadImage() called with: storagReference = [" + storageReference + "], onSuccessListener = [" + onSuccessListener + "], onFailureListener = [" + onFailureListener + "]");
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
