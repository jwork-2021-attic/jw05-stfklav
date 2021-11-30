package com.anish.maze;

import java.awt.Color;

public class Floor extends Thing {

    public Floor(Color color, World world) {
        super(color, (char) 249, world);
    }

}