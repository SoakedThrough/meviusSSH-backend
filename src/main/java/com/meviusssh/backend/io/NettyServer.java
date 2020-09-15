package com.meviusssh.backend.io;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import javax.annotation.PreDestroy;

@Component
@Order(value = 1)
@Slf4j
public class NettyServer implements CommandLineRunner {

    @Value("${server.port}")
    private int port;

    private  EventLoopGroup mainGroup;
    private EventLoopGroup subGroup;
    private Channel channel;

    public void start() throws Exception{
        mainGroup = new NioEventLoopGroup();
        subGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(mainGroup,subGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WebServerInitializer());

            ChannelFuture future = bootstrap.bind(++port).sync();
            // 监听关闭的channel，设置位同步方式
            channel = future.channel().closeFuture().sync().channel();
        }finally {
            mainGroup.shutdownGracefully();
            subGroup.shutdownGracefully();
        }
    }

    @PreDestroy
    public void stop() {
        if (channel != null) {
            log.info("Netty Server close");
            subGroup.shutdownGracefully();
            mainGroup.shutdownGracefully();
        }
    }

    @Async
    @Override
    public void run(String... args) throws Exception {
        start();
    }


}
