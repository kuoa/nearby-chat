package pro.postaru.sandu.nearbychat.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import pro.postaru.sandu.nearbychat.R;
import pro.postaru.sandu.nearbychat.models.ChatMessage;

public class ChatAdapter extends ArrayAdapter<ChatMessage> {

    private final Activity activity;

    private final int layoutResource;

    private final List<ChatMessage> messages;

    public ChatAdapter(@NonNull Activity activity, @LayoutRes int resource, @NonNull List<ChatMessage> messages) {
        super(activity, resource, messages);

        this.activity = activity;
        this.layoutResource = resource;
        this.messages = messages;
    }


    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layoutResource, null);
        }

        ChatMessage message = messages.get(position);

        TextView messageView = (TextView) convertView.findViewById(R.id.chat_message);
        messageView.setText(message.getText());

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) messageView.getLayoutParams();

        // custom style for a message sent by me
        if(message.isMyMessage()){
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            messageView.setBackgroundResource(R.drawable.rounded_corner_sent);
            messageView.setTextColor(Color.BLACK);

        }else{
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            messageView.setBackgroundResource(R.drawable.rounded_corner_received);
            messageView.setTextColor(Color.WHITE);
        }

        messageView.setPadding(20, 10, 20, 10);

        return convertView;
    }

}
