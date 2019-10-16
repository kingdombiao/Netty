package com.kingdombiao.io.bio;

import com.kingdombiao.io.ConstantInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * 描述:
 * 客户端
 *
 * @author biao
 * @create 2019-10-15 17:53
 */
public class BioClient {

    public static void main(String[] args) throws IOException {

        Socket socket = new Socket(ConstantInfo.DEFAULT_SERVER_IP, ConstantInfo.DEFAULT_PORT);
        System.out.println("请输入请求消息：");

        //启动读取服务端返回数据的线程
       new Thread( new ReadMsg(socket)).start();


        while (true){
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            pw.println(new Scanner(System.in).next());
            pw.flush();
        }
    }

    /**
     * 读取服务端返回数据的线程
     */
    private static class ReadMsg implements Runnable{

        private Socket socket;

        public ReadMsg(Socket socket) {
            this.socket=socket;
        }

        @Override
        public void run() {

            try(BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String line =null;
                while ((line=br.readLine())!=null){
                    System.out.printf("%s\n",line);
                }
            }catch (IOException e) {
                e.printStackTrace();
            } finally {
               if(socket !=null){
                   try {
                       socket.close();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
            }
        }
    }
}
