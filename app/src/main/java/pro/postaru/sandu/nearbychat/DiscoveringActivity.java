package pro.postaru.sandu.nearbychat;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pro.postaru.sandu.nearbychat.models.Endpoint;


abstract class DiscoveringActivity extends ConnectionsActivity {
    /**
     * The devices we've discovered near us.
     */
    private final Map<String, Endpoint> mDiscoveredEndpoints = new HashMap<>();
    /**
     * True if we are discovering.
     */
    private boolean mIsDiscovering = false;


    /**
     * Sets the device to discovery mode. It will now listen for devices in advertising mode. Either
     * {@link #onDiscoveryStarted()} ()} or {@link #onDiscoveryFailed()} ()} will be called once we've
     * found out if we successfully entered this mode.
     */
    protected void startDiscovering() {
        mIsDiscovering = true;
        mDiscoveredEndpoints.clear();
        Nearby.Connections.startDiscovery(
                mGoogleApiClient,
                getServiceId(),
                new EndpointDiscoveryCallback() {
                    @Override
                    public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                        logD(
                                String.format(
                                        "onEndpointFound(endpointId=%s, serviceId=%s, endpointName=%s)",
                                        endpointId, info.getServiceId(), info.getEndpointName()));

                        if (getServiceId().equals(info.getServiceId())) {
                            Endpoint endpoint = new Endpoint(endpointId, info.getEndpointName());
                            mDiscoveredEndpoints.put(endpointId, endpoint);
                            onEndpointDiscovered(endpoint);
                        }
                    }

                    @Override
                    public void onEndpointLost(String endpointId) {
                        logD(String.format("onEndpointLost(endpointId=%s)", endpointId));
                    }
                },
                new DiscoveryOptions(STRATEGY))
                .setResultCallback(
                        status -> {
                            if (status.isSuccess()) {
                                onDiscoveryStarted();
                            } else {
                                mIsDiscovering = false;
                                logW(
                                        String.format(
                                                "Discovering failed. Received status %s.",
                                                LogDebug.formatStatus(status)));
                                onDiscoveryFailed();
                            }
                        });
    }

    /**
     * Stops discovery.
     */
    protected void stopDiscovering() {
        mIsDiscovering = false;
        Nearby.Connections.stopDiscovery(mGoogleApiClient);
    }

    /**
     * @return True if currently discovering.
     */
    protected boolean isDiscovering() {
        return mIsDiscovering;
    }

    /**
     * Discovery has successfully started. Override this method to act on the event.
     */
    protected void onDiscoveryStarted() {
    }

    /**
     * Discovery has failed to start. Override this method to act on the event.
     */
    protected void onDiscoveryFailed() {
    }

    /**
     * A remote endpoint has been discovered. Override this method to act on the event. To connect to
     * the device, call {@link #connectToEndpoint(Endpoint)}.
     */
    protected void onEndpointDiscovered(Endpoint endpoint) {
    }

    /**
     * @return A list of currently connected endpoints.
     */
    protected Set<Endpoint> getDiscoveredEndpoints() {
        Set<Endpoint> endpoints = new HashSet<>();
        endpoints.addAll(mDiscoveredEndpoints.values());
        return endpoints;
    }
}
