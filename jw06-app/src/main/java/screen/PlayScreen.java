package screen;

import world.*;
import asciiPanel.AsciiPanel;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import javax.swing.JOptionPane;
import javax.swing.text.AbstractDocument.BranchElement;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Aeranythe Echosong
 */
public class PlayScreen implements Screen, Serializable {

    private World world;
    private Creature player; // 玩家
    private List<Creature> monsters; // 怪物
    private List<String> messages;
    private List<String> oldMessages;

    public boolean gameStatus; // 游戏是不是被暂停

    private int MONSTER_NUMBER;
    private int FUNGUS_NUMBER;
    private int MEDICINE_NUMBER;
    private int AMPLIFIER_NUMBER;
    private int level;

    public PlayScreen(int monster_num, int fungus_num, int medicine_num, int amplifier_num, int level) {
        MONSTER_NUMBER = monster_num;
        FUNGUS_NUMBER = fungus_num;
        MEDICINE_NUMBER = medicine_num;
        AMPLIFIER_NUMBER = amplifier_num;
        this.level = level;

        gameStatus = true;

        monsters = new ArrayList<Creature>();

        createWorld();
        this.messages = new ArrayList<String>();
        this.oldMessages = new ArrayList<String>();

        CreatureFactory creatureFactory = new CreatureFactory(this.world);
        createCreatures(creatureFactory);
    }

