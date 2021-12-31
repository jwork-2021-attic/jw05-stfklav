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
}