package pro.postaru.sandu.nearbychat.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class UserMessages {

    private String id;
    private String ownerId;
    private Map<String, List<Message>> messages;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Map<String, List<Message>> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, List<Message>> messages) {
        this.messages = messages;
    }
}
