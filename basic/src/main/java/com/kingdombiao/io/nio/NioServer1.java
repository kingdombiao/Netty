package com.kingdombiao.io.nio;

import com.kingdombiao.io.ConstantInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author biao
 * @create 2019-12-10 15:15
 */
public class NioServer1 {
    private Charset charset = Charset.forName("utf-8");
    private static Set<String> users = new HashSet<>();
    //保存请求的client
    private static Map<SocketChannel,String> clientMap=new ConcurrentHashMap<>();
    private static String SYS_USER_EXIST = "系统提示：该昵称已经存在，请换一个昵称";
    private static String SYS_USER_CONTENT_SPILIT = "#@#";
    private Selector selector;

    public NioServer1(int port) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(port));
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务端已启动，监听端口是：" + port);
    }

    public static void main(String[] args) throws IOException {
        new NioServer1(ConstantInfo.DEFAULT_PORT).listener();
    }

    public void listener() throws IOException {
        while (true) {
            int waitNum = selector.select();
            if (waitNum == 0) {
                continue;
            }
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                process(selectionKey);
            }


        }
    }

    private void process(SelectionKey selectionKey) throws IOException {
        if (selectionKey.isValid()) {
            if (selectionKey.isAcceptable()) {
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                SocketChannel socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(false);
                //注册选择器，并设置为读取模式，收到一个连接请求，然后起一个SocketChannel，并注册到selector上，之后这个连接的数据，就由这个SocketChannel处理
                socketChannel.register(selector, SelectionKey.OP_READ);

                //将此对应的channel设置为准备接受其他客户端请求
                selectionKey.interestOps(SelectionKey.OP_ACCEPT);
                socketChannel.write(charset.encode("请输入你的昵称:"));
            }

            if (selectionKey.isReadable()) {
                //返回该SelectionKey对应的 Channel，其中有数据需要读取
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                System.out.println("socketChannel="+socketChannel);
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                StringBuilder acceptBuilder = new StringBuilder();
                try {
                    while (socketChannel.read(byteBuffer) > 0) {
                        byteBuffer.flip();
                        acceptBuilder.append(charset.decode(byteBuffer));
                    }
                } catch (IOException e) {
                    if(clientMap!=null){
                        String clientName = clientMap.get(socketChannel);
                        System.out.println("************"+clientName+"已退出群聊************");
                        broadCast(socketChannel,"【"+clientName+"】已退出群聊");
                    }

                    selectionKey.cancel();
                    if (selectionKey.channel() != null) {
                        selectionKey.channel().close();
                    }
                }

                if (acceptBuilder.length() > 0) {
                    String[] splitResult = acceptBuilder.toString().split(SYS_USER_CONTENT_SPILIT);
                    if (splitResult != null && splitResult.length == 1) {
                        String nickName = splitResult[0];
                        if (users.contains(nickName)) {
                            socketChannel.write(charset.encode(SYS_USER_EXIST));
                        } else {
                            users.add(nickName);
                            int onlineNum = countOnlineNum();
                            String message = "欢迎 " + nickName + " 进入聊天室! 当前在线人数:" + onlineNum;

                            //广播给其它客户端
                            broadCast(null,message);
                        }
                        clientMap.put(socketChannel,nickName);


                    }else if(splitResult != null && splitResult.length >1){
                        String nickName = splitResult[0];
                        String msg=acceptBuilder.substring(nickName.length()+SYS_USER_CONTENT_SPILIT.length());
                        msg="【"+nickName+"】:"+msg;

                        //广播给其它客户端
                        broadCast(socketChannel,msg);
                    }
                }
            }
        }
    }

    /**
     * 广播给其它客户端
     * @param client
     * @param message
     * @throws IOException
     */
    private void broadCast(SocketChannel client, String message) throws IOException {
        for (SelectionKey selectionKey : selector.keys()) {
            SelectableChannel selectableChannel = selectionKey.channel();
            if(selectableChannel instanceof SocketChannel && selectableChannel!=client){
                ((SocketChannel) selectableChannel).write(charset.encode(message));
            }
        }
    }

    /**
     * 统计在线人数
     */
    private int countOnlineNum() {
        int num = 0;
        for (SelectionKey selectionKey : selector.keys()) {
            SelectableChannel selectableChannel = selectionKey.channel();
            if (selectableChannel instanceof SocketChannel) {
                num++;
            }
        }
        return num;
    }
}
