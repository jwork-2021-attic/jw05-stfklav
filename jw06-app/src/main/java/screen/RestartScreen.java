
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

    @Override
    public Screen respondToUserInput(KeyEvent key) {
        switch (key.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                return new PlayScreen(monster_num, fungus_num, medicine_num, amplifier_num, level);
            default:
                return this;
        }
    }

}
