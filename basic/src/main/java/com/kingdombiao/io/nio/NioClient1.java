package com.kingdombiao.io.nio;

import com.kingdombiao.io.ConstantInfo;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * 描述:
 * ${DESCRIPTION}
 *
 * @author biao
 * @create 2019-12-10 14:17
 */
public class NioClient1 {
    private Selector selector;
    private SocketChannel socketChannel;

    private String nickName = "";
    private Charset charset = Charset.forName("utf-8");
    private static String SYS_USER_EXIST = "系统提示：该昵称已经存在，请换一个昵称";
    private static String SYS_USER_CONTENT_SPILIT = "#@#";

    public NioClient1() throws IOException {
        socketChannel = SocketChannel.open(new InetSocketAddress(ConstantInfo.DEFAULT_SERVER_IP, ConstantInfo.DEFAULT_PORT));
        socketChannel.configureBlocking(false);
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    public static void main(String[] args) throws IOException {
        new NioClient1().run();
    }

    public void run(){
        //开辟一个新线程往服务器端写数据
        new sendMsg().start();

        //开辟一个新线程从服务器端读数据
        new acceptMsg().start();
    }

    /**
     * 开辟一个线程往服务端发送数据
     */
    private class sendMsg extends Thread {
        @Override
        public void run() {
            try {
                Scanner scanner = new Scanner(System.in);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if ("".equals(line)) {
                        continue;
                    }
                    if ("".equals(nickName)) {
                        nickName = line;
                        line = nickName + SYS_USER_CONTENT_SPILIT;
                    } else {
                        line = nickName + SYS_USER_CONTENT_SPILIT + line;
                    }
                    socketChannel.write(charset.encode(line));
                }
                scanner.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 接收服务器返回的数据
     */
    private class acceptMsg extends Thread{

        @Override
        public void run() {

            try {
                while (true){
                    int readChannels = selector.select();
                    if(readChannels==0) {
                        continue;
                    }

                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()){
                        SelectionKey selectionKey = iterator.next();
                        iterator.remove();
                        process(selectionKey);
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void process(SelectionKey selectionKey) throws IOException {
        if(selectionKey.isValid()){
            if (selectionKey.isReadable()){
                SocketChannel clientChannel= (SocketChannel) selectionKey.channel();
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                String acceptContent="";
                while (clientChannel.read(byteBuffer)>0){
                    byteBuffer.flip();
                    acceptContent+=charset.decode(byteBuffer);

                }

                if(SYS_USER_EXIST.equals(acceptContent)){
                    nickName="";
                }
                System.out.println(acceptContent);
                selectionKey.interestOps(SelectionKey.OP_READ);
            }
        }
    }
}
