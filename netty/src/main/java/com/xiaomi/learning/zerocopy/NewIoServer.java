package com.xiaomi.learning.zerocopy;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author wangcong
 */
public class NewIoServer {
    public static void main(String[] args) throws Exception {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(9999);
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(inetSocketAddress);
        System.out.println("Start server " + serverSocketChannel.getLocalAddress());
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        while (true) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            System.out.println("New client " + socketChannel.getRemoteAddress() + " incoming");
            int read;
            int total = 0;
            while ((read = socketChannel.read(buffer)) != -1) {
                total += read;
                buffer.rewind();
            }
            System.out.println("Receive bytes: " + total);
        }
    }
}
