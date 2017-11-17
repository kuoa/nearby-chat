package pro.postaru.sandu.nearbychat.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

@IgnoreExtraProperties
public class OnlineUser {

    // required empty constructor for firebase loading
    public OnlineUser() {

    }

    private String id;
    private Date date;

    public OnlineUser(String id) {
        this.id = id;
        date = new Date();
    }

    public Date getDate() {
        return date;
    }

    public String getId() {
        return id;
    }
}
