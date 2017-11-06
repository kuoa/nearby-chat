package pro.postaru.sandu.nearbychat;

import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;

import pro.postaru.sandu.nearbychat.models.Endpoint;


abstract class AdvertisingActivity extends ConnectionsActivity {
    /**
     * Callbacks for connections to other devices.
     */
    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    logD(
                            String.format(
                                    "onConnectionInitiated(endpointId=%s, endpointName=%s)",
                                    endpointId, connectionInfo.getEndpointName()));
                    Endpoint endpoint = new Endpoint(endpointId, connectionInfo.getEndpointName());
                    //mPendingConnections.put(endpointId, endpoint);
                    AdvertisingActivity.this.onConnectionInitiated(endpoint, connectionInfo);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    logD(String.format("onConnectionResponse(endpointId=%s, result=%s)", endpointId, result));

                  /*  // We're no longer connecting
                    mIsConnecting = false;

                    if (!result.getStatus().isSuccess()) {
                        logW(
                                String.format(
                                        "Connection failed. Received status %s.",
                                        LogDebug.formatStatus(result.getStatus())));
                        onConnectionFailed(mPendingConnections.remove(endpointId));
                        return;
                    }
                    connectedToEndpoint(mPendingConnections.remove(endpointId));*/
                }

                @Override
                public void onDisconnected(String endpointId) {
                   /* if (!mEstablishedConnections.containsKey(endpointId)) {
                        logW("Unexpected disconnection from endpoint " + endpointId);
                        return;
                    }
                    disconnectedFromEndpoint(mEstablishedConnections.get(endpointId));*/
                }
            };
    /**
     * True if we are advertising.
     */
    private boolean mIsAdvertising = false;

    /**
     * Sets the device to advertising mode. It will broadcast to other devices in discovery mode.
     * Either {@link #onAdvertisingStarted()} or {@link #onAdvertisingFailed()} will be called once
     * we've found out if we successfully entered this mode.
     */
    protected void startAdvertising() {
        mIsAdvertising = true;
        Nearby.Connections.startAdvertising(
                mGoogleApiClient,
                getName(),
                getServiceId(),
                mConnectionLifecycleCallback,
                new AdvertisingOptions(STRATEGY))
                .setResultCallback(
                        result -> {
                            if (result.getStatus().isSuccess()) {
                                logV("Now advertising endpoint " + result.getLocalEndpointName());
                                onAdvertisingStarted();
                            } else {
                                mIsAdvertising = false;
                                logW(
                                        String.format(
                                                "Advertising failed. Received status %s.",
                                                LogDebug.formatStatus(result.getStatus())));
                                onAdvertisingFailed();
                            }
                        });
    }

    /**
     * Stops advertising.
     */
    protected void stopAdvertising() {
        mIsAdvertising = false;
        Nearby.Connections.stopAdvertising(mGoogleApiClient);
    }

    /**
     * @return True if currently advertising.
     */
    protected boolean isAdvertising() {
        return mIsAdvertising;
    }

    /**
     * Advertising has successfully started. Override this method to act on the event.
     */
    protected void onAdvertisingStarted() {
    }

    /**
     * Advertising has failed to start. Override this method to act on the event.
     */
    protected void onAdvertisingFailed() {
    }

    /**
     * A pending connection with a remote endpoint has been created. Use {@link ConnectionInfo} for
     * metadata about the connection (like incoming vs outgoing, or the authentication token). If we
     * want to continue with the connection, call {@link #acceptConnection(Endpoint)}. Otherwise, call
     * {@link #rejectConnection(Endpoint)}.
     */
    protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {
        //assert (connectionInfo.isIncomingConnection()); TODO change doc redirect to chat
        Toast.makeText(getApplicationContext(), "onConnectionInitiated", Toast.LENGTH_LONG).show();
    }


}
