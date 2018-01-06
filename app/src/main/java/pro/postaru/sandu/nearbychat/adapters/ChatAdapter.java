package pro.postaru.sandu.nearbychat.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import pro.postaru.sandu.nearbychat.R;
import pro.postaru.sandu.nearbychat.constants.Constant;
import pro.postaru.sandu.nearbychat.models.Message;
import pro.postaru.sandu.nearbychat.utils.DatabaseUtils;

public class ChatAdapter extends ArrayAdapter<Message> {


    private final List<Message> messages;

    private FirebaseUser user;

    public ChatAdapter(@NonNull Activity activity, List<Message> messages) {
        super(activity,0, messages);

        this.messages = messages;
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public long getItemId(int position) {
        return messages.get(position).getId().hashCode();
    }

    @Override
    public int getViewTypeCount() {
        return Message.Type.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType().ordinal();
    }

    private View getInflatedLayoutForType(int type){

        if(type == Message.Type.TEXT.ordinal()){
            return LayoutInflater.from(getContext()).inflate(R.layout.chat_entry_text, null);
        }else if(type == Message.Type.IMAGE.ordinal()){
            return LayoutInflater.from(getContext()).inflate(R.layout.chat_entry_image, null);
        }
        else if(type == Message.Type.SOUND.ordinal()){
            return LayoutInflater.from(getContext()).inflate(R.layout.chat_entry_sound, null);
        }
        else {
            return null;
        }
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Message message = messages.get(position);

        if (convertView == null) {
            int type = getItemViewType(position);
            convertView = getInflatedLayoutForType(type);
        }


        RelativeLayout.LayoutParams params = null;
        View abstractView = null;

        // text
        if (message.getType() == Message.Type.TEXT) {

            String textContent = (String) message.getContent();

            TextView messageView = (TextView) convertView.findViewById(R.id.chat_message);
            abstractView = messageView;

            params = (RelativeLayout.LayoutParams) messageView.getLayoutParams();

            messageView.setText(textContent);
            messageView.setPadding(20, 10, 20, 10);

            if (message.getSenderId().equals(user.getUid())) {
                messageView.setTextColor(Color.BLACK);
            }
            else{
                messageView.setTextColor(Color.WHITE);
            }
        }

        else if(message.getType() == Message.Type.IMAGE){

            String imageUrl = (String) message.getContent();
            ImageView imageView = (ImageView) convertView.findViewById(R.id.chat_image);
            abstractView = imageView;


            params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();

            imageView.setPadding(20, 10, 20, 10);

            StorageReference storageReference = DatabaseUtils.getStorageDatabase()
                    .getReferenceFromUrl(imageUrl);

            DatabaseUtils.loadImage(storageReference,
                    (Object o) -> {
                        Bitmap avatar = null;
                        if (o instanceof byte[]) {
                            byte[] bytes = (byte[]) o;
                            avatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        }

                        if (o instanceof Bitmap) {
                            avatar = (Bitmap) o;
                        }

                        imageView.setImageBitmap(avatar);

                        imageView.setVisibility(View.VISIBLE);
                    },
                    null);
        }
        //TODO
        else if(message.getType() == Message.Type.SOUND){

        }
        else{
            Log.w(Constant.NEARBY_CHAT, "Wrong type of message");
        }


        // custom style for a message sent by me
        if (message.getSenderId().equals(user.getUid())) {
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);

            abstractView.setBackgroundResource(R.drawable.rounded_corner_sent);

        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);

            abstractView.setBackgroundResource(R.drawable.rounded_corner_received);
        }

        return convertView;
    }
}
