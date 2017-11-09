package pro.postaru.sandu.nearbychat.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import pro.postaru.sandu.nearbychat.R;
import pro.postaru.sandu.nearbychat.models.UserProfile;

public class OnlineUsersAdapter extends ArrayAdapter<UserProfile> {

    private final Context context;

    private final int layoutResource;

    private final List<UserProfile> userProfileList;

    public OnlineUsersAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<UserProfile> userProfiles) {
        super(context, resource, userProfiles);

        this.context = context;
        this.layoutResource = resource;
        this.userProfileList = userProfiles;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layoutResource, null);
        }

        final UserProfile user = userProfileList.get(position);

        TextView userName = (TextView) convertView.findViewById(R.id.active_user_name);
        TextView userBio = (TextView) convertView.findViewById(R.id.active_user_bio);
        ImageView userAvatar = (ImageView) convertView.findViewById(R.id.active_user_avatar);

        userName.setText(user.userName);
        userBio.setText(user.bio);
        userAvatar.setImageBitmap(user.avatar);

        convertView.setOnClickListener(v -> launchConversationWithUser(user));

        return convertView;
    }

    private void launchConversationWithUser(UserProfile user){
        Log.d("DDD", "Conversation launched");
    }
}
