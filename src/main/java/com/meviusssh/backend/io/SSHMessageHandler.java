package com.meviusssh.backend.io;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.meviusssh.backend.entity.ConnectionInfo;
import com.meviusssh.backend.entity.WebSocketMsg;
import com.meviusssh.backend.utils.SSHContext;
import com.meviusssh.backend.utils.SSHUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
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
        JSONObject msg = JSON.parseObject(context);
        if (msg.getString("type").equals("login")){
            ConnectionInfo resolvedMsg = SSHContext.getCookie(msg.getString("cookieKey"));

            if (resolvedMsg == null){
                log.info("解析失败");
                clients.writeAndFlush(new TextWebSocketFrame("fail connected"));
            }else {
                try {
                    Session session = SSHUtils.getSession(resolvedMsg);
                    SSHContext.addSession(msg.getString("cookieKey"),session);
                    clients.writeAndFlush(new TextWebSocketFrame("success"));
                    SSHContext.removeCookie(msg.getString("cookieKey"));
                }catch (JSchException e){
                    e.printStackTrace();
                    clients.writeAndFlush(new TextWebSocketFrame(e.getMessage()));
                }
            }
        }else if (msg.getString("type").equals("exec")){
            try {
                Session session = SSHContext.getSession(msg.getString("cookieKey"));
                String res = SSHUtils.executeCommand(session,msg.getString("content"));
                if (msg.getString("content").equals("exit")){
                    SSHUtils.destroyConnection(msg.getString("cookieKey"));
                }
                clients.writeAndFlush(new TextWebSocketFrame(res));
            }catch (JSchException e){
                e.printStackTrace();
                clients.writeAndFlush(new TextWebSocketFrame(e.getMessage()));
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
        SSHContext.removeSession(ctx.channel().id().asShortText());
        log.info("关闭连接-channelID:"+ctx.channel().id().asShortText());
        clients.remove(ctx.channel());
    }
}
