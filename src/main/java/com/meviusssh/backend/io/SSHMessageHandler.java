package com.meviusssh.backend.io;

import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
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

        if (context.length() > 5 && context.substring(0,5).equals("uuid:")){
            String uuid = context.substring(5);
            Session session = SSHContext.getSession(uuid);
            String channelID = channelHandlerContext.channel().id().asShortText();
            SSHContext.addChannel(channelID,session);
            SSHContext.removeSession(uuid);
            if (session == null){
                clients.writeAndFlush(new TextWebSocketFrame("fail connected"));
            }
            String hello = SSHUtils.executeCommand(session,SSHUtils.HELLO);
            clients.writeAndFlush(new TextWebSocketFrame(hello));
        }else{
            Session session = SSHContext.getSessionByChannel(channelHandlerContext.channel().id().asShortText());
            try {
                String res = SSHUtils.executeCommand(session,context);
                if (context.equals("exit")){
                    SSHUtils.destroyConnection(channelHandlerContext.channel().id().asShortText());
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
