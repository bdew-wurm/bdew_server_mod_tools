package net.bdew.wurm.tools.server;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import net.bdew.wurm.tools.server.internal.TitleInjector;

public class ModTitles {
    /**
     * Creates a custom title, must be called before server is started (e.g. from init)
     * Multiple overloads below for convenience if you don't need all parameters
     *
     * @param id         Title number
     * @param maleName   Title for male characters
     * @param femaleName Title for female characters (defaults to same as male)
     * @param skillId    Skill number if a title for skill, otherwise -1
     * @param type       Title type NORMAL (for general titles) / MINOR (for 70 skill titles) / MASTER (for 90 skill) / LEGENDARY (for 100 skill)
     */
    public static void addTitle(int id, String maleName, String femaleName, int skillId, String type) {
        try {
            TitleInjector.INSTANCE.addTitle(id, maleName, femaleName, skillId, type);
        } catch (BadBytecode | NotFoundException | CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addTitle(int id, String name, int skillId, String type) {
        addTitle(id, name, name, skillId, type);
    }

    public static void addTitle(int id, String maleName, String femaleName) {
        addTitle(id, maleName, femaleName, -1, "NORMAL");
    }

    public static void addTitle(int id, String name) {
        addTitle(id, name, name, -1, "NORMAL");
    }
}
