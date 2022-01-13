package com.nju.roguelike.world;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World implements Serializable {

    private Tile[][] tiles;
    private int width;
    private int height;
    public static final int WIDTH = 30;
    public static final int HEIGHT = 30;

    public static final int TILE_TYPES = 2;
    private ArrayList<Creature> creatures; // 序列化函数中再处理

    public World(Tile[][] tiles) {
        this.tiles = tiles;
        this.width = tiles.length;
        this.height = tiles[0].length;
        this.creatures = new ArrayList<>();
    }

    public Tile tile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return Tile.BOUNDS;
        } else {
            return tiles[x][y];
        }
    }

    public char glyph(int x, int y) {
        return tiles[x][y].glyph();
    }

    public Color color(int x, int y) {
        return tiles[x][y].color();
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public void dig(int x, int y) {
        if (tile(x, y).isWall()) {
            tiles[x][y] = Tile.FLOOR;
        }
    }

    public void setWall(int x, int y) {
        if (tile(x, y).isGround()) {
            // 是平地才能放墙
            tiles[x][y] = Tile.WALL;
        }
    }

    public synchronized void addAtEmptyLocation(Creature creature) {
        int x = 0;
        int y = 0;


        if (creature.type() == CreatureType.MONSTER) {
            // 怪物从边上进来
            x = 0;
            y = 0;
            do {
                x = (x + 1) % this.width;
            } while (!tile(x, y).isGround() || this.creature(x, y) != null);
        } else if (creature.type() == CreatureType.PLAYER) {
            x = this.width / 2;
            y = this.height / 2;
            do {
                x = (x + 9) % this.width;
                y = (y + 13) % this.height;
            } while (!tile(x, y).isGround() || this.creature(x, y) != null);
        }else if(creature.type() == CreatureType.FUNGUS){
            x = this.width / 3;
            y = this.height / 4;
            do {
                x = (x + 2) % this.width;
                y = (y + 7) % this.height;
            } while (!tile(x, y).isGround() || this.creature(x, y) != null);
        }else if(creature.type() == CreatureType.MEDICINE){
            x = this.width - 1;
            y = this.height - 1;
            do {
                x = (x + 17) % this.width;
                y = (y + 3) % this.height;
            } while (!tile(x, y).isGround() || this.creature(x, y) != null);
        }else{
            x = this.width / 2;
            y = this.height / 2;
            do {
                x = (x + 33) % this.width;
                y = (y + 21) % this.height;
            } while (!tile(x, y).isGround() || this.creature(x, y) != null);
        }

        creature.setX(x);
        creature.setY(y);

        this.creatures.add(creature);
    }

    public synchronized Creature creature(int x, int y) {
        for (Creature c : this.creatures) {
            if (c.x() == x && c.y() == y) {
                return c;
            }
        }
        return null;
    }

    public synchronized List<Creature> getCreatures() {
        return this.creatures;
    }

    public synchronized void remove(Creature target) {
        this.creatures.remove(target);
    }

    // 统计剩余玩家
    public synchronized List<Creature> playerRemain() {
        List<Creature> playerRemain = new ArrayList<>();

        for (Creature c : this.getCreatures()) {
            if (c.type() == CreatureType.PLAYER) {
                playerRemain.add(c);
            }
        }
        return playerRemain;
    }

    // 统计剩余怪物数
    public synchronized List<Creature> monsterRemain() {
        List<Creature> monsterRemain = new ArrayList<>();

        for (Creature c : this.getCreatures()) {
            if (c.type() == CreatureType.MONSTER) {
                monsterRemain.add(c);
            }
        }
        return monsterRemain;
    }

    // 统计剩余fungus
    public synchronized List<Creature> fungusRemain() {
        List<Creature> fungusRemain = new ArrayList<>();
        for (Creature c : this.getCreatures()) {
            if (c.type() == CreatureType.FUNGUS) {
                fungusRemain.add(c);
            }
        }
        return fungusRemain;
    }

    // 统计剩余medicine
    public synchronized List<Creature> medicineRemain() {
        List<Creature> medicineRemain = new ArrayList<>();
        for (Creature c : this.getCreatures()) {
            if (c.type() == CreatureType.MEDICINE) {
                medicineRemain.add(c);
            }
        }
        return medicineRemain;
    }

    // 统计剩余amplifier
    public synchronized List<Creature> amplifierRemain() {
        List<Creature> amplifierRemain = new ArrayList<>();
        for (Creature c : this.getCreatures()) {
            if (c.type() == CreatureType.MEDICINE) {
                amplifierRemain.add(c);
            }
        }
        return amplifierRemain;
    }

    // setters and getters 序列化输出输入
    private synchronized void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        // oos.writeObject(creatures);
    }

    private synchronized void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        for (Creature c : this.creatures) {
            c.setWorld(this);
            new CreatureAI(c);
        }
    }

    public synchronized void update() {
        ArrayList<Creature> toUpdate = new ArrayList<>(this.creatures);

        for (Creature creature : toUpdate) {
            creature.update();
        }
    }
}
