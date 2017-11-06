package pro.postaru.sandu.nearbychat;

import android.support.annotation.CallSuper;
import android.util.Log;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;

import java.util.Locale;

public interface LogDebug {

    /**
     * Transforms a {@link Status} into a English-readable message for logging.
     *
     * @param status The current status
     * @return A readable String. eg. [404]File not found.
     */
    static String formatStatus(Status status) {
        return String.format(
                Locale.US,
                "[%d]%s",
                status.getStatusCode(),
                status.getStatusMessage() != null
                        ? status.getStatusMessage()
                        : ConnectionsStatusCodes.getStatusCodeString(status.getStatusCode()));
    }

    @CallSuper
    default void logV(String msg) {
        Log.v(Constant.TAG, msg);
    }

    @CallSuper
    default void logD(String msg) {
        Log.d(Constant.TAG, msg);
    }

    @CallSuper
    default void logW(String msg) {
        Log.w(Constant.TAG, msg);
    }

    @CallSuper
    default void logE(String msg, Throwable e) {
        Log.e(Constant.TAG, msg, e);
    }

}
