package pro.postaru.sandu.nearbychat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import pro.postaru.sandu.nearbychat.activities.OnlineActivity;
import pro.postaru.sandu.nearbychat.constants.Database;
import pro.postaru.sandu.nearbychat.fragments.LoginFragment;
import pro.postaru.sandu.nearbychat.fragments.RegisterFragment;
import pro.postaru.sandu.nearbychat.models.OnlineUser;


public class MainActivity extends AppCompatActivity
        implements LoginFragment.OnFragmentInteractionListener,
        RegisterFragment.OnFragmentInteractionListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onStart() {
        super.onStart();

        user = firebaseAuth.getCurrentUser();

        if (user == null) {
            mountLoginFragment();
            Toast.makeText(MainActivity.this, "Please login or create a new account", Toast.LENGTH_LONG).show();
        } else {
            mountOnlineActivity();
        }
    }


    // app logic

    @Override
    public void requestLogin(String email, String password) {

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("BB", "signInWithEmail:success");
                        user = firebaseAuth.getCurrentUser();

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

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("NN", "createUserWithEmail:success");
                        user = firebaseAuth.getCurrentUser();
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
        OnlineUser onlineUser = new OnlineUser();

        databaseReference.child(Database.onlineUsers)
                .child(user.getUid())
                .setValue(onlineUser);
    }

    public void mountOnlineActivity() {
        Intent intent = new Intent(this, OnlineActivity.class);
        startActivity(intent);
    }

    @Override
    public void mountLoginFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, LoginFragment.newInstance());
        ft.commit();
    }

    @Override
    public void mountRegisterFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, RegisterFragment.newInstance());
        ft.commit();
    }

}