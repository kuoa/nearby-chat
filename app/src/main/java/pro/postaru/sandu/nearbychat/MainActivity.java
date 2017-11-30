package pro.postaru.sandu.nearbychat;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import pro.postaru.sandu.nearbychat.activities.OnlineActivity;
import pro.postaru.sandu.nearbychat.adapters.MainFragmentPagerAdapter;
import pro.postaru.sandu.nearbychat.constants.Database;
import pro.postaru.sandu.nearbychat.fragments.LoginFragment;
import pro.postaru.sandu.nearbychat.fragments.RegisterFragment;
import pro.postaru.sandu.nearbychat.models.OnlineUser;


public class MainActivity extends AppCompatActivity
        implements LoginFragment.OnFragmentInteractionListener,
        RegisterFragment.OnFragmentInteractionListener {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference database;

    private ViewPager viewPager;

    private MainFragmentPagerAdapter mainFragmentPagerAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        mainFragmentPagerAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.container_main);
        viewPager.setAdapter(mainFragmentPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs_main);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onStart() {
        super.onStart();

        user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(MainActivity.this, "Please login or create a new account", Toast.LENGTH_LONG).show();
        } else {
            mountOnlineActivity();
        }
    }


    // app logic

    @Override
    public void requestLogin(String email, String password) {

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("BB", "signInWithEmail:success");
                        user = auth.getCurrentUser();

                        registerOnlineUser();
                        mountOnlineActivity();

                        Log.d("NN", user.getEmail() != null ? user.getEmail() : "EMPTY");
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("BB", "signInWithEmail:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void requestRegister(String username, String email, String password) {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("NN", "createUserWithEmail:success");
                        user = auth.getCurrentUser();
                        Log.d("NN", user.getEmail() != null ? user.getEmail() : "EMPTY");

                        registerOnlineUser();
                        mountOnlineActivity();

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("NN", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void registerOnlineUser() {
        OnlineUser onlineUser = new OnlineUser(user.getUid());

        database.child(Database.onlineUsers)
                .child(user.getUid())
                .setValue(onlineUser);
    }

    public void mountOnlineActivity() {
        Intent intent = new Intent(this, OnlineActivity.class);
        startActivity(intent);
    }
}