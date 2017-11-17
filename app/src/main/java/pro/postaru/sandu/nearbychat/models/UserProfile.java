package pro.postaru.sandu.nearbychat.models;

import android.graphics.Bitmap;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class UserProfile implements Serializable {

    // required empty constructor for firebase loading
    public UserProfile() {
    }

    public String id;
    public String userName;
    public String bio;

    public Bitmap avatar;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserProfile that = (UserProfile) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
