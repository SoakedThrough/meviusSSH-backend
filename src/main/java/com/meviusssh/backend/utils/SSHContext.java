package com.meviusssh.backend.utils;

import com.jcraft.jsch.Session;

import java.util.HashMap;
import java.util.Map;

public class SSHContext {
    private static Map<String, Session> sessionMap = new HashMap<>();

    public static void addSession(String channelId, Session session){
        sessionMap.put(channelId, session);
    }

    public static void removeSession(String channelId){
        sessionMap.remove(channelId);
    }

    public static Session getSession(String channelId){
        return sessionMap.get(channelId);
    }
}
