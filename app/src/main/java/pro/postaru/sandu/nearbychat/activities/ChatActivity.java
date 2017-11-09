package pro.postaru.sandu.nearbychat.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import pro.postaru.sandu.nearbychat.R;
import pro.postaru.sandu.nearbychat.adapters.ChatAdapter;
import pro.postaru.sandu.nearbychat.models.ChatMessage;
import pro.postaru.sandu.nearbychat.models.UserProfile;

public class ChatActivity extends AppCompatActivity {

    public static final String CHAT_PARTNER_KEY = "pro.postaru.sandu.nearbychat.CHAT_PARTNER";

    private List<ChatMessage> currentMessages;

    private ChatAdapter chatAdapter;

    private EditText messageEditView;
    private ImageButton messageSendButton;

    private UserProfile conversationPartner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageEditView = (EditText) findViewById(R.id.message_edit);

        messageSendButton = (ImageButton) findViewById(R.id.message_send);
        messageSendButton.setOnClickListener(v -> sendMessage());

        // get conversation partner
        conversationPartner = (UserProfile) getIntent().getExtras().get(CHAT_PARTNER_KEY);

        // set conversation title
        setTitle(conversationPartner.userName);

        // hide keyboard by default
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        currentMessages = new ArrayList<>();

        for(int i = 0; i < 20; i++){
            ChatMessage message = new ChatMessage();
            message.text = "Message " + i;
            message.mine = (i % 2 == 0);
            currentMessages.add(message);
        }

        chatAdapter = new ChatAdapter(this, R.layout.chat_entry, currentMessages);

        ListView messageListView = (ListView) findViewById(R.id.message_list);
        messageListView.setAdapter(chatAdapter);
        messageListView.setSelection(currentMessages.size() - 1);
    }

    public void sendMessage(){

        String content = messageEditView.getText().toString();
        messageEditView.setText("");

        ChatMessage message = new ChatMessage();
        message.mine = true;
        message.text = content;

        chatAdapter.add(message);
    }

}
