package pro.postaru.sandu.nearbychat.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import pro.postaru.sandu.nearbychat.R;
import pro.postaru.sandu.nearbychat.adapters.ChatAdapter;
import pro.postaru.sandu.nearbychat.models.Conversation;
import pro.postaru.sandu.nearbychat.models.Message;
import pro.postaru.sandu.nearbychat.models.UserProfile;


public class ChatFragment extends Fragment {

    private static final String PARTNER_ID = "PARTNER_ID";

    private String partnerId;

    private Conversation conversation;

    private ChatAdapter chatAdapter;

    private EditText messageEditView;
    private ImageButton messageSendButton;
    private ListView messageListView;

    private UserProfile conversationPartner;

    public ChatFragment() {
        // Required empty public constructor
    }


    public static ChatFragment newInstance(String partnerId) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(PARTNER_ID, partnerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            partnerId = getArguments().getString(PARTNER_ID);
        }

        conversation = new Conversation();

        for (int i = 0; i < 20; i++) {
            Message message = new Message();
            message.text = "Message " + i;
            message.mine = (i % 2 == 0);
            conversation.getMessages().add(message);
        }

        chatAdapter = new ChatAdapter(getActivity(), R.layout.chat_entry, conversation.getMessages());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        messageEditView = (EditText) view.findViewById(R.id.message_edit);

        messageSendButton = (ImageButton) view.findViewById(R.id.message_send);
        messageSendButton.setOnClickListener(v -> sendMessage());

        // get conversation partner
        conversationPartner = new UserProfile();
        conversationPartner.userName = "TESTING";

        // set conversation title
        getActivity().setTitle(conversationPartner.userName);

        // hide keyboard by default
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        messageListView = (ListView) view.findViewById(R.id.message_list);

        messageListView.setAdapter(chatAdapter);
        messageListView.setSelection(conversation.getMessages().size() - 1);

        return view;
    }

    public void sendMessage() {

        String content = messageEditView.getText().toString();
        messageEditView.setText("");

        Message message = new Message();
        message.mine = true;
        message.text = content;

        chatAdapter.add(message);
    }
}
