package com.meviusssh.backend.entity;

import lombok.Data;

@Data
public class ConnectionInfo {
    private String ip;
    private String pwd;
    private int port;
    private String user;
    private int timeout;
    private String rsaName;

}
