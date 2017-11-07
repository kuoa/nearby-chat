package pro.postaru.sandu.nearbychat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import pro.postaru.sandu.nearbychat.activities.ActiveUsersActivity;
import pro.postaru.sandu.nearbychat.activities.ProfileActivity;
import pro.postaru.sandu.nearbychat.fragments.LoginFragment;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoginFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanNetwork();
            }
        });

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



        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // Replace the contents of the container with the new fragment
        ft.replace(R.id.fragment_container, LoginFragment.newInstance());
        // or ft.add(R.id.your_placeholder, new FooFragment());
        // Complete the changes added above
        ft.commit();


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
    public void onFragmentInteraction(Uri uri) {

    }
}
