package com.kingdombiao.io.bio;

import com.kingdombiao.io.ConstantInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author biao
 * @create 2019-10-15 18:16
 */
public class BioServerHandler implements Runnable{

    private Socket socket;

    public BioServerHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        //处理socket 读写的输入，输出
        try(BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pw=new PrintWriter(socket.getOutputStream(),true)) {

            String msg=null;
            String result=null;

            while ((msg=br.readLine())!=null){
                System.out.println("Server accept message:"+msg);
                result= ConstantInfo.response(msg);
                //通过输出流返回给客户端
                pw.println(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(socket!=null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
