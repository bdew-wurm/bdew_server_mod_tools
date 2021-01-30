package net.bdew.wurm.tools.server.internal;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Titles;
import net.bdew.wurm.tools.server.ModData;
import net.bdew.wurm.tools.server.loot.LootManager;
import org.gotti.wurmunlimited.modloader.callbacks.CallbackApi;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Map;
import java.util.function.Consumer;

public class Hooks {
    public static Map<Integer, Consumer<Titles.Title>> titleSetters;

    @CallbackApi
    public void serverStarting() {
        ModData.init();
    }

    @CallbackApi
    public void sendData(long wurmId, DataOutputStream ds) {
        ModData.packToStream(wurmId, ds);
    }

    @CallbackApi
    public void receiveData(long wurmId, DataInputStream ds) {
        ModData.unpackFromStream(wurmId, ds);
    }

    @CallbackApi
    public void deleteData(long wurmId) {
        ModData.deleteAll(wurmId);
    }

    @CallbackApi
    public static void creatureDied(Creature dead, Map<Long, Long> attackers) {
        LootManager.creatureDied(dead, attackers);
    }
}
