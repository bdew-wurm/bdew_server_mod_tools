package net.bdew.wurm.tools.server.loot;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;

@FunctionalInterface
public interface LootPredicate {
    boolean test(Creature deadCreature, Player killer);

    default LootPredicate and(LootPredicate other) {
        return (c, k) -> test(c, k) && other.test(c, k);
    }
}
