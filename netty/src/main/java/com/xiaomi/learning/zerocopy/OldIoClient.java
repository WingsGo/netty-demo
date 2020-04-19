package com.xiaomi.learning.zerocopy;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.Socket;

/**
 * @author wangcong
 */
public class OldIoClient {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 9999);
        String filename = NewIoClient.class.getClassLoader().getResource("test-zero-copy.txt").getPath();
        FileInputStream fileInputStream = new FileInputStream(filename);
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

        byte[] buffer = new byte[4096];
        long read;
        long total = 0;
        long startTime = System.currentTimeMillis();
        while ((read = fileInputStream.read(buffer)) > 0) {
            total += read;
            dataOutputStream.write(buffer, 0, (int) read);
        }
        System.out.println("Total send bytes: " + total + ". Cost: " + (System.currentTimeMillis() - startTime));
        dataOutputStream.close();
        fileInputStream.close();
        socket.close();
    }
}
