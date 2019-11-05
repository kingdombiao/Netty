package com.kingdombiao.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 描述:
 * NIO通信客户端
 *
 * @author biao
 * @create 2019-10-16 9:26
 */
public class NioClientHandle implements Runnable {

    private String ip;
    private int port;
    private Selector selector;
    private SocketChannel socketChannel;

    //客户端是否启动成功
    private volatile boolean started;

    public NioClientHandle(String ip, int port) {
        this.ip = ip;
        this.port = port;

        try {
            //创建选择器
            selector = Selector.open();

            //打开通道
            socketChannel = SocketChannel.open();

           /*如果为 false，则此通道将被置于非阻塞模式;
            如果为 true，则此通道将被置于阻塞模式；
            */
            socketChannel.configureBlocking(Boolean.FALSE);

            started = true;

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void run() {

        try {
            doConnect();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //循环遍历selector
        while (started) {
            try {
                //阻塞，至少有一个注册事件发生才会继续执行
                selector.select();

                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iteratorKey = selectionKeys.iterator();
                SelectionKey selectionKey = null;
                while (iteratorKey.hasNext()) {
                    selectionKey = iteratorKey.next();
                    iteratorKey.remove();
                    try {
                        handleInput(selectionKey);
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (selectionKey != null) {
                            selectionKey.cancel();
                            if (selectionKey.channel() != null) {
                                selectionKey.channel().close();
                            }
                        }
                    }
                }
            } catch (IOException e) {
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

    /**
     * 具体的事件处理
     *
     * @param selectionKey
     */
    private void handleInput(SelectionKey selectionKey) throws IOException {
        if (selectionKey.isValid()) {
            //获取当前事件的channel
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            if (selectionKey.isConnectable()) {
                //连接事件
                if (!socketChannel.finishConnect()) {
                    System.exit(1);
                }
            }

            //数据可读事件
            if (selectionKey.isReadable()) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                int readBytes = socketChannel.read(buffer);
                if (readBytes > 0) {
                    //将缓冲区当前的limit设置为position,position=0，用于后续对缓冲区的读取操作
                    buffer.flip();

                    //根据缓冲区可读字节数创建字节数组
                    byte[] bytes = new byte[buffer.remaining()];

                    //将缓冲区可读字节数组复制到新建的数组中
                    buffer.get(bytes);

                    String result = new String(bytes, "UTF-8");
                    System.out.println("client accept message:" + result);
                } else if (readBytes <= 0) {
                    selectionKey.cancel();
                    socketChannel.close();
                }
            }
        }
    }

    private void doConnect() throws IOException {
        if (!socketChannel.connect(new InetSocketAddress(ip, port))) {
            //连接未完成，注册连接就绪事件，向selector表示关注此事件
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }
    }

    //发送消息
    private void doWrite(SocketChannel socketChannel,String msg) throws IOException {
        byte[] bytes = msg.getBytes();
        ByteBuffer writerBuffer = ByteBuffer.allocate(bytes.length);
        //将字节数组复制到缓冲区
        writerBuffer.put(bytes);

        writerBuffer.flip();

        //发送缓冲区的字节数组
        socketChannel.write(writerBuffer);
    }

    /**
     * 发送消息
     * @param msg
     * @throws IOException
     */
    public void sendMsg(String msg) throws IOException {
        socketChannel.register(selector,SelectionKey.OP_READ);
        doWrite(socketChannel,msg);
    }

    public void stop() {
        started = false;
    }
}
