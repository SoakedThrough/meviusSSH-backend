package com.meviusssh.backend.entity;

import lombok.Data;

@Data
public class WebSocketMsg {
    private String type;
    private String cookieKey;
    private String content;
}
