package com.meviusssh.backend.io;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class NettyServer {

    @Value("${server.port}")
    private int port;

    @PostConstruct
    public void start() throws Exception{
        EventLoopGroup mainGroup = new NioEventLoopGroup();
        EventLoopGroup subGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(mainGroup,subGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WebServerInitializer());

            ChannelFuture future = bootstrap.bind(port).sync();
            // 监听关闭的channel，设置位同步方式
            future.channel().closeFuture().sync();
        }finally {
            mainGroup.shutdownGracefully();
            subGroup.shutdownGracefully();
        }
    }
}
