package com.xiaomi.learning.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author wangcong
 */
public class BioServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9999);
        System.out.println("Start listen port 9999");
        ExecutorService executorService = Executors.newCachedThreadPool();
        while (true) {
            Socket socket = serverSocket.accept();
            executorService.execute(() -> {
                System.out.println("New client incoming");
                handle(socket);
            });
        }
    }

    public static void handle(Socket socket) {
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {
            byte[] bytes = new byte[1024];
            int read;
            while ((read = inputStream.read(bytes)) != -1) {
                System.out.println("Receive info: " + new String(bytes, 0, read));
                outputStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
