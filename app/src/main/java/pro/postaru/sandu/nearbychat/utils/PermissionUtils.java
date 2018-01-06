package pro.postaru.sandu.nearbychat.utils;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

public class PermissionUtils {

    public static boolean hasWritePermission(Context context) {
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasCameraPermission(Context context) {
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasReadPermission(Context context) {
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasAudioRecordPermission(Context context){
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED;
    }

}