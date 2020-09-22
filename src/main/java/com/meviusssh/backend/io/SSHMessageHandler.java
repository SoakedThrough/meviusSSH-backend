package com.meviusssh.backend.io;

import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.meviusssh.backend.utils.MeviusChannelMatcher;
import com.meviusssh.backend.utils.SSHContext;
import com.meviusssh.backend.utils.SSHUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SSHMessageHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        String context = textWebSocketFrame.text();

        MeviusChannelMatcher channelMatcher = new MeviusChannelMatcher(channelHandlerContext.channel().id().asShortText());

        if (context.length() > 5 && context.substring(0,5).equals("uuid:")){
            String uuid = context.substring(5);
            Session session = SSHContext.getSession(uuid);
            String channelID = channelHandlerContext.channel().id().asShortText();
            SSHContext.addChannel(channelID,session);
            if (session == null){
                clients.writeAndFlush(new TextWebSocketFrame("fail connected"),channelMatcher);
            }
            String hello = SSHUtils.executeCommand(session,SSHUtils.HELLO);
            clients.writeAndFlush(new TextWebSocketFrame(hello),channelMatcher);


        }else if (context.length() >= 3 && context.substring(0,3).equals("mup")){
            if (context.length() == 3 || context.substring(4).startsWith("./")){
                Session session = SSHContext.getSessionByChannel(channelHandlerContext.channel().id().asShortText());
                try {
                    String res = SSHUtils.executeCommand(session,"pwd");
                    String[] tmp = res.split("\r\n");
                    res = "meviusUpload:success:" + tmp[1] + "/";
                    clients.writeAndFlush(new TextWebSocketFrame(res),channelMatcher);
                }catch (JSchException e){
                    e.printStackTrace();
                    clients.writeAndFlush(new TextWebSocketFrame("meviusUpload:fail:"),channelMatcher);
                }
            }else {
                clients.writeAndFlush(new TextWebSocketFrame(context.substring(4)),channelMatcher);
            }


        }else if (context.length() > 5 && context.substring(0,5).equals("mdown")){
            if (context.substring(6).startsWith("./")){
                Session session = SSHContext.getSessionByChannel(channelHandlerContext.channel().id().asShortText());
                try{
                    String res = SSHUtils.executeCommand(session,"pwd");
                    String[] tmp = res.split("\r\n");
                    res = "meviusDownload:success:" + tmp[1] + "/";
                    tmp = context.split("/");
                    res = res + tmp[tmp.length-1];
                    clients.writeAndFlush(new TextWebSocketFrame(res),channelMatcher);
                }catch (JSchException e){
                    e.printStackTrace();
                    clients.writeAndFlush(new TextWebSocketFrame("meviusDownload:fail:"),channelMatcher);
                }
            }else {
                clients.writeAndFlush(new TextWebSocketFrame("meviusDownload:success:"+context.substring(6)),channelMatcher);
            }

        }

        else if (context.endsWith("%")){
            String reg = context.substring(0,context.length()-1);
            Session session = SSHContext.getSessionByChannel(channelHandlerContext.channel().id().asShortText());
            try{
                String res = SSHUtils.executeCommand(session,"k\t\t");
                String[] list = res.split("\r\n");
                int flag = 0;
                for (int i = 0; i < list.length-1; i++) {
                    if (list[i].equals("")){
                        continue;
                    }
                    String[] tmp = list[i].split(" ");
                    for (int j = 0; j < tmp.length; j++) {
                        if (tmp[j].startsWith(reg)){
                            if (flag++ > 1){
                                break;
                            }
                            res = tmp[j];
                        }
                    }
                }

                res = res.substring(context.length()-1);
                if (flag > 1){
                    clients.writeAndFlush(new TextWebSocketFrame("meviusMatch:fail:"));
                }else {
                    clients.writeAndFlush(new TextWebSocketFrame("meviusMatch:success:"+res),channelMatcher);
                }
            }catch (JSchException e){
                e.printStackTrace();
                clients.writeAndFlush(new TextWebSocketFrame("meviusMatch:fail:"));
            }
        }

        else{
            Session session = SSHContext.getSessionByChannel(channelHandlerContext.channel().id().asShortText());
            try {
                String res = SSHUtils.executeCommand(session,context);
                if (context.equals("exit")){
                    SSHUtils.destroyConnection(channelHandlerContext.channel().id().asShortText());
                }
                clients.writeAndFlush(new TextWebSocketFrame(res),channelMatcher);
            }catch (JSchException e){
                e.printStackTrace();
                clients.writeAndFlush(new TextWebSocketFrame(e.getMessage()),channelMatcher);
            }
        }
    }

    protected SSHMessageHandler() {
        super();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("建立连接-channelID:"+ctx.channel().id().asShortText());
        clients.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        SSHUtils.destroyConnection(ctx.channel().id().asShortText());
        log.info("关闭连接-channelID:"+ctx.channel().id().asShortText());
        clients.remove(ctx.channel());
    }
}
