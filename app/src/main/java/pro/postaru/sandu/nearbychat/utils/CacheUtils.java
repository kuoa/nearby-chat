package pro.postaru.sandu.nearbychat.utils;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class CacheUtils {
    // Get max available VM memory, exceeding this amount will throw an
    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
    // int in its constructor.
    private static final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    // Use 1/8th of the available memory for this memory cache.
    private static final int cacheSize = maxMemory / 8;
    private static LruCache<String, Bitmap> mBitmapMemoryCache;
    private static LruCache<String, String> mRecordMemoryCache;

    public static void addBitmapToMemoryCache(String key, @NonNull Bitmap bitmap) {
        initBitmapCache();
        if (getBitmapFromMemCache(key) == null) {
            mBitmapMemoryCache.put(key, bitmap);
        }
    }

    public static Bitmap getBitmapFromMemCache(@NonNull String key) {
        initBitmapCache();
        return mBitmapMemoryCache.get(key);
    }


    private static void initBitmapCache() {
        if (mBitmapMemoryCache != null) return;
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.


        mBitmapMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };


    }

    private static void initRecordCache() {

        //TODO init with the last saved data from the cached directory
        if (mRecordMemoryCache != null) return;


        mRecordMemoryCache = new LruCache<>(cacheSize);


    }

    public static FileInputStream getRecordFromMemCache(String path) {
        FileInputStream fileInputStream = null;
        initRecordCache();
        String completePath = mRecordMemoryCache.get(path);
        if (completePath != null) {

            try {
                fileInputStream = new FileInputStream(new File(completePath));
            } catch (FileNotFoundException e) {
                //cache is not synced
                mRecordMemoryCache.remove(path);

            }
        }
        return fileInputStream;
    }

    public static void addRecordToMemoryCache(String storageReferencePath, String completePath) {
        initRecordCache();
        mRecordMemoryCache.put(storageReferencePath, completePath);

    }

}
