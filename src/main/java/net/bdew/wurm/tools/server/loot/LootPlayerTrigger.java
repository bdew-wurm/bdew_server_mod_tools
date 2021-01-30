package net.bdew.wurm.tools.server.loot;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;

@FunctionalInterface
public interface LootPlayerTrigger {
    void trigger(Creature deadCreature, Player killer);
}
