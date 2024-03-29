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
package screen;

import world.*;
import asciiPanel.AsciiPanel;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


import  java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Aeranythe Echosong
 */
public class PlayScreen implements Screen {

    private World world;
    private Creature player;
    private List<String> messages;
    private List<String> oldMessages;
    
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

        createWorld();
        this.messages = new ArrayList<String>();
        this.oldMessages = new ArrayList<String>();

        CreatureFactory creatureFactory = new CreatureFactory(this.world);
        createCreatures(creatureFactory);
    }

    private void createCreatures(CreatureFactory creatureFactory) {
        //this.player = creatureFactory.newPlayer(this.messages);
        
        // 开创线程池创造怪物
        ExecutorService exec = Executors.newFixedThreadPool(MONSTER_NUMBER + 1);
        this.player = creatureFactory.newPlayer(this.messages);
        exec.execute(player);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub 
                if(monsterremain() < MONSTER_NUMBER){
                    exec.execute(creatureFactory.newMonster(player));
                }
            }
            
        }, 5000, 5000);
        //exec.shutdown();
        
        for (int i = 0; i < FUNGUS_NUMBER; i++) {
            creatureFactory.newFungus();
        }

        for(int i = 0; i < MEDICINE_NUMBER; i++){
            creatureFactory.newMedicine();
        }

        for(int i = 0; i < AMPLIFIER_NUMBER; i++){
            creatureFactory.newAmplifier();
        }
    }

    private void createWorld() {
        world = new WorldBuilder(World.WIDTH, World.HEIGHT).makeCaves().build();
    }

    // 统计剩余fungus个数
    private synchronized int fungusremain(){
        int count = 0;
        for(Creature c: world.getCreatures()){
            if(c.type() == CreatureType.FUNGUS){
                ++count;
            }
        }
        return count;
    }

    // 统计剩余怪物数
    private synchronized int monsterremain(){
        int count = 0;
        for(Creature c: world.getCreatures()){
            if(c.type() == CreatureType.MONSTER){
                ++count;
            }
        }
        return count;
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
            if(creature.type() == CreatureType.FUNGUS || creature.type() == CreatureType.MEDICINE||
            creature.type() == CreatureType.AMPLIFIER){
                // 照亮才能看见
                if (creature.x() >= 0 && creature.x() < World.WIDTH && creature.y() >= 0
                    && creature.y() < World.HEIGHT) {
                    if (player.canSee(creature.x(), creature.y())) {
                        terminal.write(creature.glyph(), creature.x(), creature.y(), creature.color());
                    }
                }
            }
            else{
                 // 不用照亮就能看见
                if (creature.x() >= 0 && creature.x() < World.WIDTH && creature.y() >= 0
                    && creature.y() < World.HEIGHT){
                        terminal.write(creature.glyph(), creature.x(), creature.y(), creature.color());
                    }   
            }
        }
        // Creatures can choose their next action now
        world.update();
    }

    private void displayMessages(AsciiPanel terminal, List<String> messages) {
        int top = World.HEIGHT + 5;
        for (int i = 0; i < messages.size(); i++) {
            terminal.write(messages.get(i), 1, top + i);
        }
        oldMessages.addAll(messages);
        messages.clear();
        if(oldMessages.size() > 0){
            messages.add(oldMessages.get(oldMessages.size() - 1)) ;
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

         //判断player是不是已经死了
         if(player.hp() <= 0){
            return new LoseScreen();
        }else if(fungusremain() == 0){
            // 是否赢得胜利
            return new WinScreen();
        }
        return this;
    }

    @Override
    public Screen respondToUserInput(KeyEvent key) {
        player.setKeyEvent(key.getKeyCode());
        /*switch (key.getKeyCode()) {
            // 上下左右移动
            case KeyEvent.VK_LEFT:
                player.moveBy(-1, 0);
                break;
            case KeyEvent.VK_RIGHT:
                player.moveBy(1, 0);
                break;
            case KeyEvent.VK_UP:
                player.moveBy(0, -1);
                break;
            case KeyEvent.VK_DOWN:
                player.moveBy(0, 1);
                break;
            // w a s d放墙
            case KeyEvent.VK_A:
                player.setWall(-1, 0);
                break;
            case KeyEvent.VK_D:
                player.setWall(1, 0);
                break;
            case KeyEvent.VK_W:
                player.setWall(0, -1);
                break;
            case KeyEvent.VK_S:
                player.setWall(0, 1);
                break;
        }*/
        return this;
    }


}
