package pro.postaru.sandu.nearbychat.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import pro.postaru.sandu.nearbychat.constants.Constant;


public class CacheUtils {
    // Get max available VM memory, exceeding this amount will throw an
    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
    // int in its constructor.
    private static final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    // Use 1/8th of the available memory for this memory cache.
    private static final int cacheSize = maxMemory / 8;

    private static LruCache<String, Bitmap> mBitmapMemoryCache;
    private static LruCache<String, String> mRecordMemoryCache;

    /**
     * Store the specified bitmap and its identifier in th memory cache
     * if already something is already associated with the key we ignore the new value
     *
     * @param key    identifier
     * @param bitmap bitmap
     */
    public static void addBitmapToMemoryCache(String key, @NonNull Bitmap bitmap) {
        initBitmapCache();
        if (getBitmapFromMemCache(key) == null) {
            mBitmapMemoryCache.put(key, bitmap);
        }
    }

    /**
     * Retrieve the Bitmap associated for the specified key
     * Return null if the bitmap is not in the cache
     *
     * @param key the key
     * @return the bitmap
     */
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

    /**
     * Init the record cache
     * If you want to repopulate the in memory cache from the internal space you must specify the dedicated directory
     *
     * @param cacheDirectory cacheDirectory  (optional)
     */
    private static void initRecordCache(File cacheDirectory) {


        if (mRecordMemoryCache != null) return;


        mRecordMemoryCache = new LruCache<>(cacheSize);
        //populate in memory table
        if (cacheDirectory != null) {
            Log.d(Constant.CACHE_UTILS, "initRecordCache: populate in memory cache");
            //list of file
            File[] fileList = cacheDirectory.listFiles();

            for (File file : fileList) {
                String fileName = file.getName();
                String absolutePath = file.getAbsolutePath();
                mRecordMemoryCache.put(fileName, absolutePath);
            }
        }

    }

    /**
     * Retrieve record from the specified activity's cache directory
     * return null if not found
     *
     * @param key      key
     * @param activity current activity
     * @return the record (FileInputStream)
     */
    public static FileInputStream getRecordFromMemCache(String key, Activity activity) {
        FileInputStream fileInputStream = null;
        initRecordCache(SoundUtils.getRecordDirectory(activity));

        String absolutePath = mRecordMemoryCache.get(key);
        if (absolutePath != null) {

            try {
                fileInputStream = new FileInputStream(new File(absolutePath));
            } catch (FileNotFoundException e) {
                //cache is not synced
                mRecordMemoryCache.remove(key);

            }
        }
        return fileInputStream;
    }

    /**
     * Store the specified key,value in the memoryCache
     *
     * @param key          key
     * @param absolutePath path
     */
    public static void addRecordToMemoryCache(String key, String absolutePath) {
        initRecordCache(null);
        mRecordMemoryCache.put(key, absolutePath);

    }

}
