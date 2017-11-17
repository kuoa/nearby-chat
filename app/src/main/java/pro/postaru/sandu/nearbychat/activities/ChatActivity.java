package pro.postaru.sandu.nearbychat.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import pro.postaru.sandu.nearbychat.R;
import pro.postaru.sandu.nearbychat.adapters.ChatAdapter;
import pro.postaru.sandu.nearbychat.models.Conversation;
import pro.postaru.sandu.nearbychat.models.Message;
import pro.postaru.sandu.nearbychat.models.UserProfile;

public class ChatActivity extends AppCompatActivity {

    public static final String PARTNER_ID = "PARTNER_ID";

    private String partnerId;

    private Conversation conversation;

    private ChatAdapter chatAdapter;

    private EditText messageEditView;
    private ImageButton messageSendButton;
    private ListView messageListView;

    private ProgressBar progressBar;

    private UserProfile conversationPartner;


    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        user = auth.getCurrentUser();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_chat);
        setSupportActionBar(toolbar);

        // spinner

        progressBar = (ProgressBar) findViewById(R.id.chat_spinner);
        progressBar.setVisibility(View.VISIBLE);

        partnerId = getIntent().getStringExtra(PARTNER_ID);

        messageEditView = (EditText) findViewById(R.id.message_edit);

        messageSendButton = (ImageButton) findViewById(R.id.message_send);
        messageSendButton.setOnClickListener(v -> sendMessage());

        conversation = new Conversation("id", user.getUid(), partnerId);

        for (int i = 0; i < 20; i++) {
            Message message = new Message();
            message.setText("Message " + i);
            message.setMine(i % 2 == 0);
            conversation.getMessages().add(message);
        }

        chatAdapter = new ChatAdapter(this, R.layout.chat_entry, conversation.getMessages());

        // get conversation partner


        conversationPartner = new UserProfile();
        conversationPartner.setUserName("TESTING");

        // set conversation title
        setTitle(conversationPartner.getUserName());

        // hide keyboard by default
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        messageListView = (ListView) findViewById(R.id.message_list);
        messageListView.setVisibility(View.GONE);

        messageListView.setAdapter(chatAdapter);
        messageListView.setSelection(conversation.getMessages().size() - 1);

    }

    public void sendMessage() {

        String content = messageEditView.getText().toString();
        messageEditView.setText("");

        Message message = new Message();
        message.setMine(true);
        message.setText(content);

        chatAdapter.add(message);
    }
}
