package pro.postaru.sandu.nearbychat.utils;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pro.postaru.sandu.nearbychat.models.Conversation;
import pro.postaru.sandu.nearbychat.models.Message;
import pro.postaru.sandu.nearbychat.models.UserConversations;
import pro.postaru.sandu.nearbychat.models.UserMessages;

public class Utils {

    public static List<UserConversations> createDummyUserConversations() {

        Conversation c1 = new Conversation();
        c1.setId("c1");
        c1.setOwnerId("RpmFbBK2pGPK7zorutFJrg0XZpL2");
        c1.setPartnerId("D27bUhQl1JfL3FUlpPsqr0ktNNu1");

        Conversation c2 = new Conversation();
        c2.setId("c2");
        c2.setOwnerId("D27bUhQl1JfL3FUlpPsqr0ktNNu1");
        c2.setPartnerId("RpmFbBK2pGPK7zorutFJrg0XZpL2");

        UserConversations uc1 = new UserConversations();
        uc1.setId("uc1");
        uc1.setOwnerId("RpmFbBK2pGPK7zorutFJrg0XZpL2");
        Map<String, Conversation> uc1Map = new HashMap<>();
        uc1Map.put("D27bUhQl1JfL3FUlpPsqr0ktNNu1", c1);
        uc1.setConversations(uc1Map);

        UserConversations uc2 = new UserConversations();
        uc2.setId("uc2");
        uc2.setOwnerId("D27bUhQl1JfL3FUlpPsqr0ktNNu1");
        Map<String, Conversation> uc2Map = new HashMap<>();
        uc2Map.put("RpmFbBK2pGPK7zorutFJrg0XZpL2", c2);
        uc2.setConversations(uc2Map);

        List<UserConversations> result = new ArrayList<>();
        result.add(uc1);
        result.add(uc2);

        return result;
    }

    public static UserMessages createDummyUserMessages() {

        Message m1 = new Message();
        m1.setId("m1");
        m1.setSenderId("XXXFbBK2pGPK7zorutFJrg0XZpL2");
        m1.setDate(new Date());
        m1.setText("First message from xx");

        Message m2 = new Message();
        m2.setId("m2");
        m2.setSenderId("RpmFbBK2pGPK7zorutFJrg0XZpL2");
        m2.setDate(new Date());
        m2.setText("Reply message from rpm");

        List<Message> messages = new ArrayList<>();
        messages.add(m1);
        messages.add(m2);

        UserMessages um1 = new UserMessages();
        um1.setId("RpmFbBK2pGPK7zorutFJrg0XZpL2-XXXFbBK2pGPK7zorutFJrg0XZpL2");
        um1.setMessages(messages);

        return um1;
    }

}
