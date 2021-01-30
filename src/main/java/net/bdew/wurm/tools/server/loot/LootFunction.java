package net.bdew.wurm.tools.server.loot;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;

@FunctionalInterface
public interface LootFunction<T> {
    T apply(Creature deadCreature, Player killer);
}
