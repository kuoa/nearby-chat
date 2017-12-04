package pro.postaru.sandu.nearbychat.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import pro.postaru.sandu.nearbychat.MainActivity;
import pro.postaru.sandu.nearbychat.R;
import pro.postaru.sandu.nearbychat.adapters.ActiveConversationsAdapter;
import pro.postaru.sandu.nearbychat.adapters.OnlineFragmentPagerAdapter;
import pro.postaru.sandu.nearbychat.adapters.OnlineUsersAdapter;
import pro.postaru.sandu.nearbychat.constants.Constant;
import pro.postaru.sandu.nearbychat.constants.Database;
import pro.postaru.sandu.nearbychat.models.UserProfile;
import pro.postaru.sandu.nearbychat.utils.Network;

import static pro.postaru.sandu.nearbychat.constants.Constant.FIREBASE_STORAGE_REFERENCE;

public class OnlineActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnlineUsersAdapter.OnAdapterInteractionListener,
        ActiveConversationsAdapter.OnAdapterInteractionListener {

    private UserProfile userProfile;

    private ViewPager viewPager;
    private DrawerLayout drawer;
    private final ValueEventListener userProfileListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(Constant.NEARBY_CHAT, "onDataChange: dataSnapshot = [" + dataSnapshot + "]");
            UserProfile userProfileLocal = dataSnapshot.getValue(UserProfile.class);

            if (userProfileLocal != null) {
                userProfile = userProfileLocal;
                Log.w(Constant.NEARBY_CHAT, "Online profile loaded for id " + OnlineActivity.this.userProfile.getId());

                initProfileView(drawer);
            } else {
                Log.w(Constant.NEARBY_CHAT, "Error while loading the online sharedPreferences");
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(Constant.NEARBY_CHAT, "Error database");
        }
    };
    private ProgressBar progressBar;
    private OnlineFragmentPagerAdapter onlineFragmentPagerAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference database;
    private FirebaseStorage firebaseStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_online);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            // set the firebaseUser sharedPreferences info when the drawer is opening
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
        firebaseStorage = FirebaseStorage.getInstance(FIREBASE_STORAGE_REFERENCE);

        database = FirebaseDatabase.getInstance().getReference();

        firebaseUser = firebaseAuth.getCurrentUser();

        userProfile = new UserProfile();
    }

    // activity logic

    public void requestLogout() {
        Log.d("BB", "logout:success");
        firebaseAuth.signOut();

        // remove the firebaseUser from the online database
        removeOnlineUser();
    }

    public void removeOnlineUser() {
        Log.d("NNN", "remove online firebaseUser: success");
        database.child(Database.onlineUsers)
                .child(firebaseUser.getUid())
                .removeValue();
    }

    public void mountMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    // view logic

    /**
     * Fills the firebaseUser information in the drawer panel
     *
     * @param drawerView the drawer panel
     */
    public void fillDrawerUserProfile(View drawerView) {

        progressBar = (ProgressBar) drawerView.findViewById(R.id.drawer_spinner);

        progressBar.setVisibility(View.VISIBLE);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Network.isAvailable(connectivityManager)) {
            loadProfileOnline();
        } else {
            initProfileView(drawerView);
        }

        progressBar.setVisibility(View.GONE);
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
    public void mountChatActivity(UserProfile userProfile) {
        Intent intent = new Intent(this, ChatActivity.class);
        //todo undo this
        userProfile.setAvatar(null);
        intent.putExtra(ChatActivity.PARTNER_USER_PROFILE, userProfile);
        startActivity(intent);
    }


    /**
     * Load userProfile for the current firebaseUser with the online version
     * Async task
     */
    private void loadProfileOnline() {
        Log.d(Constant.NEARBY_CHAT, "Load profile online and add listener for id " + firebaseUser.getUid());
        database.child(Database.userProfiles).child(firebaseUser.getUid()).addListenerForSingleValueEvent(userProfileListener);
        loadProfileImage();

    }


    private void initProfileView(View drawerView) {

        TextView drawerUserNameView = (TextView) drawerView.findViewById(R.id.drawer_user_name);
        TextView drawerUserBioView = (TextView) drawerView.findViewById(R.id.drawer_user_bio);
        ImageView drawerUserAvatarView = (ImageView) drawerView.findViewById(R.id.drawer_user_avatar);

        drawerUserNameView.setText(userProfile.getUserName());
        drawerUserBioView.setText(userProfile.getBio());
        if (userProfile.getAvatar() != null) {
            drawerUserAvatarView.setImageBitmap(userProfile.getAvatar());
        }
    }

    @NonNull
    private StorageReference getStorageReference() {
        return firebaseStorage.getReference("profile/" + firebaseUser.getUid() + ".jpeg");
    }

    private void loadProfileImage() {
        ImageView drawerUserAvatarView = (ImageView) drawer.findViewById(R.id.drawer_user_avatar);
        StorageReference reference = getStorageReference();

        final long ONE_MEGABYTE = 1024 * 1024;
        reference.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            // Data for "profile" is returns, use this as needed
            Bitmap avatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            userProfile.setAvatar(avatar);
            drawerUserAvatarView.setImageBitmap(avatar);
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Log.w(Constant.NEARBY_CHAT, "loadProfileImage: ", exception);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.child(Database.userProfiles).child(firebaseUser.getUid()).removeEventListener(userProfileListener);
    }
}
