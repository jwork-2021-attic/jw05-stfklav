/*
 * Copyright (C) 2015 Aeranythe Echosong
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package world;

import java.util.List;

import asciiPanel.AsciiPanel;

/**
 *
 * @author Aeranythe Echosong
 */
public class CreatureFactory {

    private World world;

    public CreatureFactory(World world) {
        this.world = world;
    }

    public Creature newPlayer(List<String> messages) {
        Creature player = new Creature(this.world, CreatureType.PLAYER, (char)2, AsciiPanel.brightWhite, 100, 20, 0, 5);
        world.addAtEmptyLocation(player);
        new PlayerAI(player, messages);
        return player;
    }

    public Creature newFungus() {
        Creature fungus = new Creature(this.world, CreatureType.FUNGUS, (char)3, AsciiPanel.brightRed, 10, 0, 0, 0);
        world.addAtEmptyLocation(fungus);
        new FungusAI(fungus);
        return fungus;
    }

    public Creature newMedicine() {
        Creature medicine = new Creature(this.world, CreatureType.MEDICINE, (char)8, AsciiPanel.green, 8, 0, 0, 0);
        world.addAtEmptyLocation(medicine);
        new MedicineAI(medicine);
        return medicine;
    }
    public Creature newAmplifier() {
        Creature amplifier = new Creature(this.world, CreatureType.AMPLIFIER, (char)15, AsciiPanel.brightBlue, 8, 0, 0, 0);
        world.addAtEmptyLocation(amplifier);
        new AmplifierAI(amplifier);
        return amplifier;
    }

    public Creature newMonster(Creature player) {
        Creature monster = new Creature(this.world, CreatureType.MONSTER, (char)12, AsciiPanel.brightYellow, 50, 20, 5, 8);
        world.addAtEmptyLocation(monster);
        new MonsterAI(monster, this.world, player);
        return monster;
    }
}
