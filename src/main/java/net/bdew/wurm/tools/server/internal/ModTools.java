package net.bdew.wurm.tools.server.internal;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import net.bdew.wurm.tools.server.ServerThreadExecutor;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.ServerPollListener;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;

public class ModTools implements WurmServerMod, PreInitable, ServerPollListener {
    public static final String VERSION = "1.0.0";

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public void preInit() {
        try {
            ClassPool cp = HookManager.getInstance().getClassPool();
            Hooks hooks = new Hooks();

            // ====

            CtClass ctPlayerTransfer = cp.getCtClass("com.wurmonline.server.intra.PlayerTransfer");

            HookManager.getInstance().addCallback(ctPlayerTransfer, "_bdew_tools_cb", hooks);

            ctPlayerTransfer.getMethod("sendItem", "(Lcom/wurmonline/server/items/Item;Ljava/io/DataOutputStream;Z)V")
                    .insertAfter("_bdew_tools_cb.sendData($1.getWurmId(), $2);");

            // ===

            CtClass ctIntraServerConnection = cp.getCtClass("com.wurmonline.server.intra.IntraServerConnection");

            HookManager.getInstance().addCallback(ctIntraServerConnection, "_bdew_tools_cb", hooks);

            ctIntraServerConnection.getMethod("createItem", "(Ljava/io/DataInputStream;FFFLjava/util/Set;Z)V")
                    .insertAfter("_bdew_tools_cb.receiveData(lastItemId, $1);");

            // ===

            CtClass ctServerLauncher = cp.getCtClass("com.wurmonline.server.ServerLauncher");

            HookManager.getInstance().addCallback(ctServerLauncher, "_bdew_tools_cb", hooks);

            ctServerLauncher.getMethod("runServer", "(ZZ)V")
                    .insertBefore("_bdew_tools_cb.serverStarting();");

            // ===

            CtClass ctItems = cp.getCtClass("com.wurmonline.server.Items");

            HookManager.getInstance().addCallback(ctItems, "_bdew_tools_cb", hooks);

            ctItems.getMethod("destroyItem", "(JZZ)V")
                    .insertBefore("_bdew_tools_cb.deleteData($1);");

            // ===

            CtClass ctCreatures = cp.getCtClass("com.wurmonline.server.creatures.Creatures");

            HookManager.getInstance().addCallback(ctCreatures, "_bdew_tools_cb", hooks);

            ctCreatures.getMethod("permanentlyDelete", "(Lcom/wurmonline/server/creatures/Creature;)V")
                    .insertBefore("_bdew_tools_cb.deleteData($1.getWurmId());");

            // ===

            CtClass ctTitle = cp.getCtClass("com.wurmonline.server.players.Titles$Title");

            HookManager.getInstance().addCallback(ctTitle, "_bdew_tools_cb", hooks);

            TitleInjector.INSTANCE = new TitleInjector(ctTitle);

            // ====

            CtClass ctRBP = cp.get("com.wurmonline.server.items.RecipesByPlayer");
            HookManager.getInstance().addCallback(ctRBP, "_bdew_tools_cb", hooks);

            ctRBP.getMethod("packRecipes", "(Ljava/io/DataOutputStream;J)V")
                    .insertAfter("_bdew_tools_cb.sendData($2,$1);");

            ctRBP.getMethod("unPackRecipes", "(Ljava/io/DataInputStream;J)V")
                    .insertAfter("_bdew_tools_cb.receiveData($2,$1);");


        } catch (NotFoundException | CannotCompileException | BadBytecode e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onServerPoll() {
        ServerThreadExecutor.INSTANCE.tick();
    }
}
