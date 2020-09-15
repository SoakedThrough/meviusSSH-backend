package com.meviusssh.backend.utils;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;
import com.meviusssh.backend.entity.ConnectionInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SSHContext {
    private static Map<String, Session> sessionMap = new HashMap<>();
    private static Map<String,ConnectionInfo> cookieMap = new HashMap<>();
    private static Map<Session, ChannelShell> shellMap = new HashMap<>();

    public static void addShell(Session session,ChannelShell shell){
        shellMap.put(session,shell);
    }

    public static ChannelShell getShell(Session session){
        return shellMap.get(session);
    }

    public static void removeShell(Session session){
        shellMap.remove(session);
    }

    public static String addCookie(ConnectionInfo connectionInfo){
        String nameSpace = connectionInfo.getIp() + connectionInfo.getUser();
        String key = UUID.nameUUIDFromBytes(nameSpace.getBytes()).toString();
        cookieMap.put(key,connectionInfo);
        return key;
    }

    public static void removeCookie(String key){
        cookieMap.remove(key);
    }

    public static ConnectionInfo getCookie(String key){
        return cookieMap.get(key);
    }

    public static void addSession(String key, Session session){
        sessionMap.put(key, session);
    }

    public static void removeSession(String key){
        sessionMap.remove(key);
    }

    public static Session getSession(String key){
        return sessionMap.get(key);
    }
}
