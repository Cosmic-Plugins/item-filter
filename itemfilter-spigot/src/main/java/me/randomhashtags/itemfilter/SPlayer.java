package me.randomhashtags.itemfilter;

import me.randomhashtags.itemfilter.universal.UMaterial;
import me.randomhashtags.itemfilter.universal.UVersionable;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class SPlayer implements UVersionable {
    private static final String PLAYER_DATA_FOLDER = ITEM_FILTER.getDataFolder() + File.separator + "playerData";
    public static final HashMap<UUID, SPlayer> CACHED_PLAYERS = new HashMap<>();

    private boolean isLoaded;
    private UUID uuid;
    private File file;
    private YamlConfiguration yml;

    private boolean filter;
    private List<UMaterial> filteredItems;

    public SPlayer(UUID uuid) {
        this.uuid = uuid;
        final File f = new File(PLAYER_DATA_FOLDER, uuid.toString() + ".yml");
        boolean backup = false;
        if(!CACHED_PLAYERS.containsKey(uuid)) {
            if(!f.exists()) {
                try {
                    final File folder = new File(SPlayer.PLAYER_DATA_FOLDER);
                    if(!folder.exists()) {
                        folder.mkdirs();
                    }
                    f.createNewFile();
                    backup = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            file = new File(PLAYER_DATA_FOLDER, uuid.toString() + ".yml");
            yml = YamlConfiguration.loadConfiguration(file);
            CACHED_PLAYERS.put(uuid, this);
            load();
        }
        if(backup) {
            backup();
        }
    }

    public static SPlayer get(UUID player) {
        return CACHED_PLAYERS.getOrDefault(player, new SPlayer(player));
    }

    public void load() {
        filter = yml.getBoolean("filter enabled");
    }
    public void unload() {
        if(isLoaded) {
            try {
                backup();
            } catch (Exception e) {
                e.printStackTrace();
            }
            isLoaded = false;
            CACHED_PLAYERS.remove(uuid);
        }
    }
    private void save() {
        try {
            yml.save(file);
            yml = YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public UUID getUUID() {
        return uuid;
    }
    public OfflinePlayer getOfflinePlayer() {
        return uuid != null ? Bukkit.getOfflinePlayer(uuid) : null;
    }

    public void backup() {
        yml.set("filter enabled", filter);
        yml.set("filtered items", getFilteredItemz());
        save();
    }

    public boolean hasActiveFilter() {
        return filter;
    }
    public void setActiveFilter(boolean bool) {
        filter = bool;
    }

    public List<UMaterial> getFilteredItems() {
        if(filteredItems == null) {
            filteredItems = new ArrayList<>();
            for(String s : yml.getStringList("filtered items")) {
                filteredItems.add(UMaterial.valueOf(s));
            }
        }
        return filteredItems;
    }
    public List<String> getFilteredItemz() {
        final List<String> f = new ArrayList<>();
        for(UMaterial u : getFilteredItems()) {
            f.add(u.name());
        }
        return f;
    }

}
