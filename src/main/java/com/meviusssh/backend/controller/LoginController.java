package com.meviusssh.backend.controller;

import com.meviusssh.backend.entity.ConnectionInfo;
import com.meviusssh.backend.utils.SSHContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@Slf4j
public class LoginController {

    @Value("${rsa.url}")
    String rsaUrl;

    @RequestMapping("/loginByPwd")
    public String loginByPwd(String ip, String user, String pwd, @RequestParam(required = false) Integer port){
        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.setIp(ip);
        connectionInfo.setUser(user);
        connectionInfo.setPwd(pwd);
        connectionInfo.setTimeout(6000);
        if (port == null){
            connectionInfo.setPort(-1);
        }else {
            connectionInfo.setPort(port);
        }
        log.info("connectionInfo:{}",connectionInfo);

        return SSHContext.addCookie(connectionInfo);
    }

    @RequestMapping("/loginByRsa")
    public String loginByRsa(@RequestParam("file") MultipartFile file, String ip, @RequestParam(required = false) Integer port, String user){
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
            return SSHContext.addCookie(connectionInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "fail";
    }
}
