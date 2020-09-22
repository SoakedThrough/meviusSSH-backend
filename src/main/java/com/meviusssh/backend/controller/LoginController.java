package com.meviusssh.backend.controller;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.meviusssh.backend.entity.ConnectionInfo;
import com.meviusssh.backend.entity.ReturnMsg;
import com.meviusssh.backend.utils.ReturnUtils;
import com.meviusssh.backend.utils.SFTPUtils;
import com.meviusssh.backend.utils.SSHContext;
import com.meviusssh.backend.utils.SSHUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import java.io.File;
import java.io.IOException;

@RestController
@Slf4j
public class LoginController {

    @Value("${rsa.url}")
    String rsaUrl;

    @CrossOrigin
//    @Async
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

        try {
            uuid = SSHUtils.getUUID(connectionInfo);
            Session session = SSHUtils.getSession(connectionInfo);
            SSHContext.addSession(uuid, session);

           response.getWriter().write(ReturnUtils.success(uuid));
        }catch (JSchException e) {
            e.printStackTrace();
            response.getWriter().write(ReturnUtils.fail());
        }finally {
            response.getWriter().close();
        }
    }

    @CrossOrigin
//    @Async
    @PostMapping("/loginByRsa")
    public void loginByRsa(@RequestParam("file") MultipartFile file, @NotNull String ip,
                           @RequestParam(required = false) Integer port, @NotNull String user, HttpServletResponse response) throws IOException {
        try {
            if (file.isEmpty()){
                response.getWriter().write(ReturnUtils.fail());
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

            String uuid = SSHUtils.getUUID(connectionInfo);
            Session session = SSHUtils.getSession(connectionInfo);
            SSHContext.addSession(uuid,session);

            if (dest.exists() && dest.isFile()){
                dest.delete();
            }

            response.getWriter().write(ReturnUtils.success(uuid));
        } catch (IOException | JSchException e) {
            e.printStackTrace();
            response.getWriter().write(ReturnUtils.fail());
        }finally {
            response.getWriter().close();
        }
    }

}
