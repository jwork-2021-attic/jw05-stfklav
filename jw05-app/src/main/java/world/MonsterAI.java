package world;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;


public class MonsterAI extends CreatureAI{

    private List<String> messages;

    public MonsterAI(Creature creature) {
        super(creature);
        messages = new ArrayList<String>();
    }

    public void onEnter(int x, int y, Tile tile) {
        if (tile.isGround()) {
            creature.setX(x);
            creature.setY(y);
        } 
    }

    public void onNotify(String message) {
        this.messages.add(message);
    }

    @Override
    public void run() {
        
        while(true){
            this.creature.moveBy(-1, 0);
            this.creature.moveBy(0, 1);
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                //TODO: handle exception
                e.printStackTrace();
            }
        }
        
    }
}
