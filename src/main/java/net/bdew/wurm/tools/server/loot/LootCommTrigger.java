package net.bdew.wurm.tools.server.loot;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;

@FunctionalInterface
public interface LootCommTrigger {
    void trigger(Creature deadCreature, Communicator comm);
}
