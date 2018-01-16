package pro.postaru.sandu.nearbychat.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import pro.postaru.sandu.nearbychat.R;
import pro.postaru.sandu.nearbychat.constants.Constant;
import pro.postaru.sandu.nearbychat.models.Message;
import pro.postaru.sandu.nearbychat.utils.DatabaseUtils;

public class ChatAdapter extends ArrayAdapter<Message> {

    private final List<Message> messages;

    private FirebaseUser user;

    private Activity activity;

    private MediaPlayer mediaPlayer;

    private SimpleDateFormat dateFormat;

    public ChatAdapter(@NonNull Activity activity, List<Message> messages) {
        super(activity, 0, messages);

        this.activity = activity;
        this.messages = messages;

        dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

        user = FirebaseAuth.getInstance().getCurrentUser();
        mediaPlayer = new MediaPlayer();
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

    private View getInflatedLayoutForType(int type) {

        if (type == Message.Type.TEXT.ordinal()) {
            return LayoutInflater.from(getContext()).inflate(R.layout.chat_entry_text, null);
        } else if (type == Message.Type.IMAGE.ordinal()) {
            return LayoutInflater.from(getContext()).inflate(R.layout.chat_entry_image, null);
        } else if (type == Message.Type.SOUND.ordinal()) {
            return LayoutInflater.from(getContext()).inflate(R.layout.chat_entry_sound, null);
        } else {
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


        RelativeLayout.LayoutParams mainViewParams = null;
        View abstractView = null;

        // Text
        if (message.getType() == Message.Type.TEXT) {

            String textContent = (String) message.getContent();

            TextView messageView = (TextView) convertView.findViewById(R.id.chat_message);
            abstractView = messageView;

            messageView.setText(textContent);
            //messageView.setPadding(20, 10, 20, 10);

            if (message.getSenderId().equals(user.getUid())) {
                messageView.setTextColor(Color.BLACK);
            } else {
                messageView.setTextColor(Color.WHITE);
            }

            mainViewParams = (RelativeLayout.LayoutParams) messageView.getLayoutParams();

            // Image
        } else if (message.getType() == Message.Type.IMAGE) {

            String imageUrl = (String) message.getContent();
            ImageView imageView = (ImageView) convertView.findViewById(R.id.chat_image);
            abstractView = imageView;


            mainViewParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();

            //imageView.setPadding(20, 10, 20, 10);

            StorageReference storageReference = DatabaseUtils.getStorageDatabase()
                    .getReferenceFromUrl(imageUrl);

            DatabaseUtils.loadImage(storageReference,
                    bitmap -> {
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        }
                    },
                    null);
        }

        // Sound recording
        else if (message.getType() == Message.Type.SOUND) {
            String recordUrl = (String) message.getContent();
            ImageButton button = (ImageButton) convertView.findViewById(R.id.chat_audio);
            abstractView = button;

            mainViewParams = (RelativeLayout.LayoutParams) button.getLayoutParams();

            button.setPadding(20, 10, 20, 10);

            StorageReference storageReference = DatabaseUtils.getStorageDatabase()
                    .getReferenceFromUrl(recordUrl);

            DatabaseUtils.loadRecord(storageReference, message.getId(),
                    audioRecordInputStream -> {

                        if (audioRecordInputStream != null) {

                            //event for on click
                            button.setOnClickListener(v -> {
                                if (!mediaPlayer.isPlaying()) {
                                    try {
                                        button.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_stop_black_24px));

                                        mediaPlayer.reset();

                                        mediaPlayer.setDataSource(audioRecordInputStream.getFD());

                                        mediaPlayer.prepare();
                                        mediaPlayer.start();

                                        mediaPlayer.setOnCompletionListener(mp -> button.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_play_arrow_black_24px)));

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    button.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_play_arrow_black_24px));
                                    mediaPlayer.stop();
                                }
                            });


                        }
                    },
                    null, activity);

        } else {
            Log.w(Constant.NEARBY_CHAT, "Wrong type of message");
        }

        TextView dateView = (TextView) convertView.findViewById(R.id.chat_date);
        String dateText = dateFormat.format(message.getDate());
        dateView.setText(dateText);

        RelativeLayout.LayoutParams dateViewParams = (RelativeLayout.LayoutParams) dateView.getLayoutParams();

        // custom style for a message sent by me
        if (message.getSenderId().equals(user.getUid())) {
            mainViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            mainViewParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);

            dateViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            dateViewParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);

            abstractView.setBackgroundResource(R.drawable.rounded_corner_sent);

        } else {
            mainViewParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            mainViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);

            dateViewParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            dateViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);

            abstractView.setBackgroundResource(R.drawable.rounded_corner_received);
        }

        abstractView.setPadding(20, 10, 20, 10);

        return convertView;
    }
}
