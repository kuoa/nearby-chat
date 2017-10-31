package pro.postaru.sandu.nearbychat;

import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import pro.postaru.sandu.nearbychat.adapters.ConversationAdapter;
import pro.postaru.sandu.nearbychat.models.ChatMessage;

public class ChatActivity extends AppCompatActivity {

    private List<ChatMessage> currentMessages;

    private ConversationAdapter conversationAdapter;

    private EditText messageEditView;
    private ImageButton messageSendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        messageEditView = (EditText) findViewById(R.id.message_edit);

        messageSendButton = (ImageButton) findViewById(R.id.message_send);
        messageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        currentMessages = new ArrayList<>();

        for(int i = 0; i < 20; i++){
            ChatMessage message = new ChatMessage();
            message.setText("Message " + i);
            message.setMyMessage(i % 2 == 0);
            currentMessages.add(message);
        }

        conversationAdapter = new ConversationAdapter(this, R.layout.chat_entry, currentMessages);

        ListView messageListView = (ListView) findViewById(R.id.message_list);
        messageListView.setAdapter(conversationAdapter);
        messageListView.setSelection(currentMessages.size() - 1);
    }

    public void sendMessage(){

        String content = messageEditView.getText().toString();
        messageEditView.setText("");

        ChatMessage message = new ChatMessage();
        message.setMyMessage(true);
        message.setText(content);

        conversationAdapter.add(message);
    }

}
