package net.bdew.wurm.tools.server.journal;

import com.wurmonline.server.players.JournalTier;
import com.wurmonline.server.players.PlayerJournal;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import java.util.List;
import java.util.Map;

public class ModJournal {
    private static Map<Byte, JournalTier> getTiers() {
        try {
            return ReflectionUtil.getPrivateField(null, ReflectionUtil.getField(PlayerJournal.class, "allTiers"));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Registers a new journal tier, recommended to register in onItemTemplatesCreated
     *
     * @param tier tier to add
     */
    public static void addJournalTier(JournalTier tier) {
        getTiers().put(tier.getTierId(), tier);
    }

    /**
     * Returns an existing journal tier, can be used any time after init
     *
     * @param id tier number
     * @return registered tier or null
     */
    public static JournalTier getJournalTier(byte id) {
        return getTiers().get(id);
    }

    /**
     * Returns the achievement list for a journal tier, the list can be modified to change requirements
     *
     * @param id tier number
     * @return list of achievement ids
     */
    public static List<Integer> getAchievementsFromTier(byte id) {
        JournalTier tier = getJournalTier(id);

        if (tier == null)
            throw new IllegalArgumentException(String.format("Tier %d doesn't exist", id));

        try {
            return ReflectionUtil.getPrivateField(tier, ReflectionUtil.getField(JournalTier.class, "achievementList"));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
