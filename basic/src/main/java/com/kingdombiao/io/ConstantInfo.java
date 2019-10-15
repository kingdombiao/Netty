package com.kingdombiao.io;

import java.util.Date;

/**
 * 描述:
 * 服务器端口以及ip 地址
 *
 * @author biao
 * @create 2019-10-15 17:55
 */
public class ConstantInfo {

    //服务器端口号
    public static int DEFAULT_PORT = 6666;

    //服务器端ip
    public static String DEFAULT_SERVER_IP = "127.0.0.1";

    public static String repsonse(String msg){
        return "Hello "+msg+",Now is "+new Date(System.currentTimeMillis()).toString();
    }

}
