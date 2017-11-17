package pro.postaru.sandu.nearbychat.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class Conversation {

    private String id;
    private String ownerId;
    private String partnerId;

    private List<Message> messages;

    // required empty constructor for firebase loading
    public Conversation() {
    }


    public Conversation(String id, String ownerId, String partnerId) {

        this.id = id;
        this.ownerId = ownerId;
        this.partnerId = partnerId;
        this.messages = new ArrayList<>();
    }

    public List<Message> getMessages() {
        return messages;
    }
}
