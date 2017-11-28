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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import pro.postaru.sandu.nearbychat.R;
import pro.postaru.sandu.nearbychat.adapters.ActiveConversationsAdapter;
import pro.postaru.sandu.nearbychat.constants.Database;
import pro.postaru.sandu.nearbychat.models.Conversation;
import pro.postaru.sandu.nearbychat.models.UserProfile;

public class ConversationsFragment extends Fragment {

    private FirebaseUser user;
    private DatabaseReference database;

    private List<UserProfile> conversationProfiles;

    private ActiveConversationsAdapter activeConversationsAdapter;

    private final ChildEventListener userConversationsListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            Conversation conversation = dataSnapshot.getValue(Conversation.class);

            if (conversation != null) {
                UserProfile userProfile = new UserProfile();
                userProfile.setUserName(conversation.getPartnerId());
                userProfile.setId(conversation.getPartnerId());
                userProfile.setBio("No bio");

                activeConversationsAdapter.add(userProfile);
            } else {
                Log.w("BBB", "No conversations");
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w("BBB", "loadPost:onCancelled", databaseError.toException());
        }
    };


    private ListView conversationsView;

    public ConversationsFragment() {
    }


    public static ConversationsFragment newInstance() {
        ConversationsFragment fragment = new ConversationsFragment();

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference();

        conversationProfiles = new ArrayList<>();

        database.child(Database.userConversations)
                .child(user.getUid())
                .child("conversations")
                .addChildEventListener(userConversationsListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conversations, container, false);

        activeConversationsAdapter = new ActiveConversationsAdapter(getActivity(), R.layout.online_users_entry, conversationProfiles);

        conversationsView = (ListView) view.findViewById(R.id.conversation_users_list);
        conversationsView.setAdapter(activeConversationsAdapter);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        database.child(Database.userConversations)
                .child(user.getUid())
                .child("conversations")
                .removeEventListener(userConversationsListener);
    }
}
