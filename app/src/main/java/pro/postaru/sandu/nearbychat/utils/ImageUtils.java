package pro.postaru.sandu.nearbychat.utils;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

public class ImageUtils {

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
