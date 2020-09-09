package com.meviusssh.backend.utils;


import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.meviusssh.backend.entity.ConnectionInfo;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class SSHUtils {
    private static final String ENCODING = "UTF-8";
    private static final int timeout = 60 * 60 * 1000;
    private static final int defaultPort = 22;

    public static Session getSession(ConnectionInfo connectionInfo) throws JSchException {
        JSch jSch = new JSch();
        String user = connectionInfo.getUser();
        String ip = connectionInfo.getIp();
        int port = connectionInfo.getPort();
        String pwd = connectionInfo.getPwd();
        Session session = jSch.getSession(user,ip,port);
        session.setPassword(pwd);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setTimeout(timeout);
        session.connect();

        return session;
    }

    public static String executeCommand(Session session, String command) throws IOException, JSchException {
        return executeCommand(session,command,ENCODING);
    }

    public static String executeCommand(Session session, String command, String encoding) throws JSchException, IOException {
        ChannelExec exec = (ChannelExec) session.openChannel("exec");
        InputStream in = exec.getInputStream();
        exec.setCommand(command);
        exec.setErrStream(System.err);
        exec.connect();

        String result = IOUtils.toString(in,encoding);
        return result;

    }

    public static ConnectionInfo getConnect(String sshConnectMsg){

        // TODO: 2020/9/9 解析连接信息，加密技术
        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.setResolvedResult(ConnectionInfo.ResolvedResult.FAIL);

        return connectionInfo;
    }
}
