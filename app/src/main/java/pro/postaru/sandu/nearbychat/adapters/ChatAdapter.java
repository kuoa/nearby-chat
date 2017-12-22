package pro.postaru.sandu.nearbychat.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import pro.postaru.sandu.nearbychat.models.Message;
import pro.postaru.sandu.nearbychat.utils.DatabaseUtils;

public class ChatAdapter extends ArrayAdapter<Message> {

    private final Activity activity;

    private final int layoutResource;

    private final List<Message> messages;

    private FirebaseUser user;

    public ChatAdapter(@NonNull Activity activity, @LayoutRes int resource, @NonNull List<Message> messages) {
        super(activity, resource, messages);

        this.activity = activity;
        this.layoutResource = resource;
        this.messages = messages;

        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layoutResource, null);
        }

        Message message = messages.get(position);

        TextView messageView = (TextView) convertView.findViewById(R.id.chat_message);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.chat_image);

        RelativeLayout.LayoutParams params;

        // text
        if (message.getType() == Message.Type.TEXT) {

            params = (RelativeLayout.LayoutParams) messageView.getLayoutParams();

            messageView.setText(message.getContent());
            messageView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
        }
        // image
        else {
            params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();

            String imageUrl = message.getContent();

            StorageReference storageReference = DatabaseUtils.getStorageDatabase()
                    .getReferenceFromUrl(imageUrl);

            DatabaseUtils.loadImage(storageReference,
                    bytes -> {
                        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageView.setImageBitmap(image);

                        imageView.setVisibility(View.VISIBLE);
                        messageView.setVisibility(View.GONE);
                    },
                    null);
        }

        // custom style for a message sent by me
        if (message.getSenderId().equals(user.getUid())) {
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);

            messageView.setBackgroundResource(R.drawable.rounded_corner_sent);
            messageView.setTextColor(Color.BLACK);

        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);

            messageView.setBackgroundResource(R.drawable.rounded_corner_received);
            messageView.setTextColor(Color.WHITE);
        }

        messageView.setPadding(20, 10, 20, 10);
        imageView.setPadding(20, 10, 20, 10);

        return convertView;
    }
}
