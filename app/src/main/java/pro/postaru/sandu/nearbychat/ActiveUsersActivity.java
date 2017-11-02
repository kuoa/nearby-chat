package pro.postaru.sandu.nearbychat;

import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import pro.postaru.sandu.nearbychat.adapters.ActiveUsersAdapter;
import pro.postaru.sandu.nearbychat.models.UserProfile;

public class ActiveUsersActivity extends AppCompatActivity {

    private List<UserProfile> activeUsers;

    private ActiveUsersAdapter activeUsersAdapter;

    private ListView activeUsersView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_users);

        activeUsers = new ArrayList<>();

        for(int i = 0; i < 20; i++){
            UserProfile profile = new UserProfile();

            profile.setUserName("User " + i);
            profile.setBio("Bio " + i);
            activeUsers.add(profile);
        }

        activeUsersAdapter = new ActiveUsersAdapter(this, R.layout.active_users_entry, activeUsers);

        activeUsersView = (ListView) findViewById(R.id.active_users_list);
        activeUsersView.setAdapter(activeUsersAdapter);
    }
}
