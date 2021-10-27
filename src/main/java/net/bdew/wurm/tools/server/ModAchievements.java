package net.bdew.wurm.tools.server;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Achievement;
import com.wurmonline.server.players.AchievementTemplate;
import com.wurmonline.server.players.Achievements;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;

import java.lang.reflect.InvocationTargetException;

public class ModAchievements {
    public static class Builder {
        private final int id;
        private String name, description = "", requirement = "";
        private boolean invisible, showUpdatePopup, oneTimer, isForCooking, isInLiters;
        private int triggerOn = 1;
        private int[] achievementsTriggered = MiscConstants.EMPTY_INT_ARRAY;
        private int[] requiredAchievements = MiscConstants.EMPTY_INT_ARRAY;
        private byte achievementType = 3;

        public Builder(int id) {
            this.id = id;
        }

        /**
         * Sets the name of the achievement
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the description of the achievement (as seen after completing)
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the requirement of the achievement (as seen before completion - e.g. in journal)
         */
        public Builder requirement(String requirement) {
            this.requirement = requirement;
            return this;
        }

        /**
         * Sets if the achievement is not visible to players
         */
        public Builder invisible(boolean invisible) {
            this.invisible = invisible;
            return this;
        }

        /**
         * Sets if the achievement should show a popup when the count is updated
         */
        public Builder showUpdatePopup(boolean showUpdatePopup) {
            this.showUpdatePopup = showUpdatePopup;
            return this;
        }

        /**
         * Sets if the achievement should only count once
         */
        public Builder oneTimer(boolean oneTimer) {
            this.oneTimer = oneTimer;
            return this;
        }

        /**
         * Sets if the achievement is for cooking (for use with custom recipes)
         */
        public Builder forCooking(boolean isForCooking) {
            this.isForCooking = isForCooking;
            return this;
        }

        /**
         * Sets if the achievement counts liters/kilograms instead of items (for use with custom recipes)
         */
        public Builder inLiters(boolean isInLiters) {
            this.isInLiters = isInLiters;
            return this;
        }

        /**
         * Sets how many times other achievements need to trigger before this achievement is activated
         * For use with {@link #achievementsTriggered} and {@link #requiredAchievements}
         */
        public Builder triggerOn(int triggerOn) {
            this.triggerOn = triggerOn;
            return this;
        }

        /**
         * Set type (Use constants from MiscConstants)
         * A_TYPE_DIAMOND / A_TYPE_GOLD / A_TYPE_SILVER
         */
        public Builder achievementType(byte achievementType) {
            this.achievementType = achievementType;
            return this;
        }

        /**
         * Set achievements triggered by this one
         */
        public Builder achievementsTriggered(int... ids) {
            achievementsTriggered = ids;
            return this;
        }

        /**
         * Set achievements required by this one
         */
        public Builder requiredAchievements(int... ids) {
            requiredAchievements = ids;
            return this;
        }

        /**
         * Creates and registers the achievement template.
         * Should be called from onItemTemplatesCreated, do not call during (pre)init!
         *
         * @return created and registered {@link AchievementTemplate}
         */
        public AchievementTemplate buildAndRegister() {
            AchievementTemplate ach = new AchievementTemplate(id, name, invisible, triggerOn, achievementType, showUpdatePopup, oneTimer, requirement);
            ach.setDescription(description);
            ach.setIsForCooking(isForCooking);
            ach.setIsInLiters(isInLiters);
            ach.setAchievementsTriggered(achievementsTriggered);
            ach.setRequiredAchievements(requiredAchievements);
            try {
                ReflectionUtil.callPrivateMethod(null, ReflectionUtil.getMethod(Achievement.class, "addTemplate", new Class<?>[]{AchievementTemplate.class}), ach);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            return ach;
        }
    }

    /**
     * Creates a new achievement builder
     */
    public static Builder build(int id) {
        return new Builder(id);
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
