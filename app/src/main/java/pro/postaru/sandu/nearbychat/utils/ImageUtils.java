package pro.postaru.sandu.nearbychat.utils;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class ImageUtils {

    public static File createImageFile() {
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

    public static ByteArrayOutputStream compressImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes);

        return bytes;
    }

    public static Bitmap resizeImage(Bitmap myBitmap) {

        int width = myBitmap.getWidth();
        int height = myBitmap.getHeight();

        float max = Math.max(width, height);
        float ratio;

        if (max < 1024) {
            ratio = 1;
        } else {
            ratio = (1024 / max) - 0.05f;
        }

        return Bitmap.createScaledBitmap(myBitmap, (int) (width * ratio),
                (int) (height * ratio), true);
    }

}
