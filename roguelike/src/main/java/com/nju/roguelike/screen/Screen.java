package com.nju.roguelike.screen;

import com.nju.roguelike.world.*;

import asciiPanel.AsciiPanel;
import java.awt.event.KeyEvent;

public interface Screen {
    public static final int WIDTH = World.WIDTH + 10;
    public static final int HEIGHT = World.HEIGHT + 10;

    public static final String FileName = "gamefile.txt";

    public Screen displayOutput(AsciiPanel terminal);

    public Screen respondToUserInput(KeyEvent key);
}
