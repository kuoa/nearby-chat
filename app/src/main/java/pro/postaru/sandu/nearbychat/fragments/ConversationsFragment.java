package pro.postaru.sandu.nearbychat.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import pro.postaru.sandu.nearbychat.R;
import pro.postaru.sandu.nearbychat.adapters.ActiveConversationsAdapter;
import pro.postaru.sandu.nearbychat.models.Conversation;
import pro.postaru.sandu.nearbychat.models.UserProfile;
import pro.postaru.sandu.nearbychat.utils.DatabaseUtils;

public class ConversationsFragment extends Fragment {


    private List<UserProfile> conversationProfiles;
    private ActiveConversationsAdapter activeConversationsAdapter;
    private ListView conversationsView;
    private ProgressBar mainProgresBar;
    private final ValueEventListener getUserProfileListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);

            mainProgresBar.setVisibility(View.GONE);

            if (userProfile != null) {
                activeConversationsAdapter.add(userProfile);
                DatabaseUtils.loadProfileImage(userProfile.getId(), bitmap -> {
                    userProfile.setAvatar(bitmap);
                    activeConversationsAdapter.notifyDataSetChanged();
                });
            }

            Log.w("BBB", "id " + userProfile.getId());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w("BBB", "Canceled profile request");

        }
    };
    private final ChildEventListener userConversationsListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            Conversation conversation = dataSnapshot.getValue(Conversation.class);

            if (conversation != null) {

                DatabaseUtils.getUserProfileReferenceById(conversation.getPartnerId())
                        .addListenerForSingleValueEvent(getUserProfileListener);
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

    public ConversationsFragment() {
    }


    public static ConversationsFragment newInstance() {
        return new ConversationsFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        conversationProfiles = new ArrayList<>();

        mainProgresBar = (ProgressBar) getActivity().findViewById(R.id.online_spinner);
        mainProgresBar.setVisibility(View.VISIBLE);
        DatabaseUtils.getConversationsReferenceById(DatabaseUtils.getCurrentUUID()).addChildEventListener(userConversationsListener);
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
        DatabaseUtils.getConversationsReferenceById(DatabaseUtils.getCurrentUUID())
                .removeEventListener(userConversationsListener);
    }
}
