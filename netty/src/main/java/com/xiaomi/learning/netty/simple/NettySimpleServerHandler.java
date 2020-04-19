package com.xiaomi.learning.netty.simple;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

/**
 * @author wangcong
 * 自定义handler需要继承netty的某个HandlerAdapter
 */
public class NettySimpleServerHandler extends ChannelInboundHandlerAdapter {
    /**
     * 读取数据事件（读取客户端发送的消息）
     * ctx: 上下文对象，包含pipeline, channel, address
     * msg: 客户端发送的数据
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 如果有耗时操作，将该任务提交到channel对应的NIOEventLoop的taskQueue中
        System.out.println("client id=" + ctx.channel().hashCode());
        boolean runCostTimeTask = true;
        if (runCostTimeTask) {
            ctx.channel().eventLoop().execute(() -> {
                try {
                    Thread.sleep(10 * 1000);
                    ctx.writeAndFlush(Unpooled.copiedBuffer("Run a long time...", CharsetUtil.UTF_8));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } else {
            System.out.println("Server read thread: " + Thread.currentThread().getName());
            System.out.println("Server ctx = " + ctx);
            Channel channel = ctx.channel();
            ChannelPipeline pipeline = ctx.pipeline();
            ByteBuf byteBuf = (ByteBuf) msg;
            System.out.println("Receive from client" + ctx.channel().remoteAddress() + ": " + byteBuf.toString(CharsetUtil.UTF_8));
        }

        // 用户自定义定时任务，提交到scheduleTaskQueue中
        ctx.channel().eventLoop().schedule(() -> {
            try {
                Thread.sleep(10 * 1000);
                ctx.writeAndFlush(Unpooled.copiedBuffer("Run a schedule long time...", CharsetUtil.UTF_8));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 5, TimeUnit.SECONDS);
        System.out.println("Channel read done");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello netty client", CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
