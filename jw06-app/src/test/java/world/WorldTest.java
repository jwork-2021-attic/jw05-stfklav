package world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;
import world.*;
import screen.*;
import asciiPanel.*;

public class WorldTest {

    @Test
    public void SerializingAndDeserializing() throws IOException, ClassNotFoundException {
        World world = new WorldBuilder(World.WIDTH, World.HEIGHT).makeCaves().build();

        Creature player = new Creature(world, CreatureType.PLAYER, (char) 2, AsciiPanel.brightWhite, 100, 20, 0, 5);
        world.addAtEmptyLocation(player);
        new CreatureAI(player);

        Creature fungus = new Creature(world, CreatureType.FUNGUS, (char)3, AsciiPanel.brightRed, 10, 0, 0, 0);
        world.addAtEmptyLocation(fungus);

        FileOutputStream fileOutputStream = new FileOutputStream("yourfile.txt");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(world);
        objectOutputStream.flush();
        objectOutputStream.close();

        FileInputStream fileInputStream = new FileInputStream("yourfile.txt");
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        World world2 = (World) objectInputStream.readObject();
        objectInputStream.close();

        assertEquals(world2.height(), world.height());
        
    }
}