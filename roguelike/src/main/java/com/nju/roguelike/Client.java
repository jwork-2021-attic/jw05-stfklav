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
 * 客户端
 */

public class Client extends JFrame implements KeyListener {

    private AsciiPanel terminal;
    private static Screen screen;
    private Timer timer;// 计时器，固定频率刷新
    public static final int REFRESH_LAG = 100;

    // 和服务器沟通部分
    private static SocketChannel socketChannel;
    private static ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    private boolean isConnected = false;
    private static boolean start = false;

    static int player_number = 1; // 玩家编号

    public Client(int mseconds) {
        super();
        terminal = new AsciiPanel(Screen.WIDTH, Screen.HEIGHT, AsciiFont.TALRYTH_15_15);
        add(terminal);
        pack();
        // screen = new StartScreen().reload();
        screen = new PlayScreen(3, 3, 2, 2, 0);
        PlayScreen playscreen = (PlayScreen) screen;
        playscreen.setPlayer(player_number);
        playscreen.setPlayer_number(player_number);

        addKeyListener(this);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                repaint();
            }
        }, 0, mseconds);

        repaint();

        try {
            // 打开一个socketChannel
            socketChannel = SocketChannel.open();
            // 设置为非阻塞
            socketChannel.configureBlocking(false);
            // 尝试和指定的服务端建立连接
            socketChannel.connect(new InetSocketAddress(SelectorServer.SELECTOR_IP, SelectorServer.PORT));
            while (!socketChannel.finishConnect()) {
                System.out.println("同" + SelectorServer.SELECTOR_IP + "的连接正在建立，请稍等！");
                Thread.sleep(10);
            }
            System.out.println("连接已建立，待写入内容至指定ip+端口！时间为" + System.currentTimeMillis());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

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
        try {
            // 把玩家号和要做的动作发给服务器
            byteBuffer.clear();
            String str = Integer.toString(player_number) + " " + Integer.toString(key.getKeyCode()) + " ";
            // System.out.println("客户端发送：" + str);
            byteBuffer = ByteBuffer.allocate(str.length());
            byteBuffer.put(str.getBytes());
            byteBuffer.flip(); // 读缓冲区的数据之前一定要先反转(flip)
            socketChannel.write(byteBuffer);
            byteBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // screen = screen.respondToUserInput(key);

    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    // 根据玩家号和操作
    private static void operation(int player_number2, int keycode) {

        // 切换到要求的玩家操作
        PlayScreen playscreen = (PlayScreen) screen;
        playscreen.setPlayer(player_number2);

        Button a = new Button("click");
        screen = screen.respondToUserInput(new KeyEvent(a, 1, 20, 1, keycode, 'a'));

        // 切换回自己的
        playscreen.setPlayer(player_number);

    }

    public void run() throws Exception {
        try {
            while (true) {
                // 清空缓存
                byteBuffer.clear();

                // 从channel中读取数据
                ArrayList<Byte> list = new ArrayList<>();
                while (socketChannel.read(byteBuffer) > 0) {
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
                String s = (new String(bytes)).trim();
                if (s.length() > 0)
                    System.out.println("客户端收到：" + s);
                if (!s.isEmpty()) {
                    if ("start".equals(s)) {
                        start = true;
                        // 模拟按空格键
                        Button a = new Button("start");
                        screen = screen.respondToUserInput(new KeyEvent(a, 1, 20, 1, KeyEvent.VK_SPACE, 'a'));

                    } else if ("stop".equals(s)) {
                        start = false;
                        Button a = new Button("stop");
                        screen = screen.respondToUserInput(new KeyEvent(a, 1, 20, 1, KeyEvent.VK_SPACE, 'a'));
                    } else {
                        // 执行一些动作
                        String[] couple = s.split(" ");

                        // 在自己这里操作
                        operation(Integer.parseInt(couple[0]), Integer.parseInt(couple[1]));
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (Objects.nonNull(socketChannel)) {
                try {
                    socketChannel.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } finally {
            if (Objects.nonNull(socketChannel)) {
                try {
                    socketChannel.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client(REFRESH_LAG);// 100ms刷新一次，10Hz
        client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.setVisible(true);

        client.run();
    }

}