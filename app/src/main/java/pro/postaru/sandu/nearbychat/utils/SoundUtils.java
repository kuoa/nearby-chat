package pro.postaru.sandu.nearbychat.utils;

import android.app.Activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SoundUtils {

    public static FileInputStream decodeByteArray(byte[] bytes, Activity activity) throws FileNotFoundException {
        File tempAudioFile =
                null;
        try {
            tempAudioFile = File.createTempFile("cat_destroyer_69_if_you_know_what_i_mean", "3gpp",  activity.getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempAudioFile);
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        tempAudioFile.deleteOnExit();

        FileInputStream fis = new FileInputStream(tempAudioFile);
        return fis;
    }

}
