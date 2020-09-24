package com.meviusssh.backend.utils;

import com.jcraft.jsch.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;


@Component
public class SFTPUtils {

    @Value("${sftp.download.url}")
    private String getDownloadUrl;

    @Value("${sftp.upload.url}")
    private String getUploadUrl;

    private static String downloadUrl;
    private static String uploadUrl;

    @PostConstruct
    public void init(){
        downloadUrl = getDownloadUrl;
        uploadUrl = getUploadUrl;
    }

    public static ChannelSftp createSftpChannel(Session session) throws JSchException {
        ChannelSftp channelSftp = SSHContext.getSftp(session);
        if (channelSftp == null){
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            SSHContext.addSftp(session,channelSftp);
            channelSftp.connect();
        }
        return channelSftp;
    }

    public static File downloadFile(Session session, String uuid, String remoteFilePath, String fileName) throws JSchException, SftpException {

        ChannelSftp channelSftp = createSftpChannel(session);
        String localPath = downloadUrl + uuid + "-" + fileName;

        channelSftp.get(remoteFilePath,localPath);

        File file = new File(localPath);

        return file;
    }

    public static void uploadFile(Session session, String url, String localFileName, String filename) throws JSchException, SftpException {

        ChannelSftp channelSftp = createSftpChannel(session);
        String remotePath = url + filename;
        String localPath = uploadUrl  + localFileName;

        channelSftp.put(localPath,remotePath);

    }
}
