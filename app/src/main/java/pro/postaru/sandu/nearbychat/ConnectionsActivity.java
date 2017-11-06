package pro.postaru.sandu.nearbychat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Strategy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pro.postaru.sandu.nearbychat.models.Endpoint;

/**
 * A class that connects to Nearby Connections and provides convenience methods and callbacks.
 */
public abstract class ConnectionsActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LogDebug {
    /**
     * The connection strategy we'll use for Nearby Connections. In this case, we've decided on
     * P2P_STAR, which is a combination of Bluetooth Classic and WiFi Hotspots.
     */
    protected static final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    /**
     * This service id lets us find other nearby devices that are interested in the same thing. Our
     * sample does exactly one thing, so we hardcode the ID.
     */
    private static final String SERVICE_ID =
            "pro.postaru.sandu.nearbychat.SERVICE_ID";
    /**
     * These permissions are required before connecting to Nearby Connections. Only {@link
     * Manifest.permission#ACCESS_COARSE_LOCATION} is considered dangerous, so the others should be
     * granted just by having them in our AndroidManfiest.xml
     */
    private static final String[] REQUIRED_PERMISSIONS =
            new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };
    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;
    /**
     * We'll talk to Nearby Connections through the GoogleApiClient.
     */
    protected static GoogleApiClient mGoogleApiClient;
    /**
     * A random UID used as this device's endpoint name.
     */
    private static String mName;
    /**
     * The devices we have pending connections to. They will stay pending until we call {@link
     * #acceptConnection(Endpoint)} or {@link #rejectConnection(Endpoint)}.
     */
    private final Map<String, Endpoint> mPendingConnections = new HashMap<>();
    /**
     * The devices we are currently connected to. For advertisers, this may be large. For discoverers,
     * there will only be one entry in this map.
     */
    private final Map<String, Endpoint> mEstablishedConnections = new HashMap<>();


    /**
     * @return True if the app was granted all the permissions. False otherwise.
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return The client's name. Visible to others when connecting.
     */
    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        ConnectionsActivity.mName = mName;
    }

    private void createGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient =
                    new GoogleApiClient.Builder(this)
                            .addApi(Nearby.CONNECTIONS_API)
                            .addConnectionCallbacks(this)
                            .enableAutoManage(this, this)
                            .build();
        }
    }

    /**
     * Our Activity has just been made visible to the user. Our GoogleApiClient will start connecting
     * after super.onStart() is called.
     */
    @Override
    protected void onStart() {
        if (hasPermissions(this, getRequiredPermissions())) {
            createGoogleApiClient();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(getRequiredPermissions(), REQUEST_CODE_REQUIRED_PERMISSIONS);
            }
        }
        super.onStart();
        /*Toast.makeText(getApplicationContext(), "" + mGoogleApiClient.isConnected(), Toast.LENGTH_LONG).show();*/
    }

    /**
     * We've connected to Nearby Connections' GoogleApiClient.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        logV("onConnected");
    }

    /**
     * We've been temporarily disconnected from Nearby Connections' GoogleApiClient.
     */
    @CallSuper
    @Override
    public void onConnectionSuspended(int reason) {
        logW(String.format("onConnectionSuspended(reason=%s)", reason));
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
    }

    /**
     * The user has accepted (or denied) our permission request.
     */
    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_REQUIRED_PERMISSIONS) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "Cannot start without required permissions", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
            recreate();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Rejects a connection request.
     */
    protected void rejectConnection(Endpoint endpoint) {
        Nearby.Connections.rejectConnection(mGoogleApiClient, endpoint.getId())
                .setResultCallback(
                        status -> {
                            if (!status.isSuccess()) {
                                logW(
                                        String.format(
                                                "rejectConnection failed. %s", LogDebug.formatStatus(status)));
                            }
                        });
    }

    protected void disconnect(Endpoint endpoint) {
        Nearby.Connections.disconnectFromEndpoint(mGoogleApiClient, endpoint.getId());
        mEstablishedConnections.remove(endpoint.getId());
    }

    protected void disconnectFromAllEndpoints() {
        for (Endpoint endpoint : mEstablishedConnections.values()) {
            Nearby.Connections.disconnectFromEndpoint(mGoogleApiClient, endpoint.getId());
        }
        mEstablishedConnections.clear();
    }

    protected void connectedToEndpoint(Endpoint endpoint) {
        logD(String.format("connectedToEndpoint(endpoint=%s)", endpoint));
        mEstablishedConnections.put(endpoint.getId(), endpoint);
        onEndpointConnected(endpoint);
    }

    protected void disconnectedFromEndpoint(Endpoint endpoint) {
        logD(String.format("disconnectedFromEndpoint(endpoint=%s)", endpoint));
        mEstablishedConnections.remove(endpoint.getId());
        onEndpointDisconnected(endpoint);
    }

    /**
     * A connection with this endpoint has failed. Override this method to act on the event.
     */
    protected void onConnectionFailed(Endpoint endpoint) {
    }

    /**
     * Someone has connected to us. Override this method to act on the event.
     */
    protected void onEndpointConnected(Endpoint endpoint) {
    }

    /**
     * Someone has disconnected. Override this method to act on the event.
     */
    protected void onEndpointDisconnected(Endpoint endpoint) {
    }

    /**
     * @return A list of currently connected endpoints.
     */
    protected Set<Endpoint> getConnectedEndpoints() {
        Set<Endpoint> endpoints = new HashSet<>();
        endpoints.addAll(mEstablishedConnections.values());
        return endpoints;
    }

    /**
     * An optional hook to pool any permissions the app needs with the permissions ConnectionsActivity
     * will request.
     *
     * @return All permissions required for the app to properly function.
     */
    protected String[] getRequiredPermissions() {
        return REQUIRED_PERMISSIONS;
    }

    /**
     * @return The service id. This represents the action this connection is for. When discovering,
     * we'll verify that the advertiser has the same service id before we consider connecting to
     * them.
     */
    protected String getServiceId() {
        return SERVICE_ID;
    }
}
