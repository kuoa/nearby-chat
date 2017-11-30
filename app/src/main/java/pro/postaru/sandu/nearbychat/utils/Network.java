package pro.postaru.sandu.nearbychat.utils;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Network {

    public static boolean isAvailable(ConnectivityManager connectivityManager) {
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null;
    }
}
