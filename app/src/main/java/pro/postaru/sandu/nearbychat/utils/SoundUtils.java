package pro.postaru.sandu.nearbychat.utils;

import android.app.Activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SoundUtils {

    public static File decodeByteArray(String filename, byte[] bytes, Activity activity) throws FileNotFoundException {
        File tempAudioFile =
                null;
        try {
            //list of cached temp file

            tempAudioFile = File.createTempFile(filename, "3gpp", activity.getCacheDir());
            // tempAudioFile = new File(activity.getCacheDir(), filename);
            FileOutputStream fos = new FileOutputStream(tempAudioFile);
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempAudioFile;
    }

}
