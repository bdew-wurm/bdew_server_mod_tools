package net.bdew.wurm.tools.server.loot;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Player;
import com.wurmonline.shared.constants.ItemMaterials;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

public class LootDrop implements LootFunction<Collection<Item>> {
    private final LootFunction<Integer> templateGen;
    private LootFunction<Integer> repeatGen = (c, k) -> 1;
    private LootFunction<Float> qlGen = (c, k) -> 99f;
    private LootFunction<Byte> rarityGen = (c, k) -> MiscConstants.COMMON;
    private LootFunction<Byte> materialGen = (c, k) -> ItemMaterials.MATERIAL_UNDEFINED;
    private LootFunction<String> creatorGen = (c, k) -> k.getName();

    private LootFunction<Byte> auxGen = null;
    private LootFunction<Integer> data1Gen = null;
    private LootFunction<Integer> data2Gen = null;
    private LootFunction<Integer> weightGen = null;
    private LootFunction<Float> damageGen = null;
    private LootFunction<String> nameGen = null;
    private LootFunction<Integer> realTplGen = null;

    private LootDrop(LootFunction<Integer> templateId) {
        this.templateGen = templateId;
    }

    /**
     * Set number of drops from constant
     */
    public LootDrop repeat(LootFunction<Integer> repeat) {
        this.repeatGen = repeat;
        return this;
    }

    /**
     * Set number of drops function
     */
    public LootDrop repeat(int _repeat) {
        return repeat((c, k) -> _repeat);
    }

    /**
     * Set ql from constant
     */
    public LootDrop ql(LootFunction<Float> ql) {
        this.qlGen = ql;
        return this;
    }

    /**
     * Set ql function
     */
    public LootDrop ql(float _ql) {
        return ql((c, k) -> _ql);
    }

    /**
     * Set rarity from constant
     */
    public LootDrop rarity(LootFunction<Byte> rarity) {
        this.rarityGen = rarity;
        return this;
    }

    /**
     * Set rarity function
     */
    public LootDrop rarity(byte _rarity) {
        return rarity((c, k) -> _rarity);
    }

    /**
     * Set material from constant
     */
    public LootDrop material(LootFunction<Byte> material) {
        this.materialGen = material;
        return this;
    }

    /**
     * Set material function
     */
    public LootDrop material(byte _material) {
        return material((c, k) -> _material);
    }

    /**
     * Set creator from constant
     */
    public LootDrop creator(LootFunction<String> creator) {
        this.creatorGen = creator;
        return this;
    }

    /**
     * Set creator function
     */
    public LootDrop creator(String _creator) {
        return creator((c, k) -> _creator);
    }

    /**
     * Set aux from constant
     */
    public LootDrop aux(LootFunction<Byte> aux) {
        this.auxGen = aux;
        return this;
    }

    /**
     * Set aux function
     */
    public LootDrop aux(byte _aux) {
        return aux((c, k) -> _aux);
    }

    /**
     * Set data1 from constant
     */
    public LootDrop data1(LootFunction<Integer> data1) {
        this.data1Gen = data1;
        return this;
    }

    /**
     * Set data1 function
     */
    public LootDrop data1(int _data1) {
        return data1((c, k) -> _data1);
    }

    /**
     * Set data2 from constant
     */
    public LootDrop data2(LootFunction<Integer> data2) {
        this.data2Gen = data2;
        return this;
    }

    /**
     * Set data2 function
     */
    public LootDrop data2(int _data2) {
        return data2((c, k) -> _data2);
    }

    /**
     * Set weight from constant
     */
    public LootDrop weight(LootFunction<Integer> weight) {
        this.weightGen = weight;
        return this;
    }

    /**
     * Set weight function
     */
    public LootDrop weight(int _weight) {
        return weight((c, k) -> _weight);
    }

    /**
     * Set damage from constant
     */
    public LootDrop damage(LootFunction<Float> damage) {
        this.damageGen = damage;
        return this;
    }

    /**
     * Set damage function
     */
    public LootDrop damage(float _damage) {
        return damage((c, k) -> _damage);
    }

    /**
     * Set name from constant
     */
    public LootDrop name(LootFunction<String> name) {
        this.nameGen = name;
        return this;
    }

    /**
     * Set name function
     */
    public LootDrop name(String _name) {
        return name((c, k) -> _name);
    }

    /**
     * Set template from constant
     */
    public LootDrop realTemplate(LootFunction<Integer> template) {
        this.realTplGen = template;
        return this;
    }

    /**
     * Set template function
     */
    public LootDrop realTemplate(int _template) {
        return realTemplate((c, k) -> _template);
    }

    public static LootDrop create(LootFunction<Integer> templateId) {
        return new LootDrop(templateId);
    }

    public static LootDrop create(int templateId) {
        return new LootDrop((c, k) -> templateId);
    }

    /**
     * Helper for static drop with just item template id
     * the generated item will be 99ql, common and default material
     * WARNING: All values passed here will be evaluated only once,
     * if you want random or any kind of logic use a lambda instead
     */
    static LootDrop staticDrop(int templateId) {
        return create(templateId);
    }

    /**
     * Helper for static drop with item template id and ql
     * the generated item will be common and have default material
     * WARNING: All values passed here will be evaluated only once,
     * if you want random or any kind of logic use a lambda instead
     */
    static LootDrop staticDrop(int templateId, float ql) {
        return create(templateId).ql(ql);
    }

    /**
     * Helper for static drop with item template id, ql, rarity
     * the generated item will have default material
     * WARNING: All values passed here will be evaluated only once,
     * if you want random or any kind of logic use a lambda instead
     */
    static LootDrop staticDrop(int templateId, float ql, byte rarity) {
        return create(templateId).ql(ql).rarity(rarity);
    }

    /**
     * Helper for static drop with item template td, ql, rarity and material
     * WARNING: All values passed here will be evaluated only once,
     * if you want random or any kind of logic use a lambda instead
     */
    static LootDrop staticDrop(int templateId, float ql, byte rarity, byte material) {
        return create(templateId).ql(ql).rarity(rarity).material(material);
    }

    @Override
    public Collection<Item> apply(Creature deadCreature, Player killer) {
        int num = repeatGen.apply(deadCreature, killer);
        ArrayList<Item> res = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            try {
                Item item = ItemFactory.createItem(
                        templateGen.apply(deadCreature, killer),
                        qlGen.apply(deadCreature, killer),
                        materialGen.apply(deadCreature, killer),
                        rarityGen.apply(deadCreature, killer),
                        creatorGen.apply(deadCreature, killer)
                );

                if (auxGen != null) item.setAuxData(auxGen.apply(deadCreature, killer));
                if (data1Gen != null) item.setData1(data1Gen.apply(deadCreature, killer));
                if (data2Gen != null) item.setData2(data2Gen.apply(deadCreature, killer));
                if (weightGen != null) item.setWeight(weightGen.apply(deadCreature, killer), false);
                if (damageGen != null) item.setDamage(damageGen.apply(deadCreature, killer));
                if (nameGen != null) item.setName(nameGen.apply(deadCreature, killer));
                if (realTplGen != null) item.setRealTemplate(realTplGen.apply(deadCreature, killer));

                res.add(item);
            } catch (FailedException | NoSuchTemplateException e) {
                LootManager.logger.log(Level.SEVERE, String.format("Error creating drop for %s from %s", killer.getName(), deadCreature.getName()), e);
            }
        }
        return res;
    }
}
