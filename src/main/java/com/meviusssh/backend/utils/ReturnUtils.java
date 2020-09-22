package com.meviusssh.backend.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meviusssh.backend.entity.ReturnMsg;

public class ReturnUtils {
    static ObjectMapper mapper = new ObjectMapper();

    public static String success(String uuid) throws JsonProcessingException {
        ReturnMsg returnMsg = new ReturnMsg();
        returnMsg.setSuccess(true);
        returnMsg.setUuid(uuid);
        return mapper.writeValueAsString(returnMsg);
    }

    public static String fail() throws JsonProcessingException {
        ReturnMsg returnMsg = new ReturnMsg();
        returnMsg.setSuccess(true);
        return mapper.writeValueAsString(returnMsg);
    }
}
