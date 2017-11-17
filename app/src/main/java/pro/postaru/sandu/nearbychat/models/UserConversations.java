package pro.postaru.sandu.nearbychat.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class UserConversations {

    public String id;

    public List<Conversation> conversations;

    // required empty constructor for firebase loading
    public UserConversations(){
    }

    public UserConversations(String id){
        this.id = id;
        conversations = new ArrayList<>();
    }

}
