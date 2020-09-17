package com.meviusssh.backend.controller;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.meviusssh.backend.entity.ConnectionInfo;
import com.meviusssh.backend.entity.ReturnMsg;
import com.meviusssh.backend.utils.SSHContext;
import com.meviusssh.backend.utils.SSHUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.xml.ws.RequestWrapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

@RestController
@Slf4j
public class LoginController {

    @Value("${rsa.url}")
    String rsaUrl;

    @CrossOrigin
    @RequestMapping(value = "/loginByPwd",method = RequestMethod.POST)
    public void loginByPwd(@RequestBody JSONObject jsonObject, HttpServletResponse response) throws IOException {


        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.setIp(jsonObject.getString("ip"));
        connectionInfo.setUser(jsonObject.getString("user"));
        connectionInfo.setPwd(jsonObject.getString("pwd"));
        connectionInfo.setTimeout(6000);
        if (jsonObject.getString("port") == null){
            connectionInfo.setPort(-1);
        }else {
            connectionInfo.setPort(Integer.valueOf(jsonObject.getString("port")));
        }
        log.info("connectionInfo:{}",connectionInfo);

        String uuid = "";
        ObjectMapper mapper = new ObjectMapper();

        ReturnMsg returnMsg = new ReturnMsg();
        try {
            uuid = SSHContext.getUUID(connectionInfo);
            Session session = SSHUtils.getSession(connectionInfo);
            SSHContext.addSession(uuid, session);

            returnMsg.setUuid(uuid);
            returnMsg.setSuccess(true);
        }catch (JSchException e) {
            e.printStackTrace();
            returnMsg.setSuccess(false);
        }

        response.getWriter().write(mapper.writeValueAsString(returnMsg));
        response.getWriter().close();
    }

    @CrossOrigin
    @PostMapping("/loginByRsa")
    public String loginByRsa(@RequestParam("file") MultipartFile file, @NotNull String ip, @RequestParam(required = false) Integer port, @NotNull String user){
        try {
            if (file.isEmpty()){
                return "file is empty";
            }
            String fileName = file.getOriginalFilename();
            log.info(fileName);
            String path = rsaUrl + fileName;

            File dest = new File(path);
            if (!dest.getParentFile().exists()){
                dest.getParentFile().mkdirs();
            }
            file.transferTo(dest);

            ConnectionInfo connectionInfo = new ConnectionInfo();
            connectionInfo.setIp(ip);
            connectionInfo.setUser(user);
            connectionInfo.setTimeout(6000);
            connectionInfo.setRsaName(fileName);

            if (port == null){
                connectionInfo.setPort(-1);
            }else {
                connectionInfo.setPort(port);
            }
            log.info("connectionInfo:{}",connectionInfo);

            String uuid = SSHContext.getUUID(connectionInfo);
            Session session = SSHUtils.getSession(connectionInfo);
            SSHContext.addSession(uuid,session);
            return uuid;
        } catch (IOException | JSchException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
