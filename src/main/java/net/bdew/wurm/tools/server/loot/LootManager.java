package net.bdew.wurm.tools.server.loot;

import com.wurmonline.server.Players;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import net.bdew.wurm.tools.server.Tools;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LootManager {
    private static final List<LootRule> rules = new LinkedList<>();

    static final Logger logger = Logger.getLogger("LootManager");

    /**
     * Add new loot rule
     */
    public static void add(LootRule rule) {
        rules.add(rule);
    }

    /**
     * This is called internally from hook, don't call directly
     */
    public static void creatureDied(Creature dead, Map<Long, Long> attackers) {
        if (attackers == null || attackers.isEmpty()) return;
        long now = System.currentTimeMillis();
        Set<Player> killers = attackers.entrySet().stream()
                .filter(e -> now - e.getValue() < 600000L && WurmId.getType(e.getKey()) == 0)
                .flatMap(e -> Tools.streamOfNullable(Players.getInstance().getPlayerOrNull(e.getKey())))
                .collect(Collectors.toSet());

        rules.forEach(rule -> {
            if (rule.checkCreature(dead))
                rule.run(dead, killers);
        });
    }
}
