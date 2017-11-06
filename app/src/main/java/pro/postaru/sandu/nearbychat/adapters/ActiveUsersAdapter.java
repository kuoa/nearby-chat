package pro.postaru.sandu.nearbychat.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import pro.postaru.sandu.nearbychat.ChatActivity;
import pro.postaru.sandu.nearbychat.R;
import pro.postaru.sandu.nearbychat.models.UserProfile;

public class ActiveUsersAdapter extends ArrayAdapter<UserProfile> {

    private final Activity activity;

    private final int layoutResource;

    private final List<UserProfile> userProfileList;

    public ActiveUsersAdapter(@NonNull Activity activity, @LayoutRes int resource, @NonNull List<UserProfile> userProfiles) {
        super(activity, resource, userProfiles);

        this.activity = activity;
        this.layoutResource = resource;
        this.userProfileList = userProfiles;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layoutResource, null);
        }

        final UserProfile user = userProfileList.get(position);

        TextView userName = (TextView) convertView.findViewById(R.id.active_user_name);
        TextView userBio = (TextView) convertView.findViewById(R.id.active_user_bio);
        ImageView userAvatar = (ImageView) convertView.findViewById(R.id.active_user_avatar);

        userName.setText(user.getUserName());
        userBio.setText(user.getBio());
        userAvatar.setImageBitmap(user.getAvatar());

        convertView.setOnClickListener(v -> launchConversationWithUser(user));

        return convertView;
    }

    private void launchConversationWithUser(UserProfile user) {

        Intent intent = new Intent(activity, ChatActivity.class);

        // conversation partner
        intent.putExtra(ChatActivity.CHAT_PARTNER_KEY, user);

        activity.startActivity(intent);
    }
}
