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


    public Conversation() {
        messages = new ArrayList<>();
    }


    public Conversation(String id, String ownerId, String partnerId) {
        this();
        this.id = id;
        this.ownerId = ownerId;
        this.partnerId = partnerId;
    }

    public List<Message> getMessages() {
        return messages;
    }
}
