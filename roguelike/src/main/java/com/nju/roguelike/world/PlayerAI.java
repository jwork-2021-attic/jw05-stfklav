package com.nju.roguelike.world;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.awt.event.KeyEvent;

public class PlayerAI extends CreatureAI {

    private List<String> messages;

    public PlayerAI(Creature creature, List<String> messages) {
        super(creature);
        this.messages = messages;
    }

    public void onEnter(int x, int y, Tile tile) {
        if (tile.isGround()) {
            creature.setX(x);
            creature.setY(y);
        } else if (tile.isWall()) {
            this.creature.dig(x, y);
            creature.setX(x);
            creature.setY(y);
        }
    }

    @Override
    public void run() {
        while (this.creature.hp() > 0) {
            if (creature.getStatus()) {
                switch (this.creature.keyevent()) {
                    // 上下左右移动
                    case KeyEvent.VK_LEFT:
                        creature.moveBy(-1, 0);
                        break;
                    case KeyEvent.VK_RIGHT:
                        creature.moveBy(1, 0);
                        break;
                    case KeyEvent.VK_UP:
                        creature.moveBy(0, -1);
                        break;
                    case KeyEvent.VK_DOWN:
                        creature.moveBy(0, 1);
                        break;
                    // w a s d放墙
                    case KeyEvent.VK_A:
                        creature.setWall(-1, 0);
                        break;
                    case KeyEvent.VK_D:
                        creature.setWall(1, 0);
                        break;
                    case KeyEvent.VK_W:
                        creature.setWall(0, -1);
                        break;
                    case KeyEvent.VK_S:
                        creature.setWall(0, 1);
                        break;
                    default:
                        break;
                }
            }

            // 每次回到Enter键
            creature.setKeyEvent(KeyEvent.VK_ENTER);
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            // Thread.yield();
        }
        return;
    }

    public void onNotify(String message) {
        this.messages.add(message);
    }

}