package com.meviusssh.backend.io;

import com.meviusssh.backend.utils.SSLUtils;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class WebServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

//        SSLContext sslContext = SSLUtils.createSSLContext("JKS","/Users/tyh/Desktop/dest/wss.jks","mevius");
//        //SSLEngine 此类允许使用ssl安全套接层协议进行安全通信
//        SSLEngine engine = sslContext.createSSLEngine();
//        engine.setUseClientMode(false);

//        pipeline.addLast(new SslHandler(engine));

        pipeline.addLast(new HttpServerCodec());

        pipeline.addLast(new ChunkedWriteHandler());

        pipeline.addLast(new HttpObjectAggregator(1024*64));

        pipeline.addLast(new WebSocketServerProtocolHandler("/webSocket"));

        pipeline.addLast(new SSHMessageHandler());

    }
}
