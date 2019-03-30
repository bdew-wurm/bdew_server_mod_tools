package net.bdew.wurm.tools.server;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Achievement;
import com.wurmonline.server.players.AchievementTemplate;
import com.wurmonline.server.players.Achievements;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import java.lang.reflect.InvocationTargetException;

public class ModAchievements {
    /**
     * Registers a new achievement, should be called from onItemTemplatesCreated, do not call during (pre)init!
     */
    public static AchievementTemplate addTemplate(int id, String name, String description, boolean isInvisible, int triggerOn, byte achievementType, boolean playUpdateSound, boolean isOneTimer) {
        AchievementTemplate ach = new AchievementTemplate(id, name, isInvisible, triggerOn, achievementType, playUpdateSound, isOneTimer);
        ach.setDescription(description);
        try {
            ReflectionUtil.callPrivateMethod(null, ReflectionUtil.getMethod(Achievement.class, "addTemplate", new Class<?>[]{AchievementTemplate.class}), ach);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return ach;
    }

    /**
     * Gives an achievement to a player, and returns how many times he already received it.
     * If you don't care about the count you can just call Achievements.triggerAchievement
     */
    public static int triggerAndGetCount(AchievementTemplate tpl, Creature player) {
        Achievements.triggerAchievement(player.getWurmId(), tpl.getNumber());
        Achievements achievements = Achievements.getAchievementObject(player.getWurmId());
        Achievement achievement = achievements.getAchievement(tpl.getNumber());
        return achievement.getCounter();
    }
}
