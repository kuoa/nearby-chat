package pro.postaru.sandu.nearbychat;

import android.app.Application;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;

import java.util.concurrent.Semaphore;

public class App extends Application implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LogDebug {
    private static App mInstance;
    private final Semaphore available = new Semaphore(0, false);
    private GoogleApiClient mGoogleApiClient;

    public static synchronized App getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        createGoogleApiClient();
    }

    /**
     * TODO
     */
    public void createGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Nearby.CONNECTIONS_API)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    /**
     * We've connected to Nearby Connections' GoogleApiClient.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        logV("onConnected");
        available.drainPermits();
        available.release();
    }

    /**
     * We've been temporarily disconnected from Nearby Connections' GoogleApiClient.
     */
    @CallSuper
    @Override
    public void onConnectionSuspended(int reason) {
        logW(String.format("onConnectionSuspended(reason=%s)", reason));
        try {
            available.acquire();
        } catch (InterruptedException e) {
            logE("available.acquire())", e);
        }
        mGoogleApiClient.reconnect();

    }

    /**
     * We are unable to connect to Nearby Connections' GoogleApiClient. Oh uh.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        logW(
                String.format(
                        "onConnectionFailed(%s)",
                        LogDebug.formatStatus(new Status(connectionResult.getErrorCode()))));
        logV("App.onConnectionFailed : mGoogleApiClient.reconnect");
        mGoogleApiClient.reconnect();
    }

    /**
     * TODO
     * blocking
     *
     * @return
     */
    public GoogleApiClient getGoogleClientApi() {
        if (mGoogleApiClient == null) {
            createGoogleApiClient(); //Create the first time
        }

        while (!mGoogleApiClient.isConnected()) {//async connection
            try {
                available.acquire();
                available.drainPermits();
            } catch (InterruptedException e) {
                logE("available.acquire())", e);
            }
        }

        return mGoogleApiClient;

    }
}
