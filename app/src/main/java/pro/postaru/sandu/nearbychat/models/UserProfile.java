package pro.postaru.sandu.nearbychat.models;

import android.graphics.Bitmap;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class UserProfile implements Serializable {

    public UserProfile() {
    }

    ;

    public String id;
    public String userName;
    public String bio;

    public Bitmap avatar;
}
