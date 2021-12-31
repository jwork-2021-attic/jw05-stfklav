package world;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Stack;
import java.util.Random;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class MonsterAI extends CreatureAI {

    private List<String> messages;

    private World world;

    private String plan = "";
    private Creature player;

    public MonsterAI(Creature creature, World world, Creature player) {
        super(creature);
        messages = new ArrayList<String>();
        this.world = world;
        this.player = player;
    }

    public void onEnter(int x, int y, Tile tile) {
        if (tile.isGround()) {
            creature.setX(x);
            creature.setY(y);
        } else if (tile.isWall()) {
            this.creature.dig(x, y);
        }
    }

    public void onNotify(String message) {
        this.messages.add(message);
    }

    public String getPlan() {
        return this.plan;
    }

    private String[] parsePlan(String plan) {
        return plan.split("\n");
    }

    // 上下左右四个方向
    int[][] towards = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };

    private Node start;
    private Node finish;
    private Stack<Node> stack = new Stack<>();

    public void getStep() {
        // 从当前Monster的位置走到player的位置
        plan = "";

        start = new Node(this.creature.x(), this.creature.y());
        finish = new Node(player.x(), player.y());

        stack.push(start);

        int mx = finish.x > start.x ? 1 : -1;
        int my = finish.y > start.y ? 1 : -1;

        int distanceX = Math.abs(finish.x - start.x);
        int distanceY = Math.abs(finish.y - start.y);

        // 先横着走还是先竖着走随机
        Random rand = new Random();
        switch (rand.nextInt(2)) {
            case 0: {
                for (int i = 1; i <= distanceX; ++i) {
                    stack.push(new Node(start.x + mx * i, start.y));
                }
                for (int i = 1; i <= distanceY; ++i) {
                    stack.push(new Node(finish.x, start.y + my * i));
                }
                break;
            }
            case 1: {
                for (int i = 1; i <= distanceY; ++i) {
                    stack.push(new Node(start.x, start.y + my * i));
                }
                for (int i = 1; i <= distanceX; ++i) {
                    stack.push(new Node(start.x + mx * i, finish.y));
                }
                break;
            }
        }

        /*
         * if(this.canSee(player.x(), player.y())){
         * // 怪物能看见玩家，距离近，直接走不尝试绕墙
         * 
         * }else{
         * // 尝试绕开墙壁
         * int [][]status = new int[World.WIDTH][World.HEIGHT];
         * stack.push(start);
         * status[start.x][start.y] = 1;
         * 
         * boolean judge0 = false;
         * 
         * while (!stack.empty() && !judge0) {
         * Node next = stack.peek();
         * //遍历周边节点
         * boolean judge = false;
         * for(int i = 0; i < 4; ++i)
         * {
         * int x0 = next.x + towards[i][0];
         * int y0 = next.y + towards[i][1];
         * if(x0 >= 0 && x0 < World.WIDTH && y0 >= 0 && y0 < World.HEIGHT &&
         * world.tile(x0, y0).isGround() && status[x0][y0] == 0)
         * {//可尝试的节点
         * status[x0][y0] = 1;
         * stack.push(new Node(x0, y0));
         * if(x0 == finish.x && y0 == finish.y)
         * {//找到终点
         * judge0 = true;
         * }
         * judge = true;
         * break;
         * }
         * }
         * if(!judge){
         * stack.pop();
         * }
         * }
         * }
         */

        // 把路径摆正
        Stack<Node> route = new Stack<>();
        while (!stack.empty()) {
            Node tempNode = stack.pop();
            route.push(tempNode);
        }

        Node next;
        while (!route.empty()) {
            next = route.pop();
            plan += "" + next.x + "," + next.y + "\n";
        }
        return;

    }

    private void execute(String step) {
        String[] couple = step.split(",");

        int nextX = Integer.parseInt(couple[0]);
        int nextY = Integer.parseInt(couple[1]);

        int mx = nextX - this.creature.x();
        int my = nextY - this.creature.y();
        creature.moveBy(mx, my);
    }

    @Override
    public void run() {
        while (this.creature.hp() > 0) {
            if (creature.getStatus()) { // 处于运行状态再跑
                getStep();
                String[] walkSteps = parsePlan(plan);
                if (walkSteps.length > 1) {
                    execute(walkSteps[1]);
                }
            }

            try {
                TimeUnit.MILLISECONDS.sleep(400);
            } catch (InterruptedException e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            Thread.yield();
        }
        return;
    }
}

class Node {
    public final int x;
    public final int y;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
