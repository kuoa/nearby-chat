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
        c1.setPartnerId("XXXFbBK2pGPK7zorutFJrg0XZpL2");

        Conversation c2 = new Conversation();
        c2.setId("c2");
        c2.setOwnerId("XXXFbBK2pGPK7zorutFJrg0XZpL2");
        c2.setPartnerId("RpmFbBK2pGPK7zorutFJrg0XZpL2");

        UserConversations uc1 = new UserConversations();
        uc1.setId("uc1");
        uc1.setOwnerId("RpmFbBK2pGPK7zorutFJrg0XZpL2");
        Map<String, Conversation> uc1Map = new HashMap<>();
        uc1Map.put("XXXFbBK2pGPK7zorutFJrg0XZpL2", c1);
        uc1.setConversations(uc1Map);

        UserConversations uc2 = new UserConversations();
        uc2.setId("uc2");
        uc2.setOwnerId("XXXFbBK2pGPK7zorutFJrg0XZpL2");
        Map<String, Conversation> uc2Map = new HashMap<>();
        uc2Map.put("RpmFbBK2pGPK7zorutFJrg0XZpL2", c2);
        uc2.setConversations(uc2Map);

        List<UserConversations> result = new ArrayList<>();
        result.add(uc1);
        result.add(uc2);

        return result;
    }

    public static List<UserMessages> createDummyUserMessages() {

        Message m1 = new Message();
        m1.setId("m1");
        m1.setSenderId("XXXFbBK2pGPK7zorutFJrg0XZpL2");
        m1.setDate(new Date());
        m1.setText("First message from xx");

        List<Message> msgs1 = new ArrayList<>();
        msgs1.add(m1);

        UserMessages um1 = new UserMessages();
        um1.setId("um1");
        Map<String, List<Message>> umm1 = new HashMap<>();
        umm1.put("XXXFbBK2pGPK7zorutFJrg0XZpL2", msgs1);
        um1.setOwnerId("RpmFbBK2pGPK7zorutFJrg0XZpL2");
        um1.setMessages(umm1);

        Message m2 = new Message();
        m2.setId("m2");
        m2.setSenderId("RpmFbBK2pGPK7zorutFJrg0XZpL2");
        m2.setDate(new Date());
        m2.setText("Reply message from rpm");

        List<Message> msgs2 = new ArrayList<>();
        msgs2.add(m2);

        UserMessages um2 = new UserMessages();
        um2.setId("um2");
        Map<String, List<Message>> umm2 = new HashMap<>();
        umm2.put("RpmFbBK2pGPK7zorutFJrg0XZpL2", msgs2);
        um2.setOwnerId("XXXFbBK2pGPK7zorutFJrg0XZpL2");
        um2.setMessages(umm2);

        List<UserMessages> result = new ArrayList<>();
        result.add(um1);
        result.add(um2);

        return result;
    }

}
