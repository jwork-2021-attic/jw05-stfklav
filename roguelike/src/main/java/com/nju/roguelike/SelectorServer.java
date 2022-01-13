package com.nju.roguelike;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.JFrame;

import asciiPanel.*;
import com.nju.roguelike.screen.*;

/**
 * 选择器服务端
 */

public class SelectorServer extends JFrame implements KeyListener {

    private AsciiPanel terminal;
    private static Screen screen;
    private Timer timer;// 计时器，固定频率刷新
    public static final int REFRESH_LAG = 100;

    // 管理传输的部分
    static Selector selector;
    public final static String SELECTOR_IP = "127.0.0.1";
    public final static int PORT = 17531;

    private static ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    private static ServerSocketChannel serverSocketChannel;

    private static boolean closed = false;
    private static boolean start = false;
    private static boolean lock = false;

    public SelectorServer(int mseconds) throws IOException {
        super();
        terminal = new AsciiPanel(Screen.WIDTH, Screen.HEIGHT, AsciiFont.TALRYTH_15_15);
        add(terminal);
        pack();
        // screen = new StartScreen().reload();
        screen = new PlayScreen(3, 3, 2, 2, 0);

        addKeyListener(this);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                repaint();
            }
        }, 0, mseconds);

        repaint();

        /* 向客户端传输playscreen */
        int port = PORT;
        // 打开一个ServerSocketChannel
        serverSocketChannel = ServerSocketChannel.open();
        // 获取ServerSocketChannel绑定的Socket
        ServerSocket serverSocket = serverSocketChannel.socket();
        // 设置ServerSocket监听的端口
        serverSocket.bind(new InetSocketAddress(port));
        // 设置ServerSocketChannel为非阻塞模式
        serverSocketChannel.configureBlocking(false);
        // 打开一个选择器
        selector = Selector.open();
        // 将ServerSocketChannel注册到选择器上去并监听accept事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

    }

    @Override
    public void repaint() {
        terminal.clear();
        screen = screen.displayOutput(terminal);
        super.repaint();
    }

    @Override
    public void keyTyped(KeyEvent key) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(KeyEvent key) {
        screen = screen.respondToUserInput(key);

        if (key.getKeyCode() == KeyEvent.VK_SPACE) {
            lock = true;
            if (!start)
                start = true; // 开始游戏
            else
                start = false; // 暂停游戏

            // 获取SelectionKeys上已经就绪的集合
            int n = 0;
            try {
                n = selector.select();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            // 没有就绪的通道则什么也不做
            if (n != 0) {
                System.out.println(selector.selectedKeys().size());
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                // 遍历每一个Key
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();

                    if (selectionKey.isWritable()) { // 写入通知大家开始的信息
                        try {
                            SocketChannel sc = (SocketChannel) selectionKey.channel();
                            byteBuffer.clear();
                            String str = "start";
                            if (!start)
                                str = "stop";
                            byteBuffer = ByteBuffer.allocate(str.length());
                            byteBuffer.put(str.getBytes());
                            byteBuffer.flip(); // 读缓冲区的数据之前一定要先反转(flip)
                            sc.write(byteBuffer);
                            byteBuffer.clear();
                        } catch (IOException e) {
                            selectionKey.cancel();
                            continue;
                        }
                    }
                } // . end of while
            }
            lock = false;
        }

    }

    @Override
    public void keyReleased(KeyEvent key) {
        // TODO Auto-generated method stub

    }

    // 把消息发给所有客户端
    private static void writeToAll(String data) {
        lock = true;
        int n = 0;
        try {
            n = selector.select();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        // 没有就绪的通道则什么也不做
        if (n != 0) {
            // System.out.println(selector.selectedKeys().size());
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            // 遍历每一个Key
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                if (selectionKey.isWritable()) { // 写入通知大家开始的信息
                    try {
                        writeDataToSocket(selectionKey, data);

                    } catch (IOException e) {
                        selectionKey.cancel();
                        continue;
                    }
                }
            }
        }
        lock = false;
    }

    /**
     * 向通道中写数据
     * 
     * @throws IOException
     */
    private static void writeDataToSocket(SelectionKey sk, String str) throws IOException {
        // 把消息其他（一个）玩家
        SocketChannel sc = (SocketChannel) sk.channel();
        // 清空缓存
        byteBuffer.clear();
        // System.out.println(str);
        byteBuffer = ByteBuffer.allocate(str.length());
        byteBuffer.put(str.getBytes());
        byteBuffer.flip(); // 读缓冲区的数据之前一定要先反转(flip)

        // 从缓冲区读出来写进通道
        sc.write(byteBuffer);
        byteBuffer.clear();
    }

    /**
     * 从通道中读取数据
     */
    private static void readDataFromSocket(SelectionKey sk, String str) throws IOException {
        SocketChannel sc = (SocketChannel) sk.channel();
        byteBuffer.clear();
        ArrayList<Byte> list = new ArrayList<>();
    }

    // 根据玩家号和操作
    private static void operation(int player_number, int keycode) {

        PlayScreen playscreen = (PlayScreen) screen;
        playscreen.setPlayer(player_number);

        Button a = new Button("click");
        screen = screen.respondToUserInput(new KeyEvent(a, 1, 20, 1, keycode, 'a'));

    }

    public static void main(String[] args) throws IOException {
        SelectorServer app = new SelectorServer(REFRESH_LAG);// 100ms刷新一次，10Hz
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        app.setVisible(true);

        // 没被锁上的时候遍历
        while (!closed) {
            if (!lock) {
                int n = selector.select();
                // 没有就绪的通道则什么也不做
                if (n == 0) {
                    continue;
                }

                /*
                 * if (selector.selectedKeys().size() > 0)
                 * System.out.println(selector.selectedKeys().size());
                 */
                // 获取SelectionKeys上已经就绪的集合
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                // 遍历每一个Key
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    // 必须在处理完通道时自己移除。下次该通道变成就绪时，Selector会再次将其放入已选择键集中。
                    iterator.remove();

                    // 通道上是否有可接受的连接
                    // 开始游戏后不再接受新连接
                    if (selectionKey.isAcceptable()) {
                        System.out.println("Accept a new SocketChannel");
                        ServerSocketChannel sscTmp = (ServerSocketChannel) selectionKey.channel();
                        SocketChannel sc = sscTmp.accept(); // accept()方法会一直阻塞到有新连接到达。
                        sc.configureBlocking(false);
                        sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    }
                    // 读数据
                    if (selectionKey.isReadable()) {
                        // 从channel中读取数据
                        SocketChannel sc = (SocketChannel) selectionKey.channel();

                        byteBuffer.clear();

                        ArrayList<Byte> list = new ArrayList<>();
                        while (sc.read(byteBuffer) > 0) {
                            byteBuffer.flip();
                            while (byteBuffer.hasRemaining()) {
                                list.add(byteBuffer.get());
                            }
                            byteBuffer.clear();
                        }
                        byte[] bytes = new byte[list.size()];
                        for (int i = 0; i < bytes.length; i++) {
                            bytes[i] = list.get(i);
                        }
                        // 收到消息
                        String s = (new String(bytes)).trim();
                        String[] couple = s.split(" ");
                        System.out.println("服务端收到：" + s);

                        // 写给所有客户端
                        writeToAll(s);

                        // 在自己这里操作
                        operation(Integer.parseInt(couple[0]), Integer.parseInt(couple[1]));

                    }

                } // . end of while

            }

        }
    }

}
