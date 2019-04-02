package net.bdew.wurm.tools.server.journal;

import com.wurmonline.server.Players;
import com.wurmonline.server.players.AchievementTemplate;
import com.wurmonline.server.players.JournalReward;
import com.wurmonline.server.players.JournalTier;
import com.wurmonline.server.players.Player;
import net.bdew.wurm.tools.server.ModData;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModJournalTier extends JournalTier {
    private static final Logger logger = Logger.getLogger("ModJournalTier");
    private JournalReward reward;

    public ModJournalTier(byte tierId, String tierName, byte lastTierId, byte nextTierId, int unlockNextNeeded, int... achievements) {
        super(tierId, tierName, lastTierId, nextTierId, unlockNextNeeded, -1, achievements);
    }

    public ModJournalTier(byte tierId, String tierName, byte lastTierId, byte nextTierId, int unlockNextNeeded, AchievementTemplate... achievements) {
        this(tierId, tierName, lastTierId, nextTierId, unlockNextNeeded,
                Arrays.stream(achievements).mapToInt(AchievementTemplate::getNumber).toArray());
    }

    public ModJournalTier(byte tierId, String tierName, byte lastTierId, byte nextTierId, int unlockNextNeeded, String rewardText, Consumer<Player> reward, int... achievements) {
        this(tierId, tierName, lastTierId, nextTierId, unlockNextNeeded, achievements);
        setReward(new JournalReward(rewardText) {
            @Override
            public void runReward(Player player) {
                reward.accept(player);
            }
        });
    }

    public ModJournalTier(byte tierId, String tierName, byte lastTierId, byte nextTierId, int unlockNextNeeded, String rewardText, Consumer<Player> reward, AchievementTemplate... achievements) {
        this(tierId, tierName, lastTierId, nextTierId, unlockNextNeeded, rewardText, reward,
                Arrays.stream(achievements).mapToInt(AchievementTemplate::getNumber).toArray());
    }


    @Override
    public int getRewardFlag() {
        logger.log(Level.WARNING, "Access to getRewardFlag on a modded journal tier. This shouldn't happen as we don't use flags!", new Exception());
        return super.getRewardFlag();
    }

    @Override
    public boolean hasBeenAwarded(long playerId) {
        return ModData.get(playerId, "bdew.journal.completed" + getTierId())
                .filter(v -> v.equals("Y")).isPresent();
    }

    @Override
    public void setReward(JournalReward jr) {
        super.setReward(jr);
        reward = jr;
    }

    @Override
    public void awardReward(long playerId) {
        Player p = Players.getInstance().getPlayerOrNull(playerId);
        if (p != null) {
            if (this.reward != null) {
                this.reward.runReward(p);
                ModData.set(playerId, "bdew.journal.completed" + getTierId(), "Y");
                p.getCommunicator().sendSafeServerMessage("Congratulations, you fully completed " + this.getTierName() + " and earned the reward: " + this.reward.getRewardDesc(), (byte) 2);
            }

        }
    }
}
