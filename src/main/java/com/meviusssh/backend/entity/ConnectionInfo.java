package com.meviusssh.backend.entity;

import lombok.Data;

@Data
public class ConnectionInfo {
    private String ip;
    private String pwd;
    private int port;
    private String user;
    private int timeout;
    private String command;
    private ConnectType connect;
    private ResolvedResult resolvedResult;

    public enum ConnectType{
        LINK,EXECUTE
    }

    public enum ResolvedResult{
        FAIL,SUCCESS
    }
}
