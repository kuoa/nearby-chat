package pro.postaru.sandu.nearbychat.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import pro.postaru.sandu.nearbychat.R;
import pro.postaru.sandu.nearbychat.adapters.OnlineUsersAdapter;
import pro.postaru.sandu.nearbychat.constants.Database;
import pro.postaru.sandu.nearbychat.models.OnlineUser;
import pro.postaru.sandu.nearbychat.models.UserProfile;

public class MapFragment extends Fragment {

    private List<UserProfile> onlineUserProfiles;

    private ListView onlineUsersView;

    private OnlineUsersAdapter onlineUsersAdapter;

    private FirebaseUser user;
    private DatabaseReference database;

    private final ValueEventListener onlineUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            onlineUserProfiles.clear();

            for (DataSnapshot child : dataSnapshot.getChildren()) {

                OnlineUser onlineUser = child.getValue(OnlineUser.class);

                if (onlineUser != null) {
                    UserProfile userProfile = new UserProfile();
                    userProfile.userName = onlineUser.getId();
                    userProfile.bio = "No bio";

                    onlineUserProfiles.add(userProfile);

                } else {
                    Log.w("BBB", "No online users");
                }

                onlineUsersAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w("BBB", "loadPost:onCancelled", databaseError.toException());
        }
    };


    public MapFragment() {
    }


    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();

        database.child(Database.onlineUsers).addValueEventListener(onlineUserListener);

        onlineUserProfiles = new ArrayList<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        onlineUsersAdapter = new OnlineUsersAdapter(getActivity(), R.layout.online_users_entry, onlineUserProfiles);

        onlineUsersView = (ListView) view.findViewById(R.id.online_users_list);
        onlineUsersView.setAdapter(onlineUsersAdapter);

        return view;
    }
}
