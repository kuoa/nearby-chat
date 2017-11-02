package pro.postaru.sandu.nearbychat.models;

import android.graphics.Bitmap;

import java.io.Serializable;

public class UserProfile implements Serializable {

    private String id;
    private String userName;
    private String bio;

    private Bitmap avatar;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Bitmap getAvatar() {
        return avatar;
    }

    public void setAvatar(Bitmap avatar) {
        this.avatar = avatar;
    }
}
