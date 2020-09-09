package com.meviusssh.backend.io;

import com.jcraft.jsch.Session;
import com.meviusssh.backend.entity.ConnectionInfo;
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
        ConnectionInfo resolvedMsg = SSHUtils.getConnect(context);
        //解析成功
        if (resolvedMsg.getResolvedResult().equals(ConnectionInfo.ResolvedResult.SUCCESS)){
            //连接操作
            if (resolvedMsg.getConnect().equals(ConnectionInfo.ConnectType.LINK)){
                Session session = SSHUtils.getSession(resolvedMsg);
                SSHContext.addSession(channelHandlerContext.channel().id().asShortText(),session);
                clients.writeAndFlush(new TextWebSocketFrame("success"));
            }

            //执行操作
            if (resolvedMsg.getConnect().equals(ConnectionInfo.ConnectType.EXECUTE)){
                Session session = SSHContext.getSession(channelHandlerContext.channel().id().asShortText());
                String res = SSHUtils.executeCommand(session,resolvedMsg.getCommand());
                clients.writeAndFlush(new TextWebSocketFrame(res));
            }
        }

        if (resolvedMsg.getResolvedResult().equals(ConnectionInfo.ResolvedResult.FAIL)){
            clients.writeAndFlush(new TextWebSocketFrame("fail"));
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
