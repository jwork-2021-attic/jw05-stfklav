package world;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
 * Copyright (C) 2015 Aeranythe Echosong
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
/**
 *
 * @author Aeranythe Echosong
 */
public class World {

    private Tile[][] tiles;
    private int width;
    private int height;
    public static final int WIDTH = 30;
    public static final int HEIGHT = 30;
    private List<Creature> creatures;

    public static final int TILE_TYPES = 2;

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

    public void dig(int x, int y){
        if(tile(x, y).isWall()){
            tiles[x][y] = Tile.FLOOR;
        }
    }

    public void setWall(int x, int y){
        if(tile(x, y).isGround()){
            // 是平地才能放墙
            tiles[x][y] = Tile.WALL;
        }
    }

    public synchronized void addAtEmptyLocation(Creature creature) {
        int x = 0;
        int y = 0;

        if(creature.type() == CreatureType.MONSTER){
            // 怪物从边上进来
            Random rand = new Random();
            switch (rand.nextInt(4)) {
                case 0:
                    do {
                        x = (int) (Math.random() * this.width);
                        y = 0;
                    } while (!tile(x, y).isGround() || this.creature(x, y) != null);
                    break;
                case 1:
                    do {
                        x = (int) (Math.random() * this.width);
                        y = this.height - 1;
                    } while (!tile(x, y).isGround() || this.creature(x, y) != null);
                    break;
                case 3:
                    do {
                        x = 0;
                        y = (int) (Math.random() * this.height);
                    } while (!tile(x, y).isGround() || this.creature(x, y) != null);
                break;
                case 4:
                    do {
                        x = this.width - 1;
                        y = (int) (Math.random() * this.height);
                    } while (!tile(x, y).isGround() || this.creature(x, y) != null);
                break;
            }
        }
        else{
            do {
                x = (int) (Math.random() * this.width);
                y = (int) (Math.random() * this.height);
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

    public List<Creature> getCreatures() {
        return this.creatures;
    }

    public synchronized void remove(Creature target) {
        this.creatures.remove(target);
    }

    public synchronized void update() {
        ArrayList<Creature> toUpdate = new ArrayList<>(this.creatures);

        for (Creature creature : toUpdate) {
            creature.update();
        }
    }
}
