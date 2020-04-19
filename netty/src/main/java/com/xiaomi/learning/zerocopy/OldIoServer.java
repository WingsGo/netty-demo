package com.xiaomi.learning.zerocopy;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author wangcong
 */
public class OldIoServer {
    public static void main(String[] args) throws Exception{
        ServerSocket serverSocket = new ServerSocket(9999);
        while (true) {
            Socket socket = serverSocket.accept();
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            try {
                int read;
                int count = 0;
                byte[] bytes = new byte[4096];
                while ((read = dataInputStream.read(bytes)) != -1) {
                    // just read
                    count += read;
                }
                System.out.println("Total read: " + count);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
