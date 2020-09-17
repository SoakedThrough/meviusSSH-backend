package com.meviusssh.backend.utils;


import com.jcraft.jsch.*;
import com.meviusssh.backend.entity.ConnectionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;

@Component
@Slf4j
public class SSHUtils {
    private static final String ENCODING = "UTF-8";
    private static final int timeout = 60 * 60 * 1000;
    private static final int defaultPort = 22;
    public final static String HELLO = "hello";

    @Value("${rsa.url}")
    private String getRsaUrl;

    private static String rsaUrl;

    @PostConstruct
    public void init(){
        rsaUrl = getRsaUrl;
    }

    public static Session getSession(ConnectionInfo connectionInfo) throws JSchException {
        JSch jSch = new JSch();
        Session session;

        String user = connectionInfo.getUser();
        String ip = connectionInfo.getIp();
        int port = connectionInfo.getPort();

        if (port == -1){
            session = jSch.getSession(user,ip,defaultPort);
        }else {
            session = jSch.getSession(user,ip,port);
        }

        String pwd = "";
        String rsaName = "";
        if (connectionInfo.getRsaName()==null){
            pwd = connectionInfo.getPwd();
            session.setPassword(pwd);
        }else {
            session.setConfig(
                    "PreferredAuthentications",
                    "publickey,gssapi-with-mic,keyboard-interactive,password");
            rsaName = rsaUrl + connectionInfo.getRsaName();
            jSch.addIdentity(rsaName);
        }

        session.setConfig("StrictHostKeyChecking", "no");
        session.setTimeout(timeout);
        session.connect();

        File file = new File(rsaName);
        if (file.exists() && file.isFile()){
            file.delete();
        }

        return session;
    }


    public static ChannelShell createChannelShell(Session session) {
        ChannelShell channelShell = null;
        if (SSHContext.getShell(session) == null){
            try {
                Channel channel = session.openChannel("shell");
                channelShell = (ChannelShell) channel;
                //解决终端高亮显示时颜色乱码问题
                channelShell.setPtyType("dump");
                channelShell.setPty(true);
                channelShell.connect();
                SSHContext.addShell(session,channelShell);
                log.info("与" + session.getHost() + "的SHELL通道建立成功");
            } catch (JSchException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return channelShell;
        }else {
            return SSHContext.getShell(session);
        }
    }

    public static void destroyConnection(String key){
        Session session = SSHContext.getSession(key);
        SSHContext.removeChannel(key);
        SSHContext.removeShell(session);
    }

    public static String executeCommand(Session session, String command) throws JSchException, IOException, InterruptedException {
        return executeCommand(session,command,ENCODING);
    }

    public static String executeCommand(Session session, String command, String encoding) throws JSchException, IOException, InterruptedException {
        boolean hFlag = false;
        if (command.equals(HELLO)){
            command = " ";
            hFlag = true;
        }
        ChannelShell channelShell = createChannelShell(session);
        StringBuffer sBuffer = new StringBuffer();
        int beat = 0;
        String result = "";

            // 远端界面返回
            InputStream in = channelShell.getInputStream();
            // 本地内容推送到远端
            OutputStream out = channelShell.getOutputStream();
            // 要执行命令后加换行符才可以执行
            String execCommand = command + "\n";
            log.info("要执行的命令：" + command);
            // 写入执行命令
            out.write(execCommand.getBytes());
            // 清空缓存区，开始执行
            out.flush();
            Thread.sleep(100);
            while (true) {
                if (beat > 3) {
                    break;
                }
                if (in.available() > 0) {
                    //InputStream按位读取，并保存在stringbuffer中
                    byte[] bs =new byte[in.available()];
                    in.read(bs);
                    sBuffer.append(new String(bs));
                    beat ++;
                }else {
                    if (sBuffer.length() >0) {
                        beat++;
                    }
                }
            }
            // 将stringbuff读取的InputStream数据，转换成特定编码格式的字符串，一般为UTF-8格式
            result = new String(sBuffer.toString().getBytes(encoding));

            if (hFlag){
                result = "\n" + result;
            }else {
                result = result.substring(command.length());
            }

        log.info("命令执行完，返回的结果为：" + result);
        return result;
    }

}
