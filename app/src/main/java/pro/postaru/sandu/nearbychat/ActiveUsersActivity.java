package pro.postaru.sandu.nearbychat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;

import java.util.ArrayList;
import java.util.List;

import pro.postaru.sandu.nearbychat.adapters.ActiveUsersAdapter;
import pro.postaru.sandu.nearbychat.models.Endpoint;
import pro.postaru.sandu.nearbychat.models.UserProfile;

public class ActiveUsersActivity extends DiscoveringActivity {

    private List<UserProfile> activeUsers;


    private ActiveUsersAdapter activeUsersAdapter;

    private ListView activeUsersView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_users);

        activeUsers = new ArrayList<>();
        //Test Only
      /*  for (int i = 0; i < 20; i++) {
            UserProfile profile = new UserProfile();

            profile.setUserName("User " + i);
            profile.setBio("Bio " + i);
            activeUsers.add(profile);
        }*/

        activeUsersAdapter = new ActiveUsersAdapter(this, R.layout.active_users_entry, activeUsers);
        activeUsersView = (ListView) findViewById(R.id.active_users_list);
        activeUsersView.setAdapter(activeUsersAdapter);


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        logV("ActiveUsersActivity.onConnected");
        super.onConnected(bundle);
    }

    @Override
    public void onConnectionSuspended(int i) {
        logV("ActiveUsersActivity.onConnectionSuspended");
        super.onConnectionSuspended(i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        logV("ActiveUsersActivity.onConnectionFailed ");
        super.onConnectionFailed(connectionResult);

    }


    @Override
    public void onStart() {
        super.onStart();
        //discovery on
        startDiscovering();
    }

    @Override
    protected void onEndpointDiscovered(Endpoint endpoint) {
        logD("ActiveUsersActivity.onEndpointDiscovered");
        logD("endpoint = [" + endpoint + "]");
        super.onEndpointDiscovered(endpoint);

        UserProfile profile = new UserProfile();
        profile.setUserName(endpoint.getName());
        profile.setId(endpoint.getId());

        activeUsers.add(profile);
        activeUsersAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDiscoveryStarted() {
        logD("ActiveUsersActivity.onDiscoveryStarted");
        super.onDiscoveryStarted();

    }

    @Override
    protected void onDiscoveryFailed() {
        logD("ActiveUsersActivity.onDiscoveryFailed");
        super.onDiscoveryFailed();

    }

}
