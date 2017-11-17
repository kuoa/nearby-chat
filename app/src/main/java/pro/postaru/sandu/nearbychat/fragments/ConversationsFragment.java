package pro.postaru.sandu.nearbychat.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import pro.postaru.sandu.nearbychat.R;
import pro.postaru.sandu.nearbychat.adapters.ActiveConversationsAdapter;
import pro.postaru.sandu.nearbychat.models.UserConversations;
import pro.postaru.sandu.nearbychat.models.UserProfile;

public class ConversationsFragment extends Fragment {

    private UserConversations conversations;

    private FirebaseUser user;
    private DatabaseReference database;

    private List<UserProfile> conversationUsersProfiles;

    private ActiveConversationsAdapter activeConversationsAdapter;

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

        conversationUsersProfiles = new ArrayList<>();

        UserProfile user = new UserProfile();
        user.setId("Test");
        user.setUserName("test");

        conversationUsersProfiles.add(user);

        //database.child(Database.onlineUsers).addChildEventListener();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conversations, container, false);

        activeConversationsAdapter = new ActiveConversationsAdapter(getActivity(), R.layout.online_users_entry, conversationUsersProfiles);

        conversationsView = (ListView) view.findViewById(R.id.conversation_users_list);
        conversationsView.setAdapter(activeConversationsAdapter);

        return view;
    }

}
