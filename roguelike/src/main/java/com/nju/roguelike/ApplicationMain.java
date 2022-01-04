package com.nju.roguelike;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.JFrame;

import asciiPanel.AsciiFont;
import asciiPanel.AsciiPanel;
import com.nju.roguelike.screen.Screen;
import com.nju.roguelike.screen.StartScreen;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Aeranythe Echosong
 */
public class ApplicationMain extends JFrame implements KeyListener {

    private AsciiPanel terminal;
    private Screen screen;

    private Timer timer;// 计时器，固定频率刷新

    public static final int REFRESH_LAG = 100;

    public ApplicationMain(int mseconds) {
        super();
        terminal = new AsciiPanel(Screen.WIDTH, Screen.HEIGHT, AsciiFont.TALRYTH_15_15);
        add(terminal);
        pack();
        screen = new StartScreen().reload();
        
        addKeyListener(this);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                repaint();
            }
        }, 0, mseconds);

        repaint();
    }

    @Override
    public void repaint() {
        terminal.clear();
        screen = screen.displayOutput(terminal);
        super.repaint();
    }

    /**
     *
     * @param e
     */
    public void keyPressed(KeyEvent e) {
        screen = screen.respondToUserInput(e);
        // repaint();
    }

    /**
     *
     * @param e
     */
    public void keyReleased(KeyEvent e) {
    }

    /**
     *
     * @param e
     */
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        ApplicationMain app = new ApplicationMain(REFRESH_LAG);// 100ms刷新一次，10Hz
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        app.setVisible(true);
    }

}
