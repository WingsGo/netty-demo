package com.xiaomi.learning.netty.simple;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.NettyRuntime;

/**
 * @author wangcong
 */
public class NettySimpleServer {
    public static void main(String[] args) throws Exception {
        // 1. 创建事件监听线程组, 每个NioEventLoop都有一个Selector
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 2. 创建服务端启动对象，配置参数
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128) //设置线程队列的连接数
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // 3. 向pipeline设置处理器
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 使用容器管理channel和id的映射，推送时将业务加入到各个channel对应的NioEventLoop的taskQueue或schedulerTaskQueue
                            System.out.println("Unique client socketChannel: " + socketChannel.hashCode());
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new NettySimpleServerHandler());
                        }
                    }); // 设置workGroup的EventLoop 对应的管道设置处理器
            ChannelFuture channelFuture = bootstrap.bind(9999).sync();
            System.out.println("Server bind port 9999");
            // 4. 对关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
