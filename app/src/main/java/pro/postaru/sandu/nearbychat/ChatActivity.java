package pro.postaru.sandu.nearbychat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pro.postaru.sandu.nearbychat.adapters.ChatAdapter;
import pro.postaru.sandu.nearbychat.models.ChatMessage;
import pro.postaru.sandu.nearbychat.models.Endpoint;
import pro.postaru.sandu.nearbychat.models.UserProfile;

public class ChatActivity extends PayloadActivity {

    public static final String CHAT_PARTNER_KEY = "pro.postaru.sandu.nearbychat.CHAT_PARTNER";

    private final SimpleArrayMap<Long, Payload> incomingPayloads = new SimpleArrayMap<>();

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
        if (conversationPartner != null) {
            setTitle(conversationPartner.getUserName());
        }

        // hide keyboard by default
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        currentMessages = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            ChatMessage message = new ChatMessage();
            message.setText("Message " + i);
            message.setMyMessage(i % 2 == 0);
            currentMessages.add(message);
        }

        chatAdapter = new ChatAdapter(this, R.layout.chat_entry, currentMessages);

        ListView messageListView = (ListView) findViewById(R.id.message_list);
        messageListView.setAdapter(chatAdapter);
        messageListView.setSelection(currentMessages.size() - 1);


    }


    public void sendMessage() {
        //send payload here
        String content = messageEditView.getText().toString();
        messageEditView.setText("");

        ChatMessage message = new ChatMessage();
        message.setMyMessage(true);
        message.setText(content);

        chatAdapter.add(message);

        send(Payload.fromBytes(message.getText().getBytes()), conversationPartner.getId());//Send payload to partner
        chatAdapter.notifyDataSetChanged();//update view
    }

    @Override
    protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {
        super.onConnectionInitiated(endpoint, connectionInfo);
        acceptConnection(endpoint);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        super.onConnectionFailed(connectionResult);
        //refuse or cannot connect
    }

    @Override
    protected void acceptConnection(Endpoint endpoint) {
        super.acceptConnection(endpoint);

        //add first paylaod
    }

    @Override
    protected void onReceive(Endpoint endpoint, Payload payload) {
        super.onReceive(endpoint, payload);
        switch (payload.getType()) {
            //Store payload received by id
            case Payload.Type.BYTES:
                incomingPayloads.put(payload.getId(), payload);
                break;
            case Payload.Type.FILE:
                break;
            case Payload.Type.STREAM:
                break;
        }
    }

    @Override
    protected void onUpdate(String endpointId, PayloadTransferUpdate payloadTransferUpdate) {
        super.onUpdate(endpointId, payloadTransferUpdate);
        switch (payloadTransferUpdate.getStatus()) {
            case PayloadTransferUpdate.Status.SUCCESS:
                Payload payload = incomingPayloads.remove(payloadTransferUpdate.getPayloadId());
                if (payload != null) {
                    byte[] bytes = payload.asBytes();
                    if (bytes != null) {
                        ChatMessage chatMessage = getChatMessage(endpointId, payload, bytes);
                        currentMessages.add(chatMessage);
                        chatAdapter.notifyDataSetChanged();
                    }
                }
                break;

        }
    }


    @NonNull
    private ChatMessage getChatMessage(String endpointId, Payload payload, byte[] bytes) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(endpointId);
        chatMessage.setText(new String(bytes));
        chatMessage.setDate(new Date());
        chatMessage.setId(String.valueOf(payload.getId()));
        return chatMessage;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //start =>
        //connectToEndpoint recreate here dest
        connectToEndpoint(new Endpoint(conversationPartner.getId(), conversationPartner.getUserName()));
    }

    @Override
    protected void onStop() {
        super.onStop();
        disconnect(new Endpoint(conversationPartner.getId(), conversationPartner.getUserName()));

    }
}
