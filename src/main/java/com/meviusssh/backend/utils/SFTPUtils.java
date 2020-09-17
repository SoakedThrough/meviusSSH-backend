package com.meviusssh.backend.utils;

import com.jcraft.jsch.*;

import java.io.InputStream;
import java.io.PrintStream;

public class SFTPUtils {

    public static String downloadFile(Session session,String url) throws JSchException, SftpException {
        Channel channel = session.openChannel("sftp");
        channel.connect();

        ChannelSftp channelSftp = (ChannelSftp) channel;

        channelSftp.get(url,"/Users/tyh/Desktop/dest/download");


        return "";
    }
}
