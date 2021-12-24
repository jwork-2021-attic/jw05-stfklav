package world;


public class MedicineAI extends CreatureAI {

    private CreatureFactory factory;
    private int spreadcount = 0;

    public static int spores = 3;
    public static double spreadchance = 0.01;

    public MedicineAI(Creature creature, CreatureFactory factory) {
        super(creature);
        this.factory = factory;
    }

    public void onUpdate() {
        /*if (this.spreadcount < MedicineAI.spores && Math.random() < MedicineAI.spreadchance) {
            spread();
        }*/
    }

    private void spread() {
        int newx = creature.x() + (int) (Math.random() * 11) - 5;
        int newy = creature.y() + (int) (Math.random() * 11) - 5;

        if (!creature.canEnter(newx, newy)) {
            return;
        }

        Creature child = this.factory.newMedicine();
        //child.setX(newx);
        //child.setY(newy);
        spreadcount++;
    }
}