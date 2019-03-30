package net.bdew.wurm.tools.server;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides a generic key-value store for custom modded data
 * Data can be attached to anything with a wurmid (items, creatures, players, etc.)
 * Data is identified by a string key, suggested format is {modder}.{mod}.{name} eg "bdew.archeotweaks.cacheprogress"
 * Data is stored as a string, it's (de)serialization and interpretation is up to the mod
 * <p>
 * Currently stored data will be automatically sent to other servers for players and items when they transfer
 * for other objects this can be arranged using packToStream and unpackFromStream methods
 * <p>
 * Currently stored data will be automatically deleted for items and creatures when they are deleted
 * for other objects this can be performed using deleteAll method
 */
public class ModData {
    private static Connection db;
    private static final Logger logger = Logger.getLogger("ModData");

    /**
     * This will be called automatically when the server is started
     */
    public static void init() {
        if (db != null) return;
        try {
            db = ModSupportDb.getModSupportDb();
            if (!ModSupportDb.hasTable(db, "BDEW_EXTENDED_DATA")) {
                try (Statement statement = db.createStatement()) {
                    logger.log(Level.INFO, "Creating table");
                    statement.execute("CREATE TABLE BDEW_EXTENDED_DATA (" +
                            "wurmId INTEGER," +
                            "key TEXT," +
                            "value TEXT," +
                            "PRIMARY KEY(wurmId,key));");
                }
            }
            logger.log(Level.INFO, "ModData initialized");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets extended value for an object (anything with a wurmId - player, creature, item, etc.)
     *
     * @param wurmId Id of the object
     * @param key    Identifier of the data
     * @param value  Value to store
     */
    public static void set(long wurmId, String key, String value) {
        try (PreparedStatement ps = db.prepareStatement("INSERT OR REPLACE INTO BDEW_EXTENDED_DATA (wurmId,key,value) VALUES (?, ?, ?)")) {
            logger.log(Level.FINEST, String.format("Set data %s for %d to %s", key, wurmId, value));
            ps.setLong(1, wurmId);
            ps.setString(2, key);
            ps.setString(3, value);
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes stored data for the given object
     *
     * @param wurmId Id of the object
     * @param key    Identifier of the data to delete
     */
    public static void delete(long wurmId, String key) {
        try (PreparedStatement ps = db.prepareStatement("DELETE FROM BDEW_EXTENDED_DATA WHERE wurmId=? AND key=?")) {
            logger.log(Level.FINEST, String.format("Delete data %s for %d", key, wurmId));
            ps.setLong(1, wurmId);
            ps.setString(2, key);
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes ALL stored data for the given object
     *
     * @param wurmId Id of the object
     */
    public static void deleteAll(long wurmId) {
        try (PreparedStatement ps = db.prepareStatement("DELETE FROM BDEW_EXTENDED_DATA WHERE wurmId=?")) {
            ps.setLong(1, wurmId);
            ps.execute();
            logger.log(Level.FINEST, String.format("Delete all data for %d - deleted %d entries", wurmId, ps.getUpdateCount()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves data for the given object
     *
     * @param wurmId Id of the object
     * @param key    Identifier of the data to delete
     * @return previously stored value or Null
     */
    public static String getNullable(long wurmId, String key) {
        try (PreparedStatement ps = db.prepareStatement("SELECT value FROM BDEW_EXTENDED_DATA WHERE wurmId=? AND key=?")) {
            ps.setLong(1, wurmId);
            ps.setString(2, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Retrieves data for the given object
     *
     * @param wurmId Id of the object
     * @param key    Identifier of the data to delete
     * @return previously stored value or empty optional
     */
    public static Optional<String> get(long wurmId, String key) {
        return Optional.ofNullable(getNullable(wurmId, key));
    }


    /**
     * Serializes all entries for the given object and writes to stream
     *
     * @param wurmId Id of the object
     * @param ds     data stream to write to
     */
    public static void packToStream(long wurmId, DataOutputStream ds) {
        HashMap<String, String> props = new HashMap<>();
        try (PreparedStatement ps = db.prepareStatement("SELECT key,value FROM BDEW_EXTENDED_DATA WHERE wurmId=?")) {
            ps.setLong(1, wurmId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    props.put(rs.getString(1), rs.getString(2));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, String.format("Error getting data for %d during transfer", wurmId), e);
        }

        logger.log(Level.FINEST, String.format("Packing %d entries for %d", props.size(), wurmId));

        try {
            ds.writeInt(props.size());
            for (Map.Entry<String, String> entry : props.entrySet()) {
                logger.log(Level.FINEST, String.format("Packed: %s - %s", entry.getKey(), entry.getValue()));
                ds.writeUTF(entry.getKey());
                ds.writeUTF(entry.getValue());
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, String.format("Error writing data for %d during transfer", wurmId), e);
        }

    }

    /**
     * Loads serialized entries for an object from a data stream
     *
     * @param wurmId Id of the object
     * @param ds     data stream to read from
     */
    public static void unpackFromStream(long wurmId, DataInputStream ds) {
        HashMap<String, String> props = new HashMap<>();
        try {
            for (int count = ds.readInt(); count > 0; count--) {
                props.put(ds.readUTF(), ds.readUTF());
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, String.format("Error reading data for %d during transfer", wurmId), e);
            return; // Don't overwrite db data on error
        }

        logger.log(Level.FINEST, String.format("Unpacking %d entries for %d", props.size(), wurmId));

        try {
            try (PreparedStatement ps = db.prepareStatement("DELETE FROM BDEW_EXTENDED_DATA WHERE wurmId=?")) {
                ps.setLong(1, wurmId);
                ps.execute();
                logger.log(Level.FINEST, String.format("Deleted %d old entries", ps.getUpdateCount()));
            }
            try (PreparedStatement ps = db.prepareStatement("INSERT OR REPLACE INTO BDEW_EXTENDED_DATA (wurmId,key,value) VALUES (?, ?, ?)")) {
                ps.setLong(1, wurmId);
                for (Map.Entry<String, String> entry : props.entrySet()) {
                    logger.log(Level.FINEST, String.format("Unpacked: %s - %s", entry.getKey(), entry.getValue()));
                    ps.setString(2, entry.getKey());
                    ps.setString(3, entry.getValue());
                    ps.execute();
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, String.format("Error saving data for %d during transfer", wurmId), e);
        }
    }

    // ==== creature versions ====

    /**
     * Sets extended value for a creature (or player)
     *
     * @param creature Creature for which the data will be stored
     * @param key      Identifier of the data
     * @param value    Value to store
     */
    public static void set(Creature creature, String key, String value) {
        set(creature.getWurmId(), key, value);
    }

    /**
     * Deletes stored data for the given creature
     *
     * @param creature Creature for which the data will be deleted
     * @param key      Identifier of the data to delete
     */
    public static void delete(Creature creature, String key) {
        delete(creature.getWurmId(), key);
    }

    /**
     * Retrieves data for the given creature
     *
     * @param creature Creature for which the data will be retrieved
     * @param key      Identifier of the data to delete
     * @return previously stored value or Null
     */
    public static String getNullable(Creature creature, String key) {
        return getNullable(creature.getWurmId(), key);
    }

    /**
     * Retrieves data for the given creature
     *
     * @param creature Creature for which the data will be retrieved
     * @param key      Identifier of the data to delete
     * @return previously stored value or empty optional
     */
    public static Optional<String> get(Creature creature, String key) {
        return get(creature.getWurmId(), key);
    }

    // ==== item versions ====

    /**
     * Sets extended value for a item (or player)
     *
     * @param item  Item for which the data will be stored
     * @param key   Identifier of the data
     * @param value Value to store
     */
    public static void set(Item item, String key, String value) {
        set(item.getWurmId(), key, value);
    }

    /**
     * Deletes stored data for the given item
     *
     * @param item Item for which the data will be deleted
     * @param key  Identifier of the data to delete
     */
    public static void delete(Item item, String key) {
        delete(item.getWurmId(), key);
    }

    /**
     * Retrieves data for the given item
     *
     * @param item Item for which the data will be retrieved
     * @param key  Identifier of the data to delete
     * @return previously stored value or Null
     */
    public static String getNullable(Item item, String key) {
        return getNullable(item.getWurmId(), key);
    }

    /**
     * Retrieves data for the given item
     *
     * @param item Item for which the data will be retrieved
     * @param key  Identifier of the data to delete
     * @return previously stored value or empty optional
     */
    public static Optional<String> get(Item item, String key) {
        return get(item.getWurmId(), key);
    }
}
