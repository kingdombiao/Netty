package com.kingdombiao.io.bio;

import com.kingdombiao.io.ConstantInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 描述:
 * 服务端
 *
 * @author biao
 * @create 2019-10-15 18:11
 */
public class BioServer {

    private static ServerSocket serverSocket;

    private static ExecutorService executorService= Executors.newFixedThreadPool(5);

    public static void main(String[] args) {

        start();

    }

    private static void start() {
        try {
            serverSocket=new ServerSocket(ConstantInfo.DEFAULT_PORT);
            System.out.println("服务器已启动，端口号：" + ConstantInfo.DEFAULT_PORT);

            while(true){
                Socket socket = serverSocket.accept();
                System.out.println("**********有新的客户端连接**********");

                ////当有新的客户端接入时，打包成一个任务，投入线程池
                executorService.execute(new BioServerHandler(socket));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (serverSocket!=null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


}
