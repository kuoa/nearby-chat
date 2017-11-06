package pro.postaru.sandu.nearbychat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.nearby.connection.ConnectionInfo;

import java.util.Random;

import pro.postaru.sandu.nearbychat.models.Endpoint;


public class MainActivity extends AdvertisingActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> scanNetwork());

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            // set the user profile info when the drawer is opening
            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_SETTLING) {
                    // opening
                    if (!drawer.isDrawerOpen(Gravity.LEFT)) {
                        fillDrawerUserProfile(drawer);
                    }
                }
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    public void scanNetwork() {
        Intent intent = new Intent(this, ActiveUsersActivity.class);
        startActivity(intent);

    }

    /**
     * Fills the user information in the drawer panel
     *
     * @param drawerView the drawer panel
     */
    public void fillDrawerUserProfile(View drawerView) {

        TextView drawerUserNameView = (TextView) drawerView.findViewById(R.id.drawer_user_name);
        TextView drawerUserBioView = (TextView) drawerView.findViewById(R.id.drawer_user_bio);
        ImageView drawerUserAvatarView = (ImageView) drawerView.findViewById(R.id.drawer_user_avatar);

        SharedPreferences profile = getSharedPreferences(ProfileActivity.USER_INFO_PREFS, 0);

        String profileUserName = profile.getString(ProfileActivity.USER_NAME_KEY, "User name (default)");
        String profileBio = profile.getString(ProfileActivity.USER_BIO_KEY, "User bio (default)");
        String avatarPath = profile.getString(ProfileActivity.USER_AVATAR_KEY, "");

        drawerUserNameView.setText(profileUserName);
        drawerUserBioView.setText(profileBio);

        if (avatarPath != "") {
            drawerUserAvatarView.setImageBitmap(BitmapFactory.decodeFile(avatarPath));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        } else {
            return false;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        logV("MainActivity.onConnected");
        super.onConnected(bundle);
        tempMethodForName();
        startAdvertising();
    }

    private void tempMethodForName() {
        Random random = new Random();
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            name.append(random.nextInt(10));
        }
        setName(name.toString());
    }

    @Override
    public void onConnectionSuspended(int i) {
        logV("MainActivity.onConnectionSuspended");
        super.onConnectionSuspended(i);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        logV("MainActivity.onConnectionFailed");
        super.onConnectionFailed(connectionResult);

    }


    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onAdvertisingFailed() {
        logV("MainActivity.onAdvertisingFailed");
        super.onAdvertisingFailed();
    }

    @Override
    protected void onAdvertisingStarted() {
        logV("MainActivity.onAdvertisingStarted");
        super.onAdvertisingStarted();
    }

    @Override
    protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {
        super.onConnectionInitiated(endpoint, connectionInfo);
        //TODO
        //goto message connection is accepted
    }
}
