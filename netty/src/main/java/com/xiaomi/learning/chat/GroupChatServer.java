package com.xiaomi.learning.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author wangcong
 */
public class GroupChatServer {
    private Selector selector;
    private ServerSocketChannel listenChannel;
    private static final int PORT = 6667;
    private AtomicBoolean initialize = new AtomicBoolean(false);

    public GroupChatServer() {
    }

    public void setup() {
        try {
            selector = Selector.open();
            listenChannel = ServerSocketChannel.open();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(PORT);
            listenChannel.socket().bind(inetSocketAddress);
            listenChannel.configureBlocking(false);
            listenChannel.register(selector, SelectionKey.OP_ACCEPT);
            initialize.set(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() {
        try {
            while (true) {
                int count = selector.select(5000);
                if (count > 0) {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey selectionKey = iterator.next();
                        if (selectionKey.isAcceptable()) {
                            SocketChannel socketChannel = listenChannel.accept();
                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                            System.out.println("New Client " + socketChannel.getRemoteAddress() + " online...");
                        }
                        if (selectionKey.isReadable()) {
                            readData(selectionKey);
                        }
                        iterator.remove();
                    }
                } else {
                    System.out.println("Waiting new connection incoming");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readData(SelectionKey selectionKey) {
        SocketChannel channel = null;
        try {
            channel = (SocketChannel) selectionKey.channel();
            ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
            while (channel.read(byteBuffer) > 0) {
                byteBuffer.flip();
                byte[] bytes = new byte[byteBuffer.limit()];
                byteBuffer.get(bytes);
                String msg = new String(bytes);
                System.out.println("From client " + channel.getRemoteAddress() + " send msg: " + msg);
                sendInfoToOtherClients(msg, channel);
                byteBuffer.clear();
            }
        } catch (IOException e) {
            try {
                System.out.println(channel.getRemoteAddress() + " is offline");
                selectionKey.cancel();
                channel.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    private void sendInfoToOtherClients(String msg, SocketChannel selfChannel) throws IOException {
        System.out.println("Transfer msg " + msg);
        for (SelectionKey key : selector.keys()) {
            Channel targetChannel = key.channel();
            if (targetChannel instanceof SocketChannel && targetChannel != selfChannel) {
                SocketChannel dstChannel = (SocketChannel) targetChannel;
                ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                dstChannel.write(buffer);
            }
        }
    }

    public static void main(String[] args) {
        GroupChatServer groupChatServer = new GroupChatServer();
        groupChatServer.setup();
        if (!groupChatServer.initialize.get()) {
            System.out.println("Start server failed");
            return;
        }
        groupChatServer.listen();
    }
}
