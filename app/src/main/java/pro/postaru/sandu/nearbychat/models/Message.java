package pro.postaru.sandu.nearbychat.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

@IgnoreExtraProperties
public class Message {

    // required empty constructor for firebase loading
    public Message() {
    }

    public String id;
    public String senderId;
    public String text;
    public Date date;
    public boolean mine;

}
