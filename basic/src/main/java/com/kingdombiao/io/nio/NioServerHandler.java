package com.kingdombiao.io.nio;

import com.kingdombiao.io.ConstantInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.security.Key;
import java.util.Iterator;
import java.util.Set;

/**
 * 描述:
 * Nio 通信服务器端处理器
 *
 * @author biao
 * @create 2019-12-09 14:04
 */
public class NioServerHandler implements Runnable {

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private volatile boolean started;

    public NioServerHandler(int port) {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(port));
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            started = true;
            System.out.println("服务器已启动，端口号：" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        while (started) {
            try {
                //此方法阻塞，只有当至少一个注册事件发生的时候才会继续执行
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                SelectionKey selectionKey = null;
                while (iterator.hasNext()) {
                    selectionKey = iterator.next();
                    iterator.remove();
                    try {
                        handleInput(selectionKey);
                    } catch (IOException e) {
                        if (selectionKey != null) {
                            selectionKey.cancel();
                            if (selectionKey.channel() != null) {
                                selectionKey.channel().close();
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        //selector关闭后会自动释放里面管理的资源
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void handleInput(SelectionKey selectionKey) throws IOException {
        //判断当前key是否有效
        if (selectionKey.isValid()) {
            //处理新接入的请求消息
            if (selectionKey.isAcceptable()) {
                //获取当前事件的channel
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();

                System.out.println("**********socket channel 建立连接*************");
                SocketChannel socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, SelectionKey.OP_READ);
            }

            //读消息
            if (selectionKey.isReadable()) {
                System.out.println("**********socket channel 数据准备完毕，可以读取数据*************");
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                int readBytes = socketChannel.read(buffer);
                if(readBytes>0){
                    buffer.flip();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    String message = new String(bytes, "utf-8");
                    System.out.println("服务器端收到消息：" + message);

                    //发送应答信息给客户端
                    String result= ConstantInfo.response(message);
                    doWrite(socketChannel,result);

                }else if(readBytes<0){ //客户端链路已关闭，释放资源
                    System.out.println("************客户端链路已关闭，释放资源************");
                    selectionKey.cancel();
                    socketChannel.close();
                }

            }
        }
    }

    //发送应答消息给客户端
    private void doWrite(SocketChannel socketChannel, String result) throws IOException {
        byte[] bytes = result.getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
        writeBuffer.put(bytes);
        writeBuffer.flip();
        socketChannel.write(writeBuffer);
    }

    public void stop() {
        started = false;
    }


}
