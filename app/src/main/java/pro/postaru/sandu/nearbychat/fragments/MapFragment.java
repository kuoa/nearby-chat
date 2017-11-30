package pro.postaru.sandu.nearbychat.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
    private ProgressBar mainProgresBar;

    private OnlineUsersAdapter onlineUsersAdapter;
    private final ValueEventListener getUserProfileListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);

            mainProgresBar.setVisibility(View.GONE);

            if (userProfile != null) {
                onlineUsersAdapter.add(userProfile);
            }

            Log.w("BBB", "id " + userProfile.getId());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w("BBB", "Canceled profile request");

        }
    };
    private FirebaseUser user;
    private DatabaseReference database;
    private final ChildEventListener onlineUserListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            OnlineUser onlineUser = dataSnapshot.getValue(OnlineUser.class);

            // new online user => hook up the profile listener
            if (onlineUser != null) {
                database.child(Database.userProfiles)
                        .child(onlineUser.getId())
                        .addListenerForSingleValueEvent(getUserProfileListener);

            } else {
                Log.w("BBB", "No online users");
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

            OnlineUser onlineUser = dataSnapshot.getValue(OnlineUser.class);

            if (onlineUser != null) {
                UserProfile userProfile = new UserProfile();

                //only need the id to remove
                userProfile.setId(onlineUser.getId());

                Log.w("BBB", "remove id " + userProfile.getId());

                onlineUsersAdapter.remove(userProfile);

            } else {
                Log.w("BBB", "No online users");
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w("BBB", "loadPost:onCancelled", databaseError.toException());
        }
    };

    public MapFragment() {
    }


    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();

        onlineUserProfiles = new ArrayList<>();

        mainProgresBar = (ProgressBar) getActivity().findViewById(R.id.online_spinner);
        mainProgresBar.setVisibility(View.VISIBLE);

        database.child(Database.onlineUsers).addChildEventListener(onlineUserListener);

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

    @Override
    public void onDestroy() {
        super.onDestroy();

        database.child(Database.onlineUsers).removeEventListener(onlineUserListener);

    }
}
