package pro.postaru.sandu.nearbychat.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

@IgnoreExtraProperties
public class OnlineUser {

    private Date date;

    public OnlineUser() {
        date = new Date();
    }

    public Date getDate() {
        return date;
    }

}
