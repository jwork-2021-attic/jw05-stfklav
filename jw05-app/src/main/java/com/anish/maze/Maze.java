package com.anish.maze;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Random;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class Maze {
    private World world;
    private int[][] maze;
    private Node start;
    private Node finish;
    private Stack<Node> stack = new Stack<>();
    Queue<Node> queue = new LinkedList<Node>();

    public Maze(World w)
    {
        world = w;
        maze = new int[world.HEIGHT][world.WIDTH];
      
        //生成一个迷宫
        MazeGenerator mazeGenerator = new MazeGenerator(World.HEIGHT);
        mazeGenerator.generateMaze();

        maze = mazeGenerator.getMaze();
        for(int i = 0; i<World.HEIGHT; ++i)
        {
            for(int j = 0; j< World.WIDTH; ++j)
            {
                if(maze[i][j] == 1){
                    world.put(new Floor(new Color(128,128,128), this.world), j, i);
                }
            }
        }
        Random random = new Random();
        int temp = random.nextInt(World.WIDTH);
        if(maze[0][temp] == 1){
            world.put(new Flag(new Color(0,255,0), world), temp, 0);
        }else{
            while(maze[0][temp] != 1){
                temp = (temp+1) % World.WIDTH;
            }
            world.put(new Flag(new Color(0,255,0), world), temp, 0);
        }
        start = new Node(temp, 0);

        temp = random.nextInt(World.WIDTH);
        if(maze[World.HEIGHT - 1][temp] == 1){
            world.put(new Flag(new Color(220,20,60), world), temp, world.HEIGHT - 1);
        }else{
            while(maze[world.HEIGHT - 1][temp] != 1){
                temp = (temp+1) % world.WIDTH;
            }
            world.put(new Flag(new Color(220,20,60), world), temp, world.HEIGHT - 1);
        }
        finish = new Node(temp, world.HEIGHT - 1);
    }


    private String plan = "";

    //上下左右四个方向
    int [][]towards = {{1, 0}, {-1, 0}, {0, 1}, {0,-1}};

    public void solve()
    {//从start走到finish，bfs

        //状态变化，0表示尚未遍历到，1表示已遍历
        int [][]status = new int[world.HEIGHT][world.WIDTH];
        stack.push(start);
        status[start.y][start.x] = 1;

        boolean judge0 = false;

        while (!stack.empty() && !judge0) {
            Node next = stack.peek();
            //遍历周边节点
            boolean judge = false;
            for(int i=0; i<4; ++i)
            {
                int x0 = next.x + towards[i][1];
                int y0 = next.y + towards[i][0];
                if(x0>=0 && x0<World.WIDTH && y0>=0 && y0<world.HEIGHT 
                && maze[y0][x0] == 1 && status[y0][x0] == 0)
                {//可尝试的节点
                    status[y0][x0] = 1;
                    stack.push(new Node(x0, y0));
                    if(x0 == finish.x && y0 == finish.y)
                    {//找到终点
                        judge0 = true;
                    }
                    judge = true;
                    break;
                }
            }
            if(!judge){
                stack.pop();
            }
        }

        //把路径摆正
        Stack<Node> route = new Stack<>();
        if (stack.empty()) {
            System.out.println("错误！没有路径");
            return;
           } else {
            while (!stack.empty()) {
             Node tempNode = stack.pop();  
             route.push(tempNode);
            }
        }
        
        Node next;
        while(!route.empty()){
            next = route.pop();
            plan += "" + next.y + "," + next.x + "\n";
        }
        return;
        
        /*stack.push(start);
        status[start.y][start.x] = 1;
        while (!stack.empty()) {
            Node next = stack.pop();
            plan += "" + next.y + "," + next.x + "\n";
            //遍历周边节点
            for(int i=0; i<4; ++i)
            {
                int x0 = next.x + towards[i][1];
                int y0 = next.y + towards[i][0];
                if(x0>=0 && x0<World.WIDTH && y0>=0 && y0<world.HEIGHT 
                && maze[y0][x0] == 1 && status[y0][x0] == 0)
                {
                    status[y0][x0] = 1;
                    if(x0 == finish.x && y0 == finish.y)
                    {//找到终点
                        plan += "" + finish.y + "," + finish.x + "\n";
                        return;
                    }
                    else
                    {
                        stack.push(new Node(x0, y0));
                    }
                }
            }
        }*/
        /*queue.offer(start);
        status[start.y][start.x] = 1;
        while(!queue.isEmpty()){
            Node next = queue.poll();
            plan += "" + next.y + "," + next.x + "\n";
            for(int i=0; i<4; ++i)
            {
                int x0 = next.x + towards[i][1];
                int y0 = next.y + towards[i][0];
                if(x0>=0 && x0<World.WIDTH && y0>=0 && y0<world.HEIGHT 
                && maze[y0][x0] == 1 && status[y0][x0] == 0)
                {
                    status[y0][x0] = 1;
                    if(x0 == finish.x && y0 == finish.y)
                    {//找到终点
                        plan += "" + finish.y + "," + finish.x + "\n";
                        return;
                    }
                    else
                    {
                        queue.offer(new Node(x0, y0));
                    }
                }
            }
        }*/

    }
    
    public String getPlan() {
        return this.plan;
    }

    public int[][] getMaze(){
        return maze;
    }

    public Node getStart(){
        return start;
    }

    public Node getFinish(){
        return finish;
    }
}
