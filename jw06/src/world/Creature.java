package world;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

public class Creature implements Runnable, Serializable {

    private transient World world;

    private int x;
    private int y;
    private char glyph;
    private Color color;
    private transient CreatureAI ai;

    private int keyevent; // 通过键盘事件控制
    boolean status = true; // 是否正在运行，由空格键控制

    private int maxHP;
    private int hp;
    private int attackValue;
    private int defenseValue;
    private int visionRadius; // 视野范围
    private CreatureType type; // 生物类型，和ai类型一一对应

    public Creature(){

    }

    public void setWorld(World w) {
        this.world = w;
    }

    public void setX(int x) {
        this.x = x;
    }


    public int x() {
        return x;
    }

    public void setY(int y) {
        this.y = y;
    }


    public int y() {
        return y;
    }

    public char glyph() {
        return this.glyph;
    }

    public Color color() {
        return this.color;
    }

    public void setAI(CreatureAI ai) {
        this.ai = ai;
    }

    public void setKeyEvent(int k) {
        this.keyevent = k;
    }

    public int keyevent() {
        return this.keyevent;
    }

    public boolean getStatus() {
        return this.status;
    }

    public void setStatus(boolean s) {
        this.status = s;
    }

    public int maxHP() {
        return this.maxHP;
    }

    public int hp() {
        return this.hp;
    }

    public void modifyHP(int amount) {
        if (this.hp + amount > this.maxHP) {
            this.hp = this.maxHP;
        } else {
            this.hp += amount;
        }
        if (this.hp <= 0) {
            world.remove(this);
        }
    }

    public int attackValue() {
        return this.attackValue;
    }

    public int defenseValue() {
        return this.defenseValue;
    }

    public int visionRadius() {
        return this.visionRadius;
    }

    public void setVisionRadius(int vr) {
        this.visionRadius = vr;
    }

    public CreatureType type() {
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

    public void dig(int wx, int wy) {
        if (this.type == CreatureType.PLAYER) {
            this.modifyHP(-10);
        } else if (this.type == CreatureType.MONSTER) {
            this.modifyHP(-this.hp);
        }
        world.dig(wx, wy);
    }

    // 向四周布置墙壁
    public synchronized void setWall(int mx, int my) {

        Creature other = world.creature(x + mx, y + my);
        if (other == null) {
            // 没有creature，可以放wall
            world.setWall(x + mx, y + my);
        }
    }

    public synchronized void moveBy(int mx, int my) {
        Creature other = world.creature(x + mx, y + my);

        if (other == null) {
            ai.onEnter(x + mx, y + my, world.tile(x + mx, y + my));
        } else {
            if (this.type == CreatureType.PLAYER) {
                // 只有玩家可以吃
                if (other.type() == CreatureType.FUNGUS || other.type() == CreatureType.MEDICINE ||
                        other.type() == CreatureType.AMPLIFIER) {
                    eat(other);
                    ai.onEnter(x + mx, y + my, world.tile(x + mx, y + my));
                } else {
                    attack(other);
                }
            } else {
                // 怪物只能攻击玩家
                if (other.type() == CreatureType.PLAYER) {
                    attack(other);
                }
            }
        }
    }

    // 攻击别人
    public void attack(Creature other) {
        int damage = Math.max(0, this.attackValue() - other.defenseValue());
        damage = (int) (Math.random() * damage) + 1; // Math.random()生成0~1之间的随机小数

        if (other.beattacked(this, damage)) {
            this.notify("You attack the '%s' for %d damage.", other.glyph, damage);
        }
    }

    // 被别人攻击
    public boolean beattacked(Creature other, int damage) {
        // 攻击超过防御值才接受伤害
        if (damage > this.defenseValue()) {
            this.modifyHP(-damage);
            this.notify("The '%s' attacks you for %d damage.", other.glyph, damage);
            return true;
        }
        return false;
    }

    // 被治愈
    public void behealed(Creature other) {
        int heal = other.hp;

        if (this.hp < this.maxHP) {
            this.modifyHP(heal);
        }
    }

    // 吃掉fungus或medicine
    public void eat(Creature other) {
        if (other.type() == CreatureType.FUNGUS) {
            world.remove(other);
        } else if (other.type() == CreatureType.MEDICINE) {
            this.behealed(other);
            world.remove(other);
        } else if (other.type() == CreatureType.AMPLIFIER) {
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

    public Creature(World world, CreatureType type, char glyph, Color color, int maxHP, int attack, int defense,
            int visionRadius) {
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

    // setters and getters 序列化输出输入
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }

    private synchronized void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
    }

    @Override
    public void run() {
        ai.run();
    }
}