    private void createCreatures(CreatureFactory creatureFactory) {
        // this.player = creatureFactory.newPlayer(this.messages);

        // 开创线程池创造怪物
        ExecutorService exec = Executors.newFixedThreadPool(MONSTER_NUMBER + 1);
        this.player = creatureFactory.newPlayer(this.messages);
        player.setStatus(gameStatus);
        exec.execute(player);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // 限制屏幕上剩余怪物数量
                monsters = monsterRemain();
                if (monsters.size() < MONSTER_NUMBER && gameStatus) {
                    Creature m = creatureFactory.newMonster(player);
                    m.setStatus(gameStatus);
                    monsters.add(m); // 添加新的怪物
                    if (gameStatus) {
                        exec.execute(m);
                    }
                }
            }

        }, 5000, 5000); // 一开始等五秒，之后每五秒检查一次
        // exec.shutdown();

        for (int i = 0; i < FUNGUS_NUMBER; i++) {
            creatureFactory.newFungus();
        }

        for (int i = 0; i < MEDICINE_NUMBER; i++) {
            creatureFactory.newMedicine();
        }

        for (int i = 0; i < AMPLIFIER_NUMBER; i++) {
            creatureFactory.newAmplifier();
        }
    }

    private void createWorld() {
        world = new WorldBuilder(World.WIDTH, World.HEIGHT).makeCaves().build();
    }

    // 统计剩余fungus个数
    private synchronized int fungusremain() {
        int count = 0;
        for (Creature c : world.getCreatures()) {
            if (c.type() == CreatureType.FUNGUS) {
                ++count;
            }
        }
        return count;
    }

    // 统计剩余怪物数
    private synchronized List<Creature> monsterRemain() {
        List<Creature> monsterRemain = new ArrayList<>();

        for (Creature c : world.getCreatures()) {
            if (c.type() == CreatureType.MONSTER) {
                monsterRemain.add(c);
            }
        }
        return monsterRemain;
    }

    private void displayTiles(AsciiPanel terminal) {
        // Show terrain
        for (int x = 0; x < World.WIDTH; x++) {
            for (int y = 0; y < World.HEIGHT; y++) {
                if (player.canSee(x, y)) {
                    terminal.write(world.glyph(x, y), x, y, world.color(x, y));
                } else {
                    terminal.write(world.glyph(x, y), x, y, Color.DARK_GRAY);
                }
            }
        }
        // Show creatures
        for (Creature creature : world.getCreatures()) {
            if (creature.type() == CreatureType.FUNGUS || creature.type() == CreatureType.MEDICINE ||
                    creature.type() == CreatureType.AMPLIFIER) {
                // 照亮才能看见
                if (creature.x() >= 0 && creature.x() < World.WIDTH && creature.y() >= 0
                        && creature.y() < World.HEIGHT) {
                    if (player.canSee(creature.x(), creature.y())) {
                        terminal.write(creature.glyph(), creature.x(), creature.y(), creature.color());
                    }
                }
            } else {
                // 不用照亮就能看见
                if (creature.x() >= 0 && creature.x() < World.WIDTH && creature.y() >= 0
                        && creature.y() < World.HEIGHT) {
                    terminal.write(creature.glyph(), creature.x(), creature.y(), creature.color());
                }
            }
        }
        // Creatures can choose their next action now
        // world.update();
    }

    private void displayMessages(AsciiPanel terminal, List<String> messages) {
        int top = World.HEIGHT + 5;
        for (int i = 0; i < messages.size(); i++) {
            terminal.write(messages.get(i), 1, top + i);
        }
        oldMessages.addAll(messages);
        messages.clear();
        if (oldMessages.size() > 0) {
            messages.add(oldMessages.get(oldMessages.size() - 1));
        }
    }

    @Override
    public Screen displayOutput(AsciiPanel terminal) {
        // Terrain and creatures
        displayTiles(terminal);
        // Player
        terminal.write(player.glyph(), player.x(), player.y(), player.color());
        // Stats
        String levelstats = String.format("Level:%3d", this.level);
        terminal.write(levelstats, 1, World.HEIGHT);

        String hpstats = String.format("%3d/%3d hp", player.hp(), player.maxHP());
        terminal.write(hpstats, 1, World.HEIGHT + 2);

        String stats = String.format("Get%3d/%3d Hearts", FUNGUS_NUMBER - fungusremain(), FUNGUS_NUMBER);
        terminal.write(stats, 1, World.HEIGHT + 3);

        // Messages
        displayMessages(terminal, this.messages);

        // 判断player是不是已经死了
        if (player.hp() <= 0) {
            return new LoseScreen();
        } else if (fungusremain() == 0) {
            // 是否赢得胜利
            return new WinScreen();
        }
        return this;
    }

    @Override
    public Screen respondToUserInput(KeyEvent key) {
        if (key.isControlDown() && key.getKeyCode() == KeyEvent.VK_S) {// 同时按下ctrl+S
            // 首先暂停游戏 改变游戏状态
            if (gameStatus) {
                gameStatus = false;
                // 通知怪物
                monsters = monsterRemain();
                for (Creature m : monsters) {
                    m.setStatus(gameStatus);
                }
                // 通知玩家
                player.setStatus(gameStatus);

                int res = JOptionPane.showConfirmDialog(null, "已暂停，是否保存当前游戏进度并退出", "Option", JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) {
                    // 点击“是”后执行这个代码块
                    // 保存并退出游戏
                    JOptionPane.showMessageDialog(null, "保存成功", "Information",JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0); // 摁退出
                } else {
                    // 点击“否”后执行这个代码块
                    // 继续游戏
                    gameStatus = true;
                    // 通知怪物
                    monsters = monsterRemain();
                    for (Creature m : monsters) {
                        m.setStatus(gameStatus);
                    }
                    // 通知玩家
                    player.setStatus(gameStatus);
                }

            }
        } else {
            switch (key.getKeyCode()) {
                case KeyEvent.VK_SPACE: {
                    // 改变游戏状态
                    if (gameStatus) {
                        gameStatus = false;
                    } else {
                        gameStatus = true;
                    }

                    // 通知怪物
                    monsters = monsterRemain();
                    for (Creature m : monsters) {
                        m.setStatus(gameStatus);
                    }

                    // 通知玩家
                    player.setStatus(gameStatus);
                }
                    break;
                default:
                    player.setKeyEvent(key.getKeyCode());
                    break;
            }
        }
        return this;
    }

}
