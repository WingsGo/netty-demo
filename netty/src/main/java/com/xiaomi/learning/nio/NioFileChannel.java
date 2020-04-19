package com.xiaomi.learning.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class NioFileChannel {
    public static void main(String[] args) throws Exception {
        // 1. 文件输出流
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<1024; i++) {
            sb.append("hello world");
        }
        String str = sb.toString();
        FileOutputStream fileOutputStream = new FileOutputStream("file01.txt");
        FileChannel fileOutputStreamChannel = fileOutputStream.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(str.getBytes().length);
        buffer.put(str.getBytes());
        buffer.flip();
        fileOutputStreamChannel.write(buffer);
        fileOutputStream.close();

        // 2. 文件输入流
        FileInputStream fileInputStream = new FileInputStream("file01.txt");
        FileChannel fileInputStreamChannel = fileInputStream.getChannel();
        ByteBuffer inputBuffer = ByteBuffer.allocate(1024);
        int read;
        while ((read = fileInputStreamChannel.read(inputBuffer)) > 0) {
            System.out.println("Read from file: " + new String(inputBuffer.array(), 0, read));
        }

        // 3. 文件输入输出流
        FileInputStream fileInputStream1 = new FileInputStream("file01.txt");
        FileOutputStream fileOutputStream1 = new FileOutputStream("file01.copy.txt");
        FileChannel fileInputStream1Channel = fileInputStream1.getChannel();
        FileChannel fileOutputStream1Channel = fileOutputStream1.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int read1;
        while ((read1 = fileInputStream1Channel.read(byteBuffer)) != -1) {
            byteBuffer.flip();
            fileOutputStream1Channel.write(byteBuffer);
            byteBuffer.clear();
        }
        fileInputStream1.close();
        fileOutputStream1.close();

        // 4. channel拷贝文件
        FileInputStream fileInputStream2 = new FileInputStream("file01.txt");
        FileOutputStream fileOutputStream2 = new FileOutputStream("file01.copy2.txt");
        FileChannel fileInputStream2Channel = fileInputStream2.getChannel();
        FileChannel fileOutputStream2Channel = fileOutputStream2.getChannel();
        fileOutputStream2Channel.transferFrom(fileInputStream2Channel, 0, fileInputStream2Channel.size());
        fileInputStream2.close();
        fileOutputStream2.close();

        // 5. MappedByteBuffer, 在内存（堆外内存）中直接修改
        RandomAccessFile randomAccessFile = new RandomAccessFile("file01.txt", "rw");
        FileChannel randomAccessFileChannel = randomAccessFile.getChannel();
        MappedByteBuffer mappedByteBuffer = randomAccessFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, randomAccessFileChannel.size());
        mappedByteBuffer.put(0, (byte) 'A');
        mappedByteBuffer.put(1, (byte) 'B');
        mappedByteBuffer.put(2, (byte) 'C');
        randomAccessFile.close();

        // 6. Scattering && Gathering
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(8080);
        serverSocketChannel.socket().bind(inetSocketAddress);
        ByteBuffer[] byteBuffers = new ByteBuffer[2];
        byteBuffers[0] = ByteBuffer.allocate(5);
        byteBuffers[1] = ByteBuffer.allocate(3);
        SocketChannel socketChannel = serverSocketChannel.accept();
        int messageLength = 8;
        while (true) {
            int byteRead = 0;
            while (byteRead < messageLength) {
                long read3 = socketChannel.read(byteBuffers);
                byteRead += read3;
                System.out.println("byteRead = " + byteRead);
                Arrays.stream(byteBuffers).map(byteBuffer1 -> "position=" + byteBuffer1.position() + ", limit=" + byteBuffer1.limit()).forEach(System.out::println);
                Arrays.asList(byteBuffers).forEach(Buffer::flip);
                long byteWrite = 0;
                while (byteWrite < messageLength) {
                    long write = socketChannel.write(byteBuffers);
                    byteWrite += write;
                }
                Arrays.asList(byteBuffers).forEach(Buffer::clear);
                System.out.println("byteRead=" + byteRead + ", byteWrite=" + byteWrite + ", messageLength=" + messageLength);
            }
        }
    }
}
