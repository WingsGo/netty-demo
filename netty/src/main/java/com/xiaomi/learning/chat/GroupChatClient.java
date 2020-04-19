package com.xiaomi.learning.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author wangcong
 */
public class GroupChatClient {
    private final String HOST = "localhost";
    private final int PORT = 6667;
    private Selector selector;
    private SocketChannel socketChannel;
    private String username;
    private AtomicBoolean initialize = new AtomicBoolean(false);

    public GroupChatClient() {
    }

    public void setup() {
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open(new InetSocketAddress(HOST, PORT));
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
            username = socketChannel.getLocalAddress().toString().substring(1);
            initialize.set(true);
            System.out.println("User " + username + " is ready...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendInfo(String info) {
        info = username + " send info: " + info;
        try {
            socketChannel.write(ByteBuffer.wrap(info.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readInfo() {
        try {
            int readChannels = selector.select(5000);
            if (readChannels > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isReadable()) {
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        while (channel.read(buffer) > 0) {
                            buffer.flip();
                            byte[] tmp = new byte[buffer.limit()];
                            buffer.get(tmp);
                            buffer.clear();
                            System.out.println("Receive info: " + new String(tmp));
                        }
                    } else {
                        System.out.println("No available channel...");
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        GroupChatClient groupChatClient = new GroupChatClient();
        groupChatClient.setup();
        if (!groupChatClient.initialize.get()) {
            System.out.println("Start client failed");
            return;
        }
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    groupChatClient.readInfo();
                    try {
                        sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            groupChatClient.sendInfo(line);
        }
    }
}
