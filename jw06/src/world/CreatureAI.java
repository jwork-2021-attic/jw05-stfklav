package world;

public class CreatureAI {

    protected Creature creature;

    public CreatureAI(Creature creature) {
        this.creature = creature;
        this.creature.setAI(this);
    }

    public void onEnter(int x, int y, Tile tile) {
    }

    public void onUpdate() {
    }

    public void onNotify(String message) {
    }

    public boolean canSee(int x, int y) {
        if ((creature.x() - x) * (creature.x() - x) + (creature.y() - y) * (creature.y() - y) > creature.visionRadius()
                * creature.visionRadius()) {
            return false;
        }
        /*for (Point p : new Line(creature.x(), creature.y(), x, y)) {
            if (creature.tile(p.x, p.y).isGround() || (p.x == x && p.y == y)) {
                continue;
            }
            return false;
        }*/
        return true;
    }

    public void run() {
    }
}
