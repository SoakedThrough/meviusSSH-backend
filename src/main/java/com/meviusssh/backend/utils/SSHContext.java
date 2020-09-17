package com.meviusssh.backend.utils;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;
import com.meviusssh.backend.entity.ConnectionInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class SSHContext {
    private static Map<String, Session> sessionMap = new HashMap<>();
    private static Map<Session, ChannelShell> shellMap = new HashMap<>();
    private static Map<String,Session> channelMap = new HashMap<>();

    public static void addChannel(String channelID, Session session){
        channelMap.put(channelID,session);
    }

    public static Session getSessionByChannel(String channelID){
        return channelMap.get(channelID);
    }

    public static void removeChannel(String channelId){
        channelMap.remove(channelId);
    }

    public static void addShell(Session session,ChannelShell shell){
        shellMap.put(session,shell);
    }

    public static ChannelShell getShell(Session session){
        return shellMap.get(session);
    }

    public static void removeShell(Session session){
        shellMap.remove(session);
    }

    public static String getUUID(ConnectionInfo connectionInfo){
        String nameSpace = connectionInfo.getIp() + connectionInfo.getUser();
        String key = UUID.nameUUIDFromBytes(nameSpace.getBytes()).toString();
        return key;
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
