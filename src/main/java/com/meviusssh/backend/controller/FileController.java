package com.meviusssh.backend.controller;

import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.meviusssh.backend.utils.ReturnUtils;
import com.meviusssh.backend.utils.SFTPUtils;
import com.meviusssh.backend.utils.SSHContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

@RestController
@Slf4j
public class FileController {

    @Value("${sftp.download.url}")
    String sftpDownloadUrl;

    @Value("${sftp.upload.url}")
    String sftpUploadUrl;

    @CrossOrigin
//    @Async
    @RequestMapping("/downloadFile")
    public void downloadFile(@RequestBody JSONObject jsonObject, HttpServletResponse response) throws IOException {
        String remoteFilePath = jsonObject.getString("path");
        String uuid = jsonObject.getString("uuid");

        String[] tmp = remoteFilePath.split("/");
        String fileName = tmp[tmp.length-1];

        FileInputStream inputStream = null;
        BufferedInputStream bufferedInputStream = null;
        OutputStream outputStream = null;

        try {
            Session session = SSHContext.getSession(uuid);
            File file = SFTPUtils.downloadFile(session,uuid,remoteFilePath,fileName);
            log.info("获得文件：{}",file.getName());
            if (file != null){
                response.setHeader("content-type", "application/octet-stream");
                response.setContentType("application/octet-stream");
                // 下载文件能正常显示中文
                response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(uuid + "-" + fileName, "UTF-8"));

                byte[] buffer = new byte[1024];
                inputStream = new FileInputStream(file);
                bufferedInputStream = new BufferedInputStream(inputStream);
                outputStream = response.getOutputStream();

                int i = bufferedInputStream.read(buffer);
                while (i != -1) {
                    outputStream.write(buffer, 0, i);
                    i = bufferedInputStream.read(buffer);
                }
                log.info("下载成功");
            }
        } catch (JSchException | SftpException | IOException e) {
            e.printStackTrace();
        }finally {
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @CrossOrigin
//    @Async
    @RequestMapping(value = "/uploadFile",method = RequestMethod.POST)
    public void uploadFile(@RequestParam("file") MultipartFile localFile,String uuid, String path, HttpServletResponse response) throws IOException {

        try {
            if (localFile.isEmpty()) {
                response.getWriter().write(ReturnUtils.fail());
            }
            String localFileName = uuid + "-" + localFile.getOriginalFilename();
            log.info("要上传的文件名：{}",localFileName);
            String localPath = sftpUploadUrl + localFileName;

            File dest = new File(localPath);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            localFile.transferTo(dest);

            Session session = SSHContext.getSession(uuid);
            SFTPUtils.uploadFile(session,path,localFileName,localFile.getOriginalFilename());

            if (dest.exists() && dest.isFile()){
                dest.delete();
            }
        } catch (JSchException | SftpException | IOException e) {
            e.printStackTrace();
            response.getWriter().write(ReturnUtils.fail());
        }finally {
            response.getWriter().close();
        }
    }
}
