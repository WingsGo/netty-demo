package com.xiaomi.learning.zerocopy;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Objects;

/**
 * @author wangcong
 */
public class NewIoClient {
    public static void main(String[] args) throws Exception {
        boolean useZeroCopy = true;
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 9999));
        String filename = NewIoClient.class.getClassLoader().getResource("test-zero-copy.txt").getPath();
        FileChannel fileChannel = new FileInputStream(filename).getChannel();
        long startTime = System.currentTimeMillis();
        long count = 0;
        if (useZeroCopy) {
            count = fileChannel.transferTo(0, fileChannel.size(), socketChannel);
        } else {
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            int read;
            while ((read = fileChannel.read(buffer)) > 0) {
                count += read;
                buffer.flip();
                socketChannel.write(buffer);
                buffer.clear();
            }
        }
        System.out.println("Send bytes: " + count + ", Cost: " + (System.currentTimeMillis() - startTime));
        fileChannel.close();
        socketChannel.close();
    }
}
