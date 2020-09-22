package com.meviusssh.backend.utils;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;
import com.meviusssh.backend.entity.ConnectionInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SSHContext {
    private static Map<String, Session> sessionMap = new ConcurrentHashMap<>();
    private static Map<Session, ChannelShell> shellMap = new ConcurrentHashMap<>();
    private static Map<String, Session> channelMap = new ConcurrentHashMap<>();
    private static Map<Session, ChannelSftp> sftpMap = new ConcurrentHashMap<>();

    public static Map<String,Session> getSessionMap(){
        return sessionMap;
    }

    public static void addSftp(Session session,ChannelSftp channelSftp){
        sftpMap.put(session,channelSftp);
    }

    public static ChannelSftp getSftp(Session session){
        return sftpMap.get(session);
    }

    public static void removeSftp(Session session){
        sftpMap.remove(session);
    }

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
