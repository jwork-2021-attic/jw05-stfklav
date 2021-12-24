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
package world;

import java.awt.Color;
import java.awt.event.KeyEvent;

import  java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Aeranythe Echosong
 */

public class Creature implements Runnable{

    private World world;

    private int x;

    public void setX(int x) {
        this.x = x;
    }

    public int x() {
        return x;
    }

    private int y;

    public void setY(int y) {
        this.y = y;
    }

    public int y() {
        return y;
    }

    private char glyph;

    public char glyph() {
        return this.glyph;
    }

    private Color color;

    public Color color() {
        return this.color;
    }

    private CreatureAI ai;

    public void setAI(CreatureAI ai) {
        this.ai = ai;
    }

    private int keyevent;

    public void setKeyEvent(int k){
        this.keyevent = k;
    }

    public int keyevent(){
        return this.keyevent;
    }

    private int maxHP;

    public int maxHP() {
        return this.maxHP;
    }

    private int hp;

    public int hp() {
        return this.hp;
    }

    public void modifyHP(int amount) {
        if(this.hp + amount > this.maxHP){
            this.hp = this.maxHP;
        }
        else{
            this.hp += amount;
        }
        if (this.hp <= 0) {
            world.remove(this);
        }
    }

    private int attackValue;

    public int attackValue() {
        return this.attackValue;
    }

    private int defenseValue;

    public int defenseValue() {
        return this.defenseValue;
    }

    private int visionRadius;

    public int visionRadius() {
        return this.visionRadius;
    }

    public void setVisionRadius(int vr){
        this.visionRadius = vr;
    }

    private CreatureType type; // 生物类型，和ai类型一一对应

    public CreatureType type(){
        return this.type;
    }

    public void setType(CreatureType t) {
        this.type = t;
    }

    public boolean canSee(int wx, int wy) {
        return ai.canSee(wx, wy);
    }

    public Tile tile(int wx, int wy) {
        return world.tile(wx, wy);
    }

    public void dig(int wx, int wy){
        if(this.type == CreatureType.PLAYER){
            this.modifyHP(-10);
        }else if(this.type == CreatureType.MONSTER){
            this.modifyHP(-this.hp);
        }
        world.dig(wx, wy);
    }

    // 向四周布置墙壁
    public synchronized void setWall(int mx, int my){

        Creature other = world.creature(x + mx, y + my);
        if(other == null){
            // 没有creature，可以放wall
            world.setWall(x + mx, y + my);
        }
    }

    public synchronized void moveBy(int mx, int my) {
        Creature other = world.creature(x + mx, y + my);

        if (other == null) {
            ai.onEnter(x + mx, y + my, world.tile(x + mx, y + my));
        }else{
            if(this.type == CreatureType.PLAYER){
                // 只有玩家可以吃
                if(other.type() == CreatureType.FUNGUS || other.type() == CreatureType.MEDICINE ||
                other.type() == CreatureType.AMPLIFIER){
                    eat(other);
                    ai.onEnter(x + mx, y + my, world.tile(x + mx, y + my));
                }else{
                    attack(other);
                }
            }
            else{
                // 怪物只能攻击玩家
                if(other.type() == CreatureType.PLAYER){
                    attack(other);
                }
            }
        } 
    }

    // 攻击别人
    public void attack(Creature other) {
        int damage = Math.max(0, this.attackValue() - other.defenseValue());
        damage = (int) (Math.random() * damage) + 1; // Math.random()生成0~1之间的随机小数

        if(other.beattacked(this, damage)){
            this.notify("You attack the '%s' for %d damage.", other.glyph, damage);
        }
    }

    //被别人攻击
    public boolean beattacked(Creature other, int damage){
        // 攻击超过防御值才接受伤害
        if(damage > this.defenseValue()){
            this.modifyHP(-damage);
            this.notify("The '%s' attacks you for %d damage.", other.glyph, damage);
            return true;
        }
        return false;
    }

    // 被治愈
    public void behealed(Creature other){
        int heal = other.hp;

        if(this.hp < this.maxHP){
            this.modifyHP(heal);
        }
    }

    //吃掉fungus或medicine
    public void eat(Creature other){
        if(other.type() == CreatureType.FUNGUS){
            world.remove(other);
        }
        else if(other.type() == CreatureType.MEDICINE){
            this.behealed(other);
            world.remove(other);
        }else if(other.type() == CreatureType.AMPLIFIER){
            setVisionRadius(this.visionRadius * 2);
            new Timer().schedule(new TimerTask() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    setVisionRadius(visionRadius / 2);
                }
                
            }, 5000);
            world.remove(other);
        }
        
    }

    public void update() {
        this.ai.onUpdate();
    }

    public boolean canEnter(int x, int y) {
        return world.tile(x, y).isGround();
    }

    public void notify(String message, Object... params) {
        ai.onNotify(String.format(message, params));
    }

    public Creature(World world, CreatureType type, char glyph, Color color, int maxHP, int attack, int defense, int visionRadius) {
        this.world = world;
        this.type = type;
        this.glyph = glyph;
        this.color = color;
        this.maxHP = maxHP;
        this.hp = maxHP;
        this.attackValue = attack;
        this.defenseValue = defense;
        this.visionRadius = visionRadius;
        this.keyevent = KeyEvent.VK_ENTER;
    }

    @Override
    public void run() {
       ai.run();
    }
}
