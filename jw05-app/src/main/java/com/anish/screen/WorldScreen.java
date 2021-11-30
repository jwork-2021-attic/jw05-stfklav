package com.anish.screen;

import java.awt.Color;
import java.awt.event.KeyEvent;

import com.anish.maze.*;

import asciiPanel.AsciiPanel;

public class WorldScreen implements Screen {

    private World world;
    String[] walkSteps;
    private Calabash bro;
    private int[][] maze;
    private Node start;
    private Node finish;

    public WorldScreen() {
        world = new World();
        bro = new Calabash(new Color(255,218,185), 0, world);
        Maze theMaze = new Maze(world);//生成迷宫
        maze = theMaze.getMaze();
        start = theMaze.getStart();
        finish = theMaze.getFinish();

        theMaze.solve();
        
        walkSteps = this.parsePlan(theMaze.getPlan());
    }

    private String[] parsePlan(String plan) {
        return plan.split("\n");
    }

    private void execute(String step) {
        String[] couple = step.split(",");

        if(bro.getTile() != null){  
            if(bro.getX() == start.x && bro.getY() == start.y){
                world.put(new Flag(new Color(0,255,0), world), start.x, start.y);
            }else{
                world.put(new Floor(new Color(0,255,0), this.world), bro.getX(), bro.getY());
            }
        }
        int nextX = Integer.parseInt(couple[1]);
        int nextY = Integer.parseInt(couple[0]);
        if(nextX == finish.x && nextY == finish.y){//最后一步把红心换成结束标志
            world.put(new Creature(new Color(0,255,0), (char)14, world), nextX, nextY);
        }else{
            world.put(bro, nextX, nextY);
        }
        
    }
    @Override
    public void displayOutput(AsciiPanel terminal) {

        for (int x = 0; x < World.WIDTH; x++) {
            for (int y = 0; y < World.HEIGHT; y++) {

                terminal.write(world.get(x, y).getGlyph(), x, y, world.get(x, y).getColor());

            }
        }
    }

    public void reset()
    {//重置原来生成的迷宫
        for(int i = 0; i<World.HEIGHT; ++i)
        {
            for(int j = 0; j< World.WIDTH; ++j)
            {
                if(maze[i][j] == 1){
                    world.put(new Floor(new Color(128,128,128), this.world), j, i);
                }
            }
            world.put(new Flag(new Color(0,255,0), world), start.x, start.y);
            world.put(new Flag(new Color(220,20,60), world), finish.x, finish.y);
        }
    }

    private int index = 0;

    @Override
    public Screen respondToUserInput(KeyEvent key) {
        if (index < this.walkSteps.length) {
            this.execute(walkSteps[index]);
            index++;
        }
        else
        {
            index = 0;
            bro = new Calabash(new Color(255,218,185), 0, world);
            reset();
        }
        return this;
    }

}
