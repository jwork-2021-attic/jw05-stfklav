
package screen;

import asciiPanel.AsciiPanel;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public abstract class RestartScreen implements Screen {

    protected static int monster_num = 3;
    protected static int fungus_num = 3;
    protected static int medicine_num = 8;
    protected static int amplifier_num = 3;

    protected static int level = 0;

    @Override
    public abstract Screen displayOutput(AsciiPanel terminal);

    // 反序列化
     public Screen deserializing() throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(Screen.FileName);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        PlayScreen p = (PlayScreen) objectInputStream.readObject();
        monster_num = p.MONSTER_NUMBER;
        fungus_num = p.FUNGUS_NUMBER;
        medicine_num = p.MEDICINE_NUMBER;
        amplifier_num = p.AMPLIFIER_NUMBER;
        level = p.level;
        p.run();
        objectInputStream.close();
        return p;
    }

    @Override
    public Screen respondToUserInput(KeyEvent key) {
        if (key.isControlDown() && key.getKeyCode() == KeyEvent.VK_S) {// 同时按下ctrl+S
            try {
                return deserializing();
            } catch (Exception e) {
                System.out.println("反序列化失败！");
            }
        } else {
            switch (key.getKeyCode()) {
                case KeyEvent.VK_ENTER:
                    return new PlayScreen(monster_num, fungus_num, medicine_num, amplifier_num, level);
                default:
                    return this;
            }
        }
        return this;
    }

}
