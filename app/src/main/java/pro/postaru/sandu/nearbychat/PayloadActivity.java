package pro.postaru.sandu.nearbychat;


import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import pro.postaru.sandu.nearbychat.models.Endpoint;

abstract class PayloadActivity extends ConnectionsActivity {

    /**
     * Distant Endpoint for this activity/view
     */
    private Endpoint mEndpoint;
    /**
     * Callbacks for payloads (bytes of data) sent from another device to us.
     */
    private final PayloadCallback mPayloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    logD(String.format("onPayloadReceived(endpointId=%s, payload=%s)", endpointId, payload));
                    if (endpointId.equals(mEndpoint.getId())) {
                        onReceive(mEndpoint, payload);
                    }
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    logD(
                            String.format(
                                    "onPayloadTransferUpdate(endpointId=%s, update=%s)", endpointId, update));
                    onUpdate(endpointId, update);

                }
            };
    /**
     * True if we are asking a discovered device to connect to us. While we ask, we cannot ask another
     * device.
     */
    private boolean mIsConnecting = false;
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
                    mEndpoint = new Endpoint(endpointId, connectionInfo.getEndpointName());

                    PayloadActivity.this.onConnectionInitiated(mEndpoint, connectionInfo);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    logD(String.format("onConnectionResponse(endpointId=%s, result=%s)", endpointId, result));

                    // We're no longer connecting
                    mIsConnecting = false;

                    if (!result.getStatus().isSuccess()) {
                        logW(
                                String.format(
                                        "Connection failed. Received status %s.",
                                        LogDebug.formatStatus(result.getStatus())));
                        onConnectionFailed(mEndpoint);
                        return;
                    }
                    connectedToEndpoint(mEndpoint);
                }

                @Override
                public void onDisconnected(String endpointId) {
                    if (mEndpoint == null) {
                        logW("Unexpected disconnection from mEndpoint " + endpointId);
                        return;
                    }
                    disconnectedFromEndpoint(mEndpoint);
                }
            };

    /**
     * Sends a connection request to the mEndpoint.
     */
    protected void connectToEndpoint(final Endpoint endpoint) {

        logV("Sending a connection request to mEndpoint " + endpoint);
        // Mark ourselves as connecting so we don't connect multiple times
        mIsConnecting = true;

        // Ask to connect
        Nearby.Connections.requestConnection(
                mGoogleApiClient, getName(), endpoint.getId(), mConnectionLifecycleCallback)
                .setResultCallback(
                        status -> {
                            if (!status.isSuccess()) {
                                logW(
                                        String.format(
                                                "requestConnection failed. %s", LogDebug.formatStatus(status)));
                                mIsConnecting = false;
                                onConnectionFailed(endpoint);
                            }
                        });
    }

    /**
     * Accepts a connection request.
     */
    protected void acceptConnection(final Endpoint endpoint) {
        Nearby.Connections.acceptConnection(mGoogleApiClient, endpoint.getId(), mPayloadCallback)
                .setResultCallback(
                        status -> {
                            if (!status.isSuccess()) {
                                logW(
                                        String.format(
                                                "acceptConnection failed. %s", LogDebug.formatStatus(status)));
                            }
                        });
    }

    /**
     * Sends a {@link Payload} to all currently connected endpoints.
     *
     * @param payload The data you want to send.
     */
    protected void send(Payload payload, String endpointId) {
        Nearby.Connections.sendPayload(mGoogleApiClient, endpointId, payload)
                .setResultCallback(
                        status -> {
                            if (!status.isSuccess()) {
                                logW(
                                        String.format(
                                                "sendUnreliablePayload failed. %s",
                                                LogDebug.formatStatus(status)));
                            }
                        });
    }

    /**
     * Someone connected to us has sent us data. Override this method to act on the event.
     *
     * @param endpoint The sender.
     * @param payload  The data.
     */
    protected void onReceive(Endpoint endpoint, Payload payload) {
    }

    /**
     * Called with progress information about an active Payload transfer, either incoming or outgoing.
     * Override this method to act on the event
     *
     * @param endpointId The identifier for the remote mEndpoint that is sending or receiving this payload.
     * @param update     The PayloadTransferUpdate describing the status of the transfer.
     */
    protected void onUpdate(String endpointId, PayloadTransferUpdate update) {
    }

    /**
     * A pending connection with a remote mEndpoint has been created. Use {@link ConnectionInfo} for
     * metadata about the connection (like incoming vs outgoing, or the authentication token). If we
     * want to continue with the connection, call {@link #acceptConnection(Endpoint)}. Otherwise, call
     * {@link #rejectConnection(Endpoint)}.
     */
    protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {
    }

    /**
     * True if we're currently attempting to connect to another device.
     */
    protected boolean isConnecting() {
        return mIsConnecting;
    }

}
