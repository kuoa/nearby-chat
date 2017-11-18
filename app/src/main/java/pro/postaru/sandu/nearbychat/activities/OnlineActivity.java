package pro.postaru.sandu.nearbychat.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import pro.postaru.sandu.nearbychat.MainActivity;
import pro.postaru.sandu.nearbychat.R;
import pro.postaru.sandu.nearbychat.adapters.ActiveConversationsAdapter;
import pro.postaru.sandu.nearbychat.adapters.OnlineFragmentPagerAdapter;
import pro.postaru.sandu.nearbychat.adapters.OnlineUsersAdapter;
import pro.postaru.sandu.nearbychat.constants.Database;

//logged => map conversation profile
public class OnlineActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnlineUsersAdapter.OnAdapterInteractionListener,
        ActiveConversationsAdapter.OnAdapterInteractionListener {

    private ViewPager viewPager;
    private OnlineFragmentPagerAdapter onlineFragmentPagerAdapter;


    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_online);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            // set the user profile info when the drawer is opening
            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_SETTLING) {
                    // opening
                    if (!drawer.isDrawerOpen(Gravity.START)) {
                        fillDrawerUserProfile(drawer);
                    }
                }
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs_online);

        onlineFragmentPagerAdapter = new OnlineFragmentPagerAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.container_online);
        viewPager.setAdapter(onlineFragmentPagerAdapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

        firebaseAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance().getReference();

        user = firebaseAuth.getCurrentUser();

        mountMapFragment();
    }

    // activity logic

    public void requestLogout() {
        Log.d("BB", "logout:success");
        firebaseAuth.signOut();

        // remove the user from the online database
        removeOnlineUser();
    }

    public void removeOnlineUser() {
        Log.d("NNN", "remove online user: success");
        database.child(Database.onlineUsers)
                .child(user.getUid())
                .removeValue();
    }

    public void mountMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void mountMapFragment() {
        /*
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container_online, MapFragment.newInstance());
        ft.commit();
         */
    }

    // view logic

    /**
     * Fills the user information in the drawer panel
     *
     * @param drawerView the drawer panel
     */
    public void fillDrawerUserProfile(View drawerView) {

        TextView drawerUserNameView = (TextView) drawerView.findViewById(R.id.drawer_user_name);
        TextView drawerUserBioView = (TextView) drawerView.findViewById(R.id.drawer_user_bio);
        ImageView drawerUserAvatarView = (ImageView) drawerView.findViewById(R.id.drawer_user_avatar);
        //TODO use the function from profileActivity
        SharedPreferences profile = getSharedPreferences(ProfileActivity.USER_INFO_PREFS, 0);

        String profileUserName = profile.getString(ProfileActivity.USER_NAME_KEY, "User name (default)");
        String profileBio = profile.getString(ProfileActivity.USER_BIO_KEY, "User bio (default)");
        String avatarPath = profile.getString(ProfileActivity.USER_AVATAR_KEY, "");

        drawerUserNameView.setText(profileUserName);
        drawerUserBioView.setText(profileBio);

        if (!avatarPath.equals("")) {
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
        } else if (id == R.id.action_logout) {

            requestLogout();
            mountMainActivity();

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
    public void mountChatActivity(String partnerId) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.PARTNER_ID, partnerId);
        startActivity(intent);
    }
}
