package com.kingdombiao.io.nio;

import com.kingdombiao.io.ConstantInfo;

/**
 * 描述:
 * nio通信服务端
 *
 * @author biao
 * @create 2019-12-09 16:02
 */
public class NioServer {
    private static NioServerHandler nioServerHandler;
    public static void main(String[] args) {
        if(nioServerHandler!=null){
            nioServerHandler.stop();
        }

        NioServerHandler nioServerHandler = new NioServerHandler(ConstantInfo.DEFAULT_PORT);
        new Thread(nioServerHandler,"nioServer").start();
    }
}
