package com.kingdombiao.io.nio;

import com.kingdombiao.io.ConstantInfo;

import java.io.IOException;
import java.util.Scanner;

/**
 * 描述:
 * NIO通信客户端
 *
 * @author biao
 * @create 2019-10-16 10:14
 */
public class NioClient {
    private static NioClientHandle nioClientHandle;

    public static void main(String[] args) throws IOException {
        start();
        Scanner scanner = new Scanner(System.in);
        while (sendMsg(scanner.next()));
    }

    private static boolean sendMsg(String msg) throws IOException {
        nioClientHandle.sendMsg(msg);
        return true;
    }

    private static void start() {
        if(nioClientHandle!=null){
            nioClientHandle.stop();
        }
        nioClientHandle=new NioClientHandle(ConstantInfo.DEFAULT_SERVER_IP,ConstantInfo.DEFAULT_PORT);
        new Thread(nioClientHandle,"client").start();
    }
}
