package net.bdew.wurm.tools.server.loot;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;

import java.util.Set;

@FunctionalInterface
public interface LootTrigger {
    void trigger(Creature deadCreature, Set<Player> killers);
}
