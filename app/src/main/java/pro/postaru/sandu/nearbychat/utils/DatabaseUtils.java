package pro.postaru.sandu.nearbychat.utils;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

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

    public static DatabaseReference getUserProfileReferenceById(String userId) {
        return getCurrentDatabaseReference().child(Database.userProfiles).child(userId);
    }

    public static DatabaseReference getConversationsReferenceById(String userId) {
        return getCurrentDatabaseReference().child(Database.userConversations).child(userId).child("conversations");
    }

    public static DatabaseReference getMessagesByConversationId(String conversationId) {
        return getCurrentDatabaseReference().child(Database.userMessages).child(conversationId).child("messages");
    }

    /**
     * Save the specified image profile of the current user in the dedicated storage and call the listener when it's done
     *
     * @param bitmap            profileImage
     * @param onSuccessListener onSuccessListener
     * @param onFailureListener onFailureListener
     */
    public static void saveProfilePicture(Bitmap bitmap, OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener, OnFailureListener onFailureListener) {
        StorageReference reference = DatabaseUtils.getCurrentProfileStorageReference();
        savePictureOnline(bitmap, reference, onSuccessListener, onFailureListener);
    }

    /**
     * Store the provided image in the specified storage reference and call the listener when it's done
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

    /**
     * Load the profile image of the specified user from the dedicated storage and call the listener when it's done
     *
     * @param userId            user id
     * @param onSuccessListener onSuccessListener
     * @param onFailureListener onFailureListener
     */
    public static void loadProfileImage(String userId, OnSuccessListener<Bitmap> onSuccessListener, OnFailureListener onFailureListener) {
        loadImage(getProfileStorageReferenceForId(userId), onSuccessListener, onFailureListener);
    }

    /**
     * Load the image from the specified storage reference and call the listener when it's done
     *
     * @param storageReference  storageReference
     * @param onSuccessListener onSuccessListener
     * @param onFailureListener onFailureListener
     */
    public static void loadImage(StorageReference storageReference, OnSuccessListener<Bitmap> onSuccessListener, OnFailureListener onFailureListener) {
        Bitmap bitmapFromMemCache = CacheUtils.getBitmapFromMemCache(storageReference.getPath());
        if (bitmapFromMemCache != null) {
            //cache work
            Log.d(Constant.CACHE_UTILS, "loadImage: ok");
            onSuccessListener.onSuccess(bitmapFromMemCache);
        } else {
            try {

                final long ONE_MEGABYTE = 1024 * 1024;

                Task<byte[]> task = storageReference.getBytes(ONE_MEGABYTE);//async storage exception when the image doesn't exist

                if (onFailureListener != null) {
                    task.addOnFailureListener(onFailureListener);
                }
                //debug listeners
                task.addOnSuccessListener(bytes -> {
                    Log.d(Constant.NEARBY_CHAT, "loadImage() called with: storagReference = [" + storageReference + "], onSuccessListener = [" + onSuccessListener + "], onFailureListener = [" + onFailureListener + "]");
                    Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    if (image != null) {
                        CacheUtils.addBitmapToMemoryCache(storageReference.getPath(), image);
                        if (onSuccessListener != null) {
                            onSuccessListener.onSuccess(image);
                        }
                    }

                    Log.d(Constant.CACHE_UTILS, "storeImage:ok ");

                }).addOnFailureListener(exception -> {
                    // Handle any errors
                    Log.d(Constant.NEARBY_CHAT, "loadImage() called with: storagReference = [" + storageReference + "], onSuccessListener = [" + onSuccessListener + "], onFailureListener = [" + onFailureListener + "]");
                    Log.w(Constant.NEARBY_CHAT, "loadProfileImage: ", exception);
                });
            } catch (RuntimeException e) {
                Log.w(Constant.NEARBY_CHAT, "loadImage: ", e);
            }
        }
    }

    /**
     * Store the provided audio record online and call the listener when it's done
     * The listeners are optional
     *
     * @param audioPath         the audio path
     * @param reference         the storage reference for the image
     * @param onSuccessListener listener when the upload is ok
     * @param onFailureListener listener when the upload is ko
     */
    public static void saveRecordOnline(String audioPath, StorageReference reference, OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener, OnFailureListener onFailureListener) {

        Uri audioUri = Uri.fromFile(new File(audioPath));
        UploadTask uploadTask = reference.putFile(audioUri);

        if (onSuccessListener != null) {
            uploadTask.addOnSuccessListener(onSuccessListener);
        }
        if (onFailureListener != null) {
            uploadTask.addOnFailureListener(onFailureListener);
        }

        //debug listeners
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Log.w(Constant.NEARBY_CHAT, "save audio online: ko ", exception);
        }).addOnSuccessListener(taskSnapshot -> {
            Log.d(Constant.NEARBY_CHAT, "save audio: ok");
            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
            Uri downloadUrl = taskSnapshot.getDownloadUrl();
            Log.d(Constant.NEARBY_CHAT, "save audio online : uri= " + downloadUrl);
        });
    }

    /**
     * and call the listener when it's done
     *
     * @param storageReference  storageReference
     * @param identifier        record identifier
     * @param onSuccessListener onSuccessListener
     * @param onFailureListener onFailureListener
     * @param activity          the activity
     */
    public static void loadRecord(StorageReference storageReference, String identifier, OnSuccessListener<FileInputStream> onSuccessListener, OnFailureListener onFailureListener, Activity activity) {
        //Retrieve the record from the cache
        FileInputStream fileInputStreamFromCache = CacheUtils.getRecordFromMemCache(identifier, activity);
        if (fileInputStreamFromCache != null) {
            //cache work
            Log.d(Constant.CACHE_UTILS, "loadRecord: ok identifier = [" + identifier + "]+activity = [" + activity + "]");
            onSuccessListener.onSuccess(fileInputStreamFromCache);
        } else {
            try {
                final long ONE_MEGABYTE = 1024 * 1024;

                Task<byte[]> task = storageReference.getBytes(ONE_MEGABYTE); //async storage exception when the record doesn't exist

                if (onSuccessListener != null) {
                    task.addOnSuccessListener(bytes -> {
                        try {
                            //we decode the bytes and store it in the internal temp directory
                            File file = SoundUtils.decodeByteArray(identifier, bytes, activity);
                            if (file != null) {
                                //we save the path of the newly created file in the cache
                                Log.d(Constant.CACHE_UTILS, "SaveRecord: ok identifier = [" + identifier + "]+activity = [" + activity + "]");
                                CacheUtils.addRecordToMemoryCache(identifier, file.getAbsolutePath());
                                onSuccessListener.onSuccess(new FileInputStream(file));
                            }

                        } catch (FileNotFoundException e) {
                            Log.e(Constant.NEARBY_CHAT, "loadRecord: fail ", e);
                        }
                    });
                }
                if (onFailureListener != null) {
                    task.addOnFailureListener(onFailureListener);
                }
                //debug listeners
                task.addOnSuccessListener(bytes -> Log.d(Constant.NEARBY_CHAT, "loadRecord() called with: storagReference = [" + storageReference + "], onSuccessListener = [" + onSuccessListener + "], onFailureListener = [" + onFailureListener + "]"));
                task.addOnFailureListener(exception -> {
                    // Handle any errors
                    Log.d(Constant.NEARBY_CHAT, "loadRecord() called with: storagReference = [" + storageReference + "], onSuccessListener = [" + onSuccessListener + "], onFailureListener = [" + onFailureListener + "]");
                    Log.w(Constant.NEARBY_CHAT, "loadRecord: ", exception);
                });
            } catch (RuntimeException e) {
                Log.w(Constant.NEARBY_CHAT, "loadRecord: ", e);
            }
        }
    }


}
